package com.github.alex2772.auiclionplugin.services

import com.intellij.openapi.project.Project
import com.github.alex2772.auiclionplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
