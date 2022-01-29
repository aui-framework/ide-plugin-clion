package com.github.aui.clion.util

import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.jetbrains.cidr.lang.types.OCType
import com.jetbrains.sourceglider.contextSensitive.input.Bool
import java.lang.IllegalStateException

class AUISyntaxUtil {
    companion object {
        fun isLanguageFeatureEmulationBinaryOperator(name: String): Boolean {
            return when (name) {
                "let", "with_style" -> true
                else -> false
            }
        }

        fun isViewType(type: OCType): Boolean {
            return when(type.name) {
                "Vertical", "Horizontal", "Stacked", "Centered" -> true
                else -> false
            }
        }
    }
}
