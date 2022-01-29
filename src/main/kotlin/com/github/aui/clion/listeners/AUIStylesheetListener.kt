package com.github.aui.clion.listeners

import com.github.aui.clion.fileType.AUIStylesheetFileType
import com.github.aui.clion.services.AUIProjectService
import com.github.aui.clion.toolWindow.AUIPreviewToolWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.*

/**
 * Listens for *.ass.cpp file.
 */
class AUIStylesheetListener(private val mAUIProjectService: AUIProjectService) : BulkFileListener {

    override fun after(events: MutableList<out VFileEvent>) {
        super.after(events)
        events.forEach {
            if (it.file?.fileType is AUIStylesheetFileType) {
                when (it) {
                    is VFileDeleteEvent -> {
                        if (it.file == mAUIProjectService.stylesheet) {
                            mAUIProjectService.stylesheet = null
                            AUIPreviewToolWindow.of(mAUIProjectService.project)?.setStylesheetCode(null)
                        }
                    }
                    is VFileCreateEvent, is VFilePropertyChangeEvent, is VFileContentChangeEvent -> {
                        mAUIProjectService.stylesheet = it.file
                        val code = mAUIProjectService.stylesheet?.run { String(this.contentsToByteArray()) }
                        AUIPreviewToolWindow.of(mAUIProjectService.project)?.setStylesheetCode(code)
                    }
                }
            }
        }
    }
}
