package com.github.aui.clion.toolWindow

import com.github.aui.clion.aui.AUIViewContainer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager

class AUIPreviewToolWindow(toolWindow: ToolWindow): AUIViewContainer() {
    private var mPreviewNativePtr: Long = 0

    companion object {
        fun of(project: Project): AUIPreviewToolWindow? {
            ToolWindowManager.getInstance(project).getToolWindow("AUI")?.apply {
                return this.contentManager.getContent(0)?.component as AUIPreviewToolWindow
            }
            return null
        }
    }

    private external fun nInit()
    private external fun nParseLayoutCode(layoutCodeToRender: String)
    private external fun nSetStylesheetCode(stylesheet: String?)

    init {
        nInit()
    }

    fun updateLayoutCode(layoutCodeToRender: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            nParseLayoutCode(layoutCodeToRender)
            ApplicationManager.getApplication().invokeLater {
                repaint()
            }
        }
    }

    fun setStylesheetCode(stylesheet: String?) {
        nSetStylesheetCode(stylesheet)
    }


}
