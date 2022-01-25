package com.github.aui.clion.toolWindow

import com.github.aui.clion.aui.AUIViewContainer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.ToolWindow

class AUIPreviewToolWindow(toolWindow: ToolWindow): AUIViewContainer() {
    private external fun init()

    init {
        init()
    }

    external fun sendCodeToCppBackend(text: String)
}
