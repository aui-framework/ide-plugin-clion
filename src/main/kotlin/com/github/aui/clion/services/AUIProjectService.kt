package com.github.aui.clion.services

import com.github.aui.clion.listeners.AUIFileStateForCMakeReloadListener
import com.github.aui.clion.listeners.AUIStylesheetListener
import com.github.aui.clion.listeners.AUIElementCaretListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.SVGLoader
import java.awt.Image
import java.awt.MediaTracker
import javax.swing.Icon
import javax.swing.ImageIcon

class AUIProjectService(private val mProject: Project): Disposable {

    private val mCaretListener = AUIElementCaretListener(mProject)

    init {
//        EditorFactory.getInstance().eventMulticaster.addCaretListener(mCaretListener, mCaretListener)
//        EditorFactory.getInstance().eventMulticaster.addDocumentListener(mCaretListener, mCaretListener)

        mProject.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, AUIFileStateForCMakeReloadListener(this))
        mProject.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, AUIStylesheetListener(this))
    }

    private val mAssetIcons = hashMapOf<String, Ref<Icon>>()
    private var mStylesheet: VirtualFile? = null

    var stylesheet get(): VirtualFile? = mStylesheet
                   set(v) {mStylesheet = v}

    val project get() = mProject

    fun getAssetIcon(key: String, virtualFile: VirtualFile): Icon? {
        mAssetIcons[key]?.apply {
            return get()
        }
        val icon = loadAssetIcon(virtualFile)
        mAssetIcons[key] = Ref.create(icon)
        return icon
    }

    private fun loadAssetIcon(virtualFile: VirtualFile): Icon? {
        val url = virtualFile.toNioPath().toUri().toURL()
        val rawImage = ImageIcon(url)
        if (rawImage.imageLoadStatus != MediaTracker.COMPLETE) {
            val svgImage = SVGLoader.load(url, 1.0f)
            if (svgImage != null) {
                return ImageIcon(svgImage.getScaledInstance(12, 12, Image.SCALE_SMOOTH))
            }
            return null
        }
        return ImageIcon(rawImage.image.getScaledInstance(12, 12, Image.SCALE_SMOOTH))
    }

    override fun dispose() {
        mCaretListener.dispose()
    }
}
