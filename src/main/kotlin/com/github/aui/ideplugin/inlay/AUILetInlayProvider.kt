package com.github.aui.ideplugin.inlay

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.cidr.lang.psi.OCBinaryExpression
import com.jetbrains.cidr.lang.psi.OCCallExpression
import com.jetbrains.cidr.lang.psi.OCLambdaExpression
import com.jetbrains.cidr.lang.psi.OCMacroCall
import com.jetbrains.cidr.lang.types.OCCppReferenceType
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class AUILetInlayProvider: InlayHintsProvider<AUILetInlayProvider.Settings> {
    override val key: SettingsKey<Settings> get() = KEY
    override val name: String = "Type hints"
    override val previewText: String get() = "_new<AButton>(\"Hello\") let { it->setDefault(); }"

    override fun createConfigurable(settings: Settings): ImmediateConfigurable = object : ImmediateConfigurable {
        override val cases: List<ImmediateConfigurable.Case>
            get() = listOf(
                ImmediateConfigurable.Case("Show for let syntax", "let", settings::showForLet)
            )

        override fun createComponent(listener: ChangeListener): JComponent = JPanel()

    }

    override fun createSettings(): Settings = Settings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Settings,
        sink: InlayHintsSink
    ): InlayHintsCollector = object : FactoryInlayHintsCollector(editor) {

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if (element is OCMacroCall) {
                if (element.text == "let") {
                    val parent = element.parent ?: return true
                    if (parent is OCBinaryExpression) {
                        val children = element.parent.children
                        if (children.size == 3) {
                            val lhs = children[0]
                            val rhs = children[2]
                            if (lhs is OCCallExpression) {
                                if (rhs is OCLambdaExpression) {
                                    var type = lhs.resolvedType
                                    if (type is OCCppReferenceType) { // remove reference
                                        type = type.refType
                                    }

                                    val inlay =
                                        factory.roundWithBackground(factory.smallText("it: ${type.name}"))
                                    val offset = rhs.body?.firstChild?.textRange ?: return true
                                    sink.addInlineElement(offset.endOffset, false, inlay, false)
                                }
                            }
                        }
                    }
                }
            }
            return true
        }

    }

    companion object {
        private val KEY: SettingsKey<Settings> = SettingsKey("aui.type.hints")
    }

    data class Settings(
        var showForLet: Boolean = true,
    )
}