package com.github.aui.ideplugin.project

import com.github.aui.ideplugin.ConfigurationData
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