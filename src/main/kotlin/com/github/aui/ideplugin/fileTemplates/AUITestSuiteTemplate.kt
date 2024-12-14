package com.github.aui.ideplugin.fileTemplates

import com.github.aui.ideplugin.AUIIcons
import com.github.aui.ideplugin.services.AUIProjectService
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class AUITestSuiteTemplate: CreateFileFromTemplateAction(CAPTION, "Creates new AUI Test Suite.", AUIIcons.TEST_SUITE),
    DumbAware {

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = CAPTION

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (!super.isAvailable(dataContext)) return false
        CommonDataKeys.PROJECT.getData(dataContext)?.getService(AUIProjectService::class.java) ?: return false
        return CommonDataKeys.VIRTUAL_FILE.getData(dataContext)?.name.equals("tests")
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(CAPTION)
            .addKind("Empty file", AUIIcons.TEST_SUITE, CAPTION)
    }

    private companion object {
        private const val CAPTION = "AUI Test Suite"
    }
}