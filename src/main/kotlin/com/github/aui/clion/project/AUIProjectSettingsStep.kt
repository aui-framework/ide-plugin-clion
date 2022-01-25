package com.github.aui.clion.project

import com.github.aui.clion.ConfigurationData
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.platform.DirectoryProjectGenerator

class AUIProjectSettingsStep(projectGenerator: DirectoryProjectGenerator<ConfigurationData>) :
    ProjectSettingsStepBase<ConfigurationData>(projectGenerator, AbstractNewProjectStep.AbstractCallback()) {


}
