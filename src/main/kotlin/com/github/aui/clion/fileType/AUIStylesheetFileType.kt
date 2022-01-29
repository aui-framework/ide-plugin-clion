package com.github.aui.clion.fileType

import com.github.aui.clion.AUIIcons
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.jetbrains.cidr.lang.OCLanguage
import javax.swing.Icon

class AUIStylesheetFileType: LanguageFileType(OCLanguage.getInstance()) {
    companion object {
        val INSTANCE = AUIStylesheetFileType()
    }
    override fun getName(): String = "AUI Stylesheet"
    override fun getDescription(): String = "AUI style description file"
    override fun getDefaultExtension(): String = "ass.cpp"
    override fun getIcon(): Icon = AUIIcons.STYLESHEET
}