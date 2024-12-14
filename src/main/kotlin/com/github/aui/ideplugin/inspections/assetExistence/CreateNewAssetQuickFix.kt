package com.github.aui.ideplugin.inspections.assetExistence

import com.github.aui.ideplugin.util.AUIVfsUtil
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project


class CreateNewAssetQuickFix(private val fileName: String, private val assetPath: String) : LocalQuickFix {

    override fun getName(): String {
        return "Create file '${fileName}'"
    }

    override fun getFamilyName(): String {
        return "Create"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val projectDir = AUIVfsUtil.findModuleRootBySrcFile(descriptor.psiElement.containingFile) ?: return
        ApplicationManager.getApplication().runWriteAction {
            FileEditorManager.getInstance(project)
                .openFile(AUIVfsUtil.makePathAndFile(projectDir, "assets/$assetPath"), true)
        }
    }
}