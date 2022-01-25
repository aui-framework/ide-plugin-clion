package com.github.aui.clion.preview

import com.github.aui.clion.toolWindow.AUIPreviewToolWindow
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.cidr.lang.psi.OCCallExpression
import com.jetbrains.cidr.lang.psi.OCCastExpression
import com.jetbrains.cidr.lang.psi.OCCompoundInitializer

@Suppress("UnstableApiUsage")
class MyUIElementCaretListener(private val mProject: Project) : CaretListener, Disposable {
    companion object {
        val MATCHED_UI_ELEMENT = TextAttributesKey.createTextAttributesKey("com.github.aui.clion.MATCHED_UI_ELEMENT")
        val KEY_MATCHED_UI_ELEMENT = Key.create<Data>("KEY_MATCHED_UI_ELEMENT")
    }

    data class Data(val rangeHighlighter: RangeHighlighterEx, val elementToSelect: PsiElement, val line: Int)

    override fun caretPositionChanged(event: CaretEvent) {
        super.caretPositionChanged(event)
        val pos = event.newPosition
        val editor = event.editor as EditorImpl
        val offset = editor.logicalPositionToOffset(pos)

        // filter listener in order to optimize
        editor.getUserData(KEY_MATCHED_UI_ELEMENT)?.run {
            if (pos.line == line) return
        }

        val document = editor.document
        (ApplicationManager.getApplication() as ApplicationImpl).executeOnPooledThread {
            var result: Pair<OCCastExpression?, PsiElement?>? = null
            ApplicationManagerEx.getApplicationEx().tryRunReadAction {
                val psiFile = PsiDocumentManager.getInstance(mProject).getPsiFile(document) ?: return@tryRunReadAction
                val elementUnderCaret =
                    PsiTreeUtil.findElementOfClassAtOffset(psiFile, offset, PsiElement::class.java, false) ?: return@tryRunReadAction

                // find root OCCastExpression which is a root container
                result = analyze(elementUnderCaret)
            }
            val rootElement = result?.first ?: return@executeOnPooledThread
            val elementToSelect = result?.second  ?: return@executeOnPooledThread

            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    editor.getUserData(KEY_MATCHED_UI_ELEMENT)?.run {
                        // todo can we reuse that?
                        editor.markupModel.removeHighlighter(rangeHighlighter)
                    }

                    ToolWindowManager.getInstance(mProject).getToolWindow("AUI")?.apply {
                        val preview = this.contentManager.getContent(0)?.component as AUIPreviewToolWindow ?: return@apply
                        preview.sendCodeToCppBackend(rootElement.text)
                    }

                    val highlight = editor.markupModel.addRangeHighlighterAndChangeAttributes(
                        MATCHED_UI_ELEMENT,
                        elementToSelect.startOffset,
                        elementToSelect.endOffset,
                        0,
                        HighlighterTargetArea.EXACT_RANGE,
                        false
                    ) { rangeHightligher -> }

                    editor.putUserData(KEY_MATCHED_UI_ELEMENT, Data(highlight, elementToSelect, pos.line))
                }
            }
        }
    }

    /**
     * Finds selected view and root. The selected can be either view or view container.
     *  - `OCCastExpression` is a container (i.e. `Vertical { ... }`)`
     *  - `OCCallExpression` is a leaf view (i.e. `_new<AButton>("Hello")`)`
     */
    private fun analyze(elementUnderCaret: PsiElement): Pair<OCCastExpression?, PsiElement?> {
        var target: PsiElement? = elementUnderCaret
        var root: OCCastExpression? = null
        var selected: PsiElement? = null
        while (target != null) {
            if (selected == null) {
                if ((target is OCCallExpression && target.parent?.run {
                        this is OCCompoundInitializer || parent?.run {
                            this is OCCompoundInitializer
                        } == true
                    } == true)
                    || target is OCCastExpression) {
                    selected = target
                }
            }
            if (target is OCCastExpression) {
                root = target
                if (target.parent !is OCCompoundInitializer) {
                    break
                }
            }
            target = target.parent
        }
        return Pair(root, selected)
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}