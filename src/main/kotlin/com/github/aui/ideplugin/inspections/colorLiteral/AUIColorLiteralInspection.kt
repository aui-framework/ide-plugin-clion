package com.github.aui.ideplugin.inspections.colorLiteral

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.cidr.lang.inspections.OCInspectionBase
import com.jetbrains.cidr.lang.psi.OCLiteralExpression
import com.jetbrains.cidr.lang.psi.OCUDLiteralExpression
import com.jetbrains.cidr.lang.psi.visitors.OCVisitor

class AUIColorLiteralInspection: OCInspectionBase() {
    companion object {
        const val BAD_COLOR_LITERAL_MESSAGE = "Bad color literal padding with zeros"
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : OCVisitor() {
            override fun visitLiteralExpression(expression: OCLiteralExpression?) {
                super.visitLiteralExpression(expression)
                expression?.apply {
                    if (parent is OCUDLiteralExpression) {
                        val hasAlpha = when((parent as OCUDLiteralExpression).name) {
                            "_argb" -> true
                            "_rgb" -> false
                            else -> return
                        }
                        val literalValue = parent.firstChild.text
                        literalValue?.apply {
                            val requiredLength = if (hasAlpha) 8 else 6

                            // hex-style check
                            if (!startsWith("0x") && !startsWith("0X")) {
                                val asInteger = try {
                                    literalValue.toInt()
                                } catch (_: NumberFormatException){
                                    try {
                                        literalValue.toInt(16)
                                    } catch (_: NumberFormatException) {
                                        holder.registerProblem(expression, "Color literal is too long or invalid")
                                        return
                                    }
                                }
                                holder.registerProblem(expression,
                                                       "Use HEX-formatted integer for colors",
                                                       ReplaceLiteralValueQuickFix(this, "0x${asInteger.toString(16).padStart(requiredLength, '0')}"))
                                return
                            }

                            // literal length check
                            val actualPadLength = length - 2
                            if (actualPadLength != requiredLength) {
                                try {
                                    val replacement = "0x${literalValue.substring(2).toInt(16).toString(16).padStart(requiredLength, '0')}"

                                    when (actualPadLength) {
                                        8 -> holder.registerProblem(parent,
                                                                    BAD_COLOR_LITERAL_MESSAGE,
                                                                    ReplaceLiteralNameQuickFix("_rgb", "_argb"),
                                                                    ReplaceLiteralValueQuickFix(this, replacement))
                                        6 -> holder.registerProblem(parent,
                                                                    BAD_COLOR_LITERAL_MESSAGE,
                                                                    ReplaceLiteralValueQuickFix(this, replacement),
                                                                    ReplaceLiteralNameQuickFix("_argb", "_rgb"))
                                        else -> holder.registerProblem(expression,
                                                                    BAD_COLOR_LITERAL_MESSAGE,
                                                                    ReplaceLiteralValueQuickFix(this, replacement))
                                    }

                                } catch (_: NumberFormatException) {
                                    holder.registerProblem(expression, "Color literal is too long or invalid")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

}