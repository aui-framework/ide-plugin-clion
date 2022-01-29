package com.github.aui.clion.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.aui.clion.services.AUIProjectService
import com.intellij.openapi.components.serviceIfCreated

internal class MyProjectManagerListener : ProjectManagerListener {
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
