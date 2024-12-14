package com.github.aui.ideplugin.fileTemplates

import com.github.aui.ideplugin.AUIIcons
import com.github.aui.ideplugin.services.AUIProjectService
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.CreateFromTemplateAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import java.lang.IllegalStateException
import javax.swing.Icon

abstract class AUIMultipleFileTemplate(
    @NlsActions.ActionText private val text: String,
    @NlsActions.ActionDescription description: String,
    icon: Icon
) : CreateFromTemplateAction<PsiFile>(text, description, icon),
    DumbAware {

    abstract fun getFileTemplates(): List<String>

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (!super.isAvailable(dataContext)) return false
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false

        return project.getService(AUIProjectService::class.java) != null
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(text)
            .addKind("Empty file", AUIIcons.WINDOW, getFileTemplates().last())
    }


    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        getFileTemplates().apply {
            var s = size
            forEach {
                s -= 1
                val result = CreateFileFromTemplateAction.createFileFromTemplate(
                    name,
                    FileTemplateManager.getInstance(dir.project).getInternalTemplate(it),
                    dir,
                    null,
                    true
                )
                if (s == 0) {
                    return result
                }
            }
        }
        throw IllegalStateException("createFile shouldn't have reached here")
    }

}