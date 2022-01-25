@file:Suppress("UnstableApiUsage", "DialogTitleCapitalization")

package com.github.aui.clion.project

import com.github.aui.clion.ConfigurationData
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.jetbrains.cidr.ui.itemValue


class AUINewProjectPanel {

    data class AUIModule(val name: String, val description: String, val enabledByDefault: Boolean = false)
    companion object {
        val allModules = arrayOf(
            AUIModule("views", "UI toolkit", true),
            AUIModule("crypt", "encryption/decryption"),
            AUIModule("curl", "http(s) requests"),
            AUIModule("data", "ORM"),
            AUIModule("json", "JSON parser"),
            AUIModule("xml", "XML parser"),
        )
    }


    val auiSubProject = JBCheckBox("Include AUI as a subproject")
    val staticBuild = JBCheckBox("Static build")
    val assetsCompilations = JBCheckBox("Assets")
    val tests = JBCheckBox("Tests")
    val modules = mutableSetOf("core")

    fun create(config: ConfigurationData) = panel {
        config.panel = this@AUINewProjectPanel
        group("Modules") {
            row {
                checkBox("aui::core").apply {
                    component.isSelected = true
                    component.isEnabled = false
                }
                comment("base functionality")
            }.layout(RowLayout.LABEL_ALIGNED)
            for (i in allModules) {
                row {
                    cell(JBCheckBox("aui::${i.name}").apply {
                        addItemListener {
                            if ((it.item as JBCheckBox).isSelected) {
                                modules.add(i.name)
                            } else {
                                modules.remove(i.name)
                            }
                        }
                        isSelected = i.enabledByDefault
                    })
                    comment(i.description)
                }.layout(RowLayout.LABEL_ALIGNED)
            }
        }

        row {
            cell(auiSubProject)
                .comment("for developing the framework itself")
            comment("(-DAUI_BOOT_AUI_ADD_SUBDIRECTORY=TRUE)")
        }
        row {
            cell(staticBuild)
                .comment("to create a standalone binary")
            comment("(-DBUILD_SHARED_LIBS=FALSE)")
        }
        row {
            cell(assetsCompilations)
                .comment("to compile files into the binary")
            comment("(aui_compile_assets())")
        }
        row {
            cell(tests)
                .comment("to perform unit testing")
            comment("(aui_enable_tests())")
        }
    }
}
