package com.github.aui.ideplugin.fileType

import com.github.aui.ideplugin.AUIIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.jetbrains.cidr.lang.OCLanguage
import javax.swing.Icon

class AUIStylesheetFileType: LanguageFileType(OCLanguage.getInstance()) {
    companion object {
        val INSTANCE = AUIStylesheetFileType()
    }
    override fun getName(): String = "AUI Global Stylesheet"
    override fun getDescription(): String = "AUI style description file"
    override fun getDefaultExtension(): String = "ass.cpp"
    override fun getIcon(): Icon = AUIIcons.STYLESHEET
}