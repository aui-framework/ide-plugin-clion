package com.github.aui.ideplugin.gutter

import com.github.aui.ideplugin.services.AUIProjectService
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.cidr.lang.parser.OCLexerTokenTypes
import com.jetbrains.cidr.lang.psi.OCLiteralExpression
import com.jetbrains.cidr.lang.psi.OCUDLiteralExpression
import com.jetbrains.cidr.lang.refactoring.util.OCChangeUtil
import java.awt.Color

class AUIColorProvider: ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        if (element.project.serviceIfCreated<AUIProjectService>() == null) {
            return null
        }

        // TODO this function should have triggered only for leaf PsiElement; however, it triggers for non-leaf elements
        // because setColorTo could not handle leaf element

        if (element is OCLiteralExpression) {
            if (element.parent is OCUDLiteralExpression) {
                val hasAlpha = when ((element.parent as OCUDLiteralExpression).name) {
                    "_argb" -> true
                    "_rgb" -> false
                    else -> return null
                }
                try {
                    val colorValueInt = element.firstChild.text.substring(2).toInt(16)
                    return Color(
                        (colorValueInt shr 16) and 0xff,
                        (colorValueInt shr 8) and 0xff,
                        colorValueInt and 0xff,
                        if (hasAlpha) (colorValueInt shr 24) and 0xff else 255
                    )
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    companion object {
        fun getColorComponentHex(i: Int): String = i.toString(16).padStart(2, '0')
        fun getColorHex(c: Color): String = "${getColorComponentHex(c.red)}${getColorComponentHex(c.green)}${getColorComponentHex(c.blue)}"
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        val value = if (color.alpha >= 255)
            "0x${getColorHex(color)}"
        else
            "0x${getColorComponentHex(color.alpha)}${getColorHex(color)}"
        OCChangeUtil.replaceChild(element.node, element.firstChild.node, LeafPsiElement(OCLexerTokenTypes.INTEGER_LITERAL, value).node)
        /*

        if (element.isValid) {
            val project = element.project
            val result = Ref.create(element)
            WriteCommandAction.runWriteCommandAction(project,
                "Update color",
                null as String?,
                {
                    if (!project.isDisposed() && !editor.isDisposed) {
                        PsiDocumentManager.getInstance(project).commitDocument(editor.document)
                        if (element.isValid) {

                        }
                    }
                },
                *arrayOf(element.containingFile)
            )
            result.get() as PsiElement
        } else {
            element
        }*/
    }
}