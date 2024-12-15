package com.github.aui.ideplugin.gutter

import com.github.aui.ideplugin.icons.AUIIcons
import com.github.aui.ideplugin.AUIBundle
import com.github.aui.ideplugin.services.AUIProjectService
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.cidr.lang.psi.OCLiteralExpression

class AUIAssetGutter: RelatedItemLineMarkerProvider()
{
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        super.collectNavigationMarkers(element, result)
        val auiProject = element.project.serviceIfCreated<AUIProjectService>() ?: return
        if (element is LeafPsiElement) {
            val parent = element.parent
            if (parent is OCLiteralExpression) {
                val unescaped = parent.unescapedLiteralText
                if (unescaped.startsWith(":")) {
                    // asset url literal
                    val pathWithoutColon = unescaped.substring(1)

                    auiProject.assetsDir?.findFileByRelativePath(pathWithoutColon)?.apply {
                        val builder = NavigationGutterIconBuilder
                            .create(fileType.icon ?: AUIIcons.RESOURCES_ROOT)
                            .setTooltipTitle(AUIBundle.message("action.AUI.goto_asset.title"))
                            .setTooltipText(AUIBundle.message("action.AUI.goto_asset.text", pathWithoutColon))
                            .setTargets(PsiManager.getInstance(element.project).findFile(this))
                        result.add(builder.createLineMarkerInfo(element))
                    }
                }
            }
        }
    }
}