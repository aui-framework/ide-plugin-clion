package com.github.aui.clion.fileTypeOverrider

import com.github.aui.clion.fileType.AUIStylesheetFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile

class AUIStylesheetFileTypeOverrider: FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        if (file.name.endsWith(".${AUIStylesheetFileType.INSTANCE.defaultExtension}")) {
            return AUIStylesheetFileType.INSTANCE
        }
        return null
    }
}