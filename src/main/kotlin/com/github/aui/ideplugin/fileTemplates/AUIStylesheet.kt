package com.github.aui.ideplugin.fileTemplates

import com.github.aui.ideplugin.icons.AUIIcons
import com.github.aui.ideplugin.services.AUIProjectService
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.CreateFromTemplateAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile

class AUIStylesheet : CreateFromTemplateAction<PsiFile>(CAPTION, TEXT, AUIIcons.STYLESHEET),
    DumbAware {

    companion object {
        val CAPTION = "AUI Global Stylesheet"
        val TEXT = "Creates new AUI Global Stylesheet."
        val TEMPLATE = "AUI Global Stylesheet.ass"
    }


    override fun isAvailable(dataContext: DataContext): Boolean {
        if (!super.isAvailable(dataContext)) return false
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false

        return project.getService(AUIProjectService::class.java) != null
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(CAPTION)
            .addKind("Empty file", AUIIcons.WINDOW, TEMPLATE)
    }


    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        return CreateFileFromTemplateAction.createFileFromTemplate(
            name,
            FileTemplateManager.getInstance(dir.project).getInternalTemplate(TEMPLATE),
            dir,
            null,
            true
        )
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = CAPTION

}