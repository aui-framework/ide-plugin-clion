package com.github.aui.ideplugin.fileTemplates

import com.github.aui.ideplugin.icons.AUIIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDirectory

class AUIWindowTemplate : AUIMultipleFileTemplate(ACTION_NAME, "Creates new AUI Window.", AUIIcons.WINDOW),
    DumbAware {
    override fun getFileTemplates(): List<String> {
        return listOf("AUI Window Header",
                      "AUI Window CPP")
    }


    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = ACTION_NAME

    private companion object {
        private const val ACTION_NAME = "AUI Window"
    }

}