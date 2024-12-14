package com.github.aui.ideplugin.project

import com.github.aui.ideplugin.ConfigurationData
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.platform.DirectoryProjectGenerator

class AUIProjectSettingsStep(projectGenerator: DirectoryProjectGenerator<ConfigurationData>) :
    ProjectSettingsStepBase<ConfigurationData>(projectGenerator, AbstractNewProjectStep.AbstractCallback()) {


}
