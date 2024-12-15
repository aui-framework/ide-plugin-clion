package com.github.aui.ideplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.aui.ideplugin.services.AUIProjectService

internal class AUIProjectManagerListener : ProjectManagerListener {
    companion object {
        fun determineIsAuiProject(project: Project): Boolean {
            val cmakeLists = project.baseDir?.findChild("CMakeLists.txt") ?: return false
            try {
                cmakeLists.inputStream.use {
                    return String(cmakeLists.inputStream.readAllBytes()).contains("aui")
                }
            } catch (_: Exception) {}
            return false
        }
    }

    override fun projectOpened(project: Project) {
        if (determineIsAuiProject(project)) {
            project.service<AUIProjectService>()
        }
    }
}
