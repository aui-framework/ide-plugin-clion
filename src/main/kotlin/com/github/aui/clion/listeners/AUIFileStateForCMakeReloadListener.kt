package com.github.aui.clion.listeners

import com.github.aui.clion.services.AUIProjectService
import com.github.aui.clion.util.AUIVfsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.impl.PsiManagerImpl
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace

/**
 * Schedules CMake configuration reload when a *.cpp file is created, renamed or deleted.
 */
internal class AUIFileStateForCMakeReloadListener : BulkFileListener {
    companion object {
        fun isEventApplicable(event: VFileEvent): Boolean {
            if (event is VFileCreateEvent || event is VFileDeleteEvent) {
                return true
            }
            if (event is VFilePropertyChangeEvent) {
                return event.propertyName == "name"
            }
            return false
        }
    }

    override fun after(events: MutableList<out VFileEvent>) {
        super.after(events)
        var project: Project? = null
        events.forEach {
            if (isEventApplicable(it)) {
                if (project == null) {
                    if (it.requestor is PsiManagerImpl) {
                        project = (it.requestor as PsiManagerImpl).project.apply {
                            if (getService(AUIProjectService::class.java) == null) {
                                return
                            }
                        }
                    }
                }
                val file = it.file ?: return@forEach
                val projectDir = AUIVfsUtil.findModuleRootBySrcFile(file) ?: return@forEach
                val path = it.file?.path ?: return@forEach
                if (path.startsWith("$projectDir/src") || path.startsWith("$projectDir/assets")) {
                    project?.apply {
                        CMakeWorkspace.getInstance(this).scheduleReload()
                        return
                    }
                }
            }
        }
    }
}
