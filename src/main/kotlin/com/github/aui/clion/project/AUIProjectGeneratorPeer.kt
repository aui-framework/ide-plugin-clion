package com.github.aui.clion.project

import com.github.aui.clion.ConfigurationData
import com.intellij.platform.GeneratorPeerImpl
import javax.swing.JComponent

class AUIProjectGeneratorPeer : GeneratorPeerImpl<ConfigurationData>() {
    private val mNewProjectPanel = AUINewProjectPanel()
    private val mConfig = ConfigurationData()

    override fun getSettings(): ConfigurationData {
        return mConfig
    }

    override fun getComponent(): JComponent = mNewProjectPanel.create(getSettings())
}