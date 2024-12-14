package com.github.aui.ideplugin.inspections.colorLiteral

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.lang.refactoring.util.OCChangeUtil
import com.jetbrains.cidr.lang.util.OCElementFactory

class ReplaceLiteralNameQuickFix(private val before: String, private val after: String): LocalQuickFix {

    override fun getName(): String {
        return "Replace '${before}' with '${after}'"
    }

    override fun getFamilyName(): String {
        return "Replace"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiElement = descriptor.psiElement
        OCChangeUtil.replaceAST(psiElement, OCElementFactory.expressionFromText("${psiElement.firstChild.text}${after}", psiElement.context!!)!!)
    }
}