package com.github.aui.clion.listeners

import com.github.aui.clion.services.AUIProjectService
import com.github.aui.clion.toolWindow.AUIPreviewToolWindow
import com.github.aui.clion.util.AUISyntaxUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.prevLeafs
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.oldRange
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.cidr.lang.parser.OCPunctuatorElementType
import com.jetbrains.cidr.lang.psi.*

@Suppress("UnstableApiUsage")
class AUIElementCaretListener(private val mProject: Project) : CaretListener, Disposable, DocumentListener {
    companion object {
        val MATCHED_UI_ELEMENT = TextAttributesKey.createTextAttributesKey("com.github.aui.clion.MATCHED_UI_ELEMENT")
        val KEY_MATCHED_UI_ELEMENT = Key.create<Data>("KEY_MATCHED_UI_ELEMENT")

        /**
         * Finds selected view and root. The selected can be either view or view container.
         *  - `OCCastExpression` is a container (i.e. `Vertical { ... }`)`
         *  - `OCCallExpression` is a leaf view (i.e. `_new<AButton>("Hello")`)`
         */
        fun analyze(psiFile: PsiFile, offset: Int): Pair<OCCastExpression?, PsiElement?> {
            val elementUnderCaret =
                PsiTreeUtil.findElementOfClassAtOffset(psiFile, offset, PsiElement::class.java, false) ?: return Pair(null, null)
            var target: PsiElement? = elementUnderCaret
            var root: OCCastExpression? = null
            var selected: PsiElement? = null

            var triedSwap = false // avoid infinite loop

            while (target != null) {
                if (selected == null) {
                    if (((target is OCCallExpression || target is OCBinaryExpression) && target.parent?.run {
                            this is OCCompoundInitializer || parent?.run {
                                this is OCCompoundInitializer
                            } == true
                        } == true)
                        || target is OCCastExpression) {
                        // handle with_style or let syntax
                        if (target.parent is OCBinaryExpression) {
                            target = target.parent
                        }
                        if (target.run {
                                if (this is OCCastExpression) {
                                    return@run AUISyntaxUtil.isViewType(this.resolvedType)
                                }
                                return@run true
                            }) {
                            selected = target
                        }
                    }
                }
                if (target is OCCastExpression) {
                    root = target
                    when (target.parent) {
                        is OCCompoundInitializer, // container
                        is OCBinaryExpression    // container with with_style or let expression
                        -> {}
                        else -> break
                    }
                }
                var updateTarget = true
                if (!triedSwap) {
                    // if current target is a comma or whitespace we probably stay near the actual element; check it
                    if (isApplicableToFindTargetAround(target!!)) {
                        triedSwap = true
                        target.prevLeaf(true)?.run {
                            target = this
                        }
                    }
                }
                target!!.parent?.run {
                    if (!triedSwap) {
                        // also possibly our current target is a brace or whitespace; first child can be possibly OCCastExpression
                        val children = this.children
                        if (children.size == 1) {
                            val possibleTarget = children.first()
                            if (possibleTarget is OCCastExpression) {
                                target = possibleTarget
                                triedSwap = true
                                updateTarget = false
                            }
                        }
                    }
                }
                if (updateTarget) {
                    target = target!!.parent
                }
            }
            return Pair(root, selected)
        }

        /**
         * @return true if target is a comma or whitespace
         */
        private fun isApplicableToFindTargetAround(target: PsiElement): Boolean {
            return target.elementType is OCPunctuatorElementType
        }
    }

    data class Data(val rangeHighlighter: RangeHighlighterEx, val rootElement: PsiElement, val elementToSelect: PsiElement, val line: Int)

    override fun caretPositionChanged(event: CaretEvent) {
        super.caretPositionChanged(event)
        val pos = event.newPosition
        val editor = event.editor as EditorImpl
        val offset = editor.logicalPositionToOffset(pos)
        val auiProjectService = editor.project?.getServiceIfCreated(AUIProjectService::class.java)?: return
        val document = editor.document

        // filter listener in order to optimize
        document.getUserData(KEY_MATCHED_UI_ELEMENT)?.run {
            if (pos.line == line) {
                return
            }
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            var result: Pair<OCCastExpression?, PsiElement?>? = null
            ApplicationManagerEx.getApplicationEx().tryRunReadAction {
                val psiFile = PsiDocumentManager.getInstance(mProject).getPsiFile(document) ?: return@tryRunReadAction
                // find root OCCastExpression which is a root container
                result = analyze(psiFile, offset)
            }
            val rootElement = result?.first ?: return@executeOnPooledThread
            val elementToSelect = result?.second  ?: return@executeOnPooledThread

            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    document.getUserData(KEY_MATCHED_UI_ELEMENT)?.run {
                        if (this.rootElement == rootElement) {
                            return@runWriteAction
                        }

                        // todo can we reuse that?
                        editor.markupModel.removeHighlighter(rangeHighlighter)
                    }

                    AUIPreviewToolWindow.of(mProject)?.updateLayoutCode(rootElement.text)

                    val highlight = editor.markupModel.addRangeHighlighterAndChangeAttributes(
                        MATCHED_UI_ELEMENT,
                        elementToSelect.startOffset,
                        elementToSelect.endOffset,
                        0,
                        HighlighterTargetArea.EXACT_RANGE,
                        false
                    ) { rangeHightligher -> }

                    document.putUserData(KEY_MATCHED_UI_ELEMENT, Data(highlight, rootElement, elementToSelect, pos.line))
                }
            }
        }
    }


    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        event.document.getUserData(KEY_MATCHED_UI_ELEMENT)?.run {
            if (rootElement.textRange.intersects(event.oldRange)) {
                try {
                    AUIPreviewToolWindow.of(mProject)
                        ?.updateLayoutCode(event.document.getText(rootElement.textRange.grown(event.newLength - event.oldLength)))
                } catch (_:Exception) {} // could throw out of bounds
            }
        }
    }

    override fun dispose() {
        EditorFactory.getInstance().eventMulticaster.removeCaretListener(this)
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(this)
    }
}