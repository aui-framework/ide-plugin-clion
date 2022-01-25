package com.github.aui.clion.inspections.assetExistence

import com.github.aui.clion.util.AUIVfsUtil
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CopyFileToAssetQuickFix(private val fileName: String, private val assetPath: String): LocalQuickFix {

    override fun getName(): String {
        return "Choose file and copy to '${fileName}'"
    }

    override fun getFamilyName(): String {
        return "Copy"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val projectDir = AUIVfsUtil.findModuleRootBySrcFile(descriptor.psiElement.containingFile) ?: return
        ApplicationManager.getApplication().invokeLater {
            FileChooser.chooseFile(
                FileChooserDescriptor(true, false, false, true, false, false),
                project,
                null
            ) { vf: VirtualFile ->
                ApplicationManager.getApplication().runWriteAction {
                    AUIVfsUtil.makePathAndFile(projectDir, "assets/$assetPath")
                        .setBinaryContent(vf.inputStream.readAllBytes())
                }
            }
        }
    }
}