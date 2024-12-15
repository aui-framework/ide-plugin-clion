package com.github.aui.ideplugin.services

import com.github.aui.ideplugin.listeners.AUIFileStateForCMakeReloadListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace

class AUIProjectService(private val mProject: Project): Disposable {

    init {
//        EditorFactory.getInstance().eventMulticaster.addCaretListener(mCaretListener, mCaretListener)
//        EditorFactory.getInstance().eventMulticaster.addDocumentListener(mCaretListener, mCaretListener)

        mProject.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, AUIFileStateForCMakeReloadListener(this))
    }

    val assetsDir get(): VirtualFile? {
        return projectRoot?.findFileByRelativePath("assets")
    }

    val projectRoot get(): VirtualFile? = CMakeWorkspace.getInstance(project).effectiveContentRoot

    val project get() = mProject

    override fun dispose() {
    }
}
