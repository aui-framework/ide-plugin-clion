package com.github.aui.clion.gutter

import com.github.aui.clion.AUIIcons
import com.github.aui.clion.services.AUIProjectService
import com.github.aui.clion.util.AUIVfsUtil
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
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
        if (element is LeafPsiElement) {
            val parent = element.parent
            if (parent is OCLiteralExpression) {
                val unescaped = parent.unescapedLiteralText
                if (unescaped.startsWith(":")) {
                    // asset url literal
                    val pathWithoutColon = unescaped.substring(1)

                    val auiProjectService = element.project.getService(AUIProjectService::class.java)


                    AUIVfsUtil.findModuleRootBySrcFile(element.containingFile)?.findFileByRelativePath("assets/$pathWithoutColon")?.apply {
                        val builder = NavigationGutterIconBuilder
                            .create(auiProjectService.getAssetIcon(pathWithoutColon, this) ?: AUIIcons.RESOURCES_ROOT)
                            .setTooltipTitle("Go To Asset File")
                            .setTooltipText("Asset: $pathWithoutColon")
                            .setTargets(PsiManager.getInstance(element.project).findFile(this))
                        result.add(builder.createLineMarkerInfo(element))
                    }
                }
            }
        }
    }
}