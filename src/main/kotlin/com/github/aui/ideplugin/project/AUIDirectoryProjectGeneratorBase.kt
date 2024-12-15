@file:Suppress("DialogTitleCapitalization")

package com.github.aui.ideplugin.project

import com.github.aui.ideplugin.ConfigurationData
import com.github.aui.ideplugin.icons.AUIIcons
import com.intellij.ide.impl.setTrusted
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.ProjectGeneratorPeer
import com.jetbrains.cidr.cpp.cmake.projectWizard.generators.CLionProjectGenerator
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import java.util.function.Consumer
import javax.swing.Icon

abstract class AUIDirectoryProjectGeneratorBase : CLionProjectGenerator<ConfigurationData>(),
    CustomStepProjectGenerator<ConfigurationData> {

    override fun getGroupName(): String = "AUI"
    override fun getGroupDisplayName(): String = groupName

    override fun createPeer(): ProjectGeneratorPeer<ConfigurationData> = AUIProjectGeneratorPeer()

    override fun getLogo(): Icon? {
        return AUIIcons.LOGO
    }

    abstract fun getCMakeTargetDeclaration(projectName: String): String

    abstract fun isLibrary(): Boolean

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: ConfigurationData, module: Module) {
        val projectName = project.name
        ApplicationManager.getApplication().runWriteAction {
            if (settings.panel.assetsCompilations.isSelected) {
                AUIIcons::class.java.getResourceAsStream("/icons/logo.svg")?.let {
                    baseDir.createChildDirectory(this, "assets")
                        .createChildData(this, "logo.svg")
                        .setBinaryContent(it.readAllBytes())
                }
            }
            if (settings.panel.tests.isSelected) {
                val testSuiteName = "${projectName}Tests".replace("-", "")
                baseDir.createChildDirectory(this, "tests")
                    .createChildData(this, "${testSuiteName}.cpp")
                    .setBinaryContent("""
#include <gtest/gtest.h>

TEST(${projectName}, Simple) {
    EXPECT_EQ(2 + 2, 4);
}
                    """.trimIndent().toByteArray())
            }

            val cmakeFile = baseDir.createChildData(this, "CMakeLists.txt").apply { setBinaryContent(
                """
# Standard routine
cmake_minimum_required(VERSION 3.16)
project(${projectName})

${if (settings.panel.staticBuild.isSelected) "set(BUILD_SHARED_LIBS OFF)\n" else ""}

set(AUI_VERSION ${settings.panel.version.text})

# Use AUI.Boot
file(
        DOWNLOAD
        https://raw.githubusercontent.com/aui-framework/aui/${'$'}{AUI_VERSION}/aui.boot.cmake
        ${'$'}{CMAKE_CURRENT_BINARY_DIR}/aui.boot.cmake)
include(${'$'}{CMAKE_CURRENT_BINARY_DIR}/aui.boot.cmake)

# link AUI
auib_import(aui https://github.com/aui-framework/aui
            COMPONENTS ${settings.panel.modules.joinToString(" ")}
            VERSION ${'$'}{AUI_VERSION})

${getCMakeTargetDeclaration(projectName)}

# Link required libs
aui_link(${projectName} PRIVATE ${settings.panel.modules.joinToString(" ") { s: String -> "aui::${s}" }})

${if (settings.panel.tests.isSelected) "aui_enable_tests(${projectName})" else ""}
${if (settings.panel.assetsCompilations.isSelected) "aui_compile_assets(${projectName})" else ""}
        """.trimIndent().toByteArray()
            ) }
            val mainFile = baseDir.createChildDirectory(this, "src")
                .createChildData(this, "main.cpp").apply {
                    if (settings.panel.modules.contains("views") && !isLibrary()) {
                        setBinaryContent(
                            """
#include <AUI/Platform/Entry.h>
#include <AUI/Platform/AWindow.h>
#include <AUI/Util/UIBuildingHelpers.h>
${ if (settings.panel.assetsCompilations.isSelected) "#include <AUI/View/ADrawableView.h>" else "" }

using namespace declarative;

class MyWindow: public AWindow {
public:
    MyWindow(): AWindow("${projectName}", 300_dp, 200_dp)
    {
        setContents(
          Centered {
            Vertical {
               ${ if (settings.panel.assetsCompilations.isSelected) "Icon { \":logo.svg\" } with_style { FixedSize { 64_dp } }," else "" }
              _new<ALabel>("Hello world, ${projectName}!"),
            }
          }
        );
    }
};

AUI_ENTRY {
    _new<MyWindow>()->show();
    return 0;
}
            """.trimIndent().toByteArray())
                    } else {
                        setBinaryContent(
                            """
#include <AUI/Platform/Entry.h>
#include <AUI/Logging/ALogger.h>

AUI_ENTRY {
    ALogger::info("Hello world!");
    return 0;
}
            """.trimIndent().toByteArray())
                    }
                }
            // open files in editor
            if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
                PsiNavigationSupport.getInstance().createNavigatable(project, cmakeFile, -1).navigate(false)
                listOf(cmakeFile, mainFile).forEach(Consumer { vf: VirtualFile? ->
                    PsiNavigationSupport.getInstance().createNavigatable(
                        project,
                        vf!!, -1
                    ).navigate(true)
                })
            }
        }
        project.setTrusted(true)                                                                   // trust the project
        var cmake = CMakeWorkspace.getInstance(project)

        // write -DAUIB_AUI_AS=TRUE
        if (settings.panel.auiSubProject.isSelected) {
            cmake.settings.profiles = cmake.settings.profiles.map { it.withGenerationOptions("-DAUIB_AUI_AS=TRUE") }
        }

        cmake.selectProjectDir(VfsUtilCore.virtualToIoFile(baseDir))                               // load CMake project
    }

    override fun createStep(
        projectGenerator: DirectoryProjectGenerator<ConfigurationData>?,
        callback: AbstractNewProjectStep.AbstractCallback<ConfigurationData>?
    ): AbstractActionWithPanel {
        return AUIProjectSettingsStep(projectGenerator!!)
    }
}