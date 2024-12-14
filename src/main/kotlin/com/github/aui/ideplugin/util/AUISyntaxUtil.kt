package com.github.aui.ideplugin.util

import com.jetbrains.cidr.lang.types.OCType

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
