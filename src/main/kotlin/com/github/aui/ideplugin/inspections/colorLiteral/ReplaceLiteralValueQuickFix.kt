package com.github.aui.ideplugin.inspections.colorLiteral

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.lang.psi.OCUDLiteralExpression
import com.jetbrains.cidr.lang.refactoring.util.OCChangeUtil
import com.jetbrains.cidr.lang.util.OCElementFactory

class ReplaceLiteralValueQuickFix(private val before: String, private val after: String): LocalQuickFix {

    override fun getName(): String {
        return "Replace '${before}' with '${after}'"
    }

    override fun getFamilyName(): String {
        return "Replace"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        var psiElement = descriptor.psiElement
        if (psiElement is OCUDLiteralExpression) {
            psiElement = psiElement.firstChild ?: return
        }
        OCChangeUtil.replaceAST(psiElement, OCElementFactory.expressionFromText(after, psiElement.context!!)!!)
    }
}