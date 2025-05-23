@file:Suppress("DialogTitleCapitalization")

package com.github.aui.ideplugin.project

import com.github.aui.ideplugin.ConfigurationData
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator

class AUILibraryDirectoryProjectGenerator : AUIDirectoryProjectGeneratorBase(),
    CustomStepProjectGenerator<ConfigurationData> {
    override fun getName(): String {
        return "AUI Library"
    }

    override fun getCMakeTargetDeclaration(projectName: String) = """
# Create the library. This function automatically links all sources from the src/ folder, creates CMake target and
# places the resulting binaries to bin/ and lib/ folders.
aui_module(${projectName})
    """.trimIndent()

    override fun isLibrary(): Boolean = true

}