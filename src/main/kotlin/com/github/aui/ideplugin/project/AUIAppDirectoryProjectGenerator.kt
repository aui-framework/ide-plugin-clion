@file:Suppress("DialogTitleCapitalization")

package com.github.aui.ideplugin.project

import com.github.aui.ideplugin.ConfigurationData
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator

class AUIAppDirectoryProjectGenerator : AUIDirectoryProjectGeneratorBase(),
    CustomStepProjectGenerator<ConfigurationData> {
    override fun getName(): String {
        return "AUI Application"
    }
    override fun isLibrary(): Boolean = false

    override fun getCMakeTargetDeclaration(projectName: String) = """
# Create the executable. This function automatically links all sources from the src/ folder, creates CMake target and
# places the resulting executable to bin/ folder.
aui_executable(${projectName})
    """.trimIndent()

}