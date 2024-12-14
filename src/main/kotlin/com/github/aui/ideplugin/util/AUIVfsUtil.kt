package com.github.aui.ideplugin.util

import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.lang.IllegalStateException

class AUIVfsUtil {
    companion object {
        fun makePathAndFile(baseDir: VirtualFile, filePath: String): VirtualFile {
            val target = Ref.create(baseDir)
            val splt = filePath.split('/')
            splt.forEachIndexed { i: Int, it: String  ->
                if (i + 1 == splt.size) {
                    return target.get()!!.createChildData(this, it)
                } else {
                    target.set(target.get()!!.findChild(it) ?: target.get()!!.createChildDirectory(this, it))
                }
            }
            throw IllegalStateException("shouldn't have reached here")
        }

        fun findModuleRootBySrcFile(containingFile: PsiFile): VirtualFile? {
            return findModuleRootBySrcFile(containingFile.virtualFile!!)
        }

        fun findModuleRootBySrcFile(containingFile: VirtualFile): VirtualFile? {
            var target = containingFile.parent
            while (target != null) {
                if (target.name == "src") {
                    return target.parent
                }
                target = target.parent
            }
            return null
        }
    }
}
