package com.github.aui.clion

import com.intellij.openapi.util.IconLoader

class AUIIcons {
    companion object {
        val LOGO = IconLoader.getIcon("/icons/logo.svg", AUIIcons::class.java)
        val WINDOW = IconLoader.getIcon("/icons/auiWindow.svg", AUIIcons::class.java)
        val TEST_SUITE = IconLoader.getIcon("/icons/auiTestSuite.svg", AUIIcons::class.java)
        val RESOURCES_ROOT = IconLoader.getIcon("/icons/auiResourcesRoot.svg", AUIIcons::class.java)
    }
}