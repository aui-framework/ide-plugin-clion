package com.github.aui.clion.inspections.assetExistence

import com.github.aui.clion.util.AUIVfsUtil
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.cidr.lang.inspections.OCInspectionBase
import com.jetbrains.cidr.lang.psi.OCLiteralExpression
import com.jetbrains.cidr.lang.psi.OCStringLiteralExpression
import com.jetbrains.cidr.lang.psi.visitors.OCVisitor
import kotlin.io.path.Path

class AUIAssetExistenceInspection: OCInspectionBase() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : OCVisitor() {
            override fun visitLiteralExpression(expression: OCLiteralExpression?) {
                try {
                    if (expression is OCStringLiteralExpression) {
                        expression.escapedLiteralText.apply {
                            if (startsWith(":") && !contains("\\s")) {
                                // target expression is probably an url string l
                                // iteral
                                val assetPath = this.substring(1)
                                val projectDir = AUIVfsUtil.findModuleRootBySrcFile(expression.containingFile) ?: return
                                val path = projectDir.findChild("assets")?.findFileByRelativePath(assetPath)
                                if (path == null) {
                                    val fileName = Path(assetPath).fileName.toString()
                                    holder.registerProblem(
                                        expression,
                                        "No such file: $fileName",
                                        CreateNewAssetQuickFix(fileName, assetPath),
                                        CopyFileToAssetQuickFix(fileName, assetPath),
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

        }
    }

}