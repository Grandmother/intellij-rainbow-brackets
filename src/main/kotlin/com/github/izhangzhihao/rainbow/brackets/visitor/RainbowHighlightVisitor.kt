package com.github.izhangzhihao.rainbow.brackets.visitor

import com.github.izhangzhihao.rainbow.brackets.RainbowHighlighter.getHighlightInfo
import com.github.izhangzhihao.rainbow.brackets.RainbowInfo
import com.github.izhangzhihao.rainbow.brackets.RainbowInfoStore.RAINBOW_INFO_KEY
import com.github.izhangzhihao.rainbow.brackets.settings.RainbowSettings
import com.github.izhangzhihao.rainbow.brackets.util.memoizedFileExtension
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.awt.Color


abstract class RainbowHighlightVisitor : HighlightVisitor {

    private var highlightInfoHolder: HighlightInfoHolder? = null

    override fun suitableForFile(file: PsiFile): Boolean {
        return RainbowSettings.instance.isRainbowEnabled &&
                !RainbowSettings.instance.getLanguageBlacklist.contains(file.fileType.name) &&
                !RainbowSettings.instance.getLanguageBlacklist.contains(memoizedFileExtension(file.name)) &&
                fileIsNotHaskellOrIntelliJHaskellPluginNotEnabled(file.fileType.name)
    }

    private fun fileIsNotHaskellOrIntelliJHaskellPluginNotEnabled(fileType: String) =
            fileType != "Haskell" || !isIntelliJHaskellEnabled

    final override fun analyze(file: PsiFile, updateWholeFile: Boolean, holder: HighlightInfoHolder, action: Runnable)
            : Boolean {
        highlightInfoHolder = holder
        onBeforeAnalyze(file, updateWholeFile)
        action.run()
        onAfterAnalyze()
        return true
    }

    protected open fun onBeforeAnalyze(file: PsiFile, updateWholeFile: Boolean) = Unit

    protected open fun onAfterAnalyze() {
        highlightInfoHolder = null
    }

    protected fun PsiElement.setHighlightInfo(parent: PsiElement?,
                                              level: Int,
                                              startElement: PsiElement?,
                                              endElement: PsiElement?) {
        val holder = highlightInfoHolder ?: return
        @Suppress("USELESS_ELVIS") val schema = holder.colorsScheme?: EditorColorsManager.getInstance().globalScheme
        getHighlightInfo(schema, this, level)
                ?.also {
                    holder.add(it)

                    if (startElement != null || endElement != null) {
                        val color: Color? = it.forcedTextAttributes.foregroundColor
                        color?.let {
                            parent?.saveRainbowInfo(level, color, startElement, endElement)
                        }
                    }
                }
    }

    private fun PsiElement.saveRainbowInfo(level: Int,
                                           color: Color,
                                           startElement: PsiElement?,
                                           endElement: PsiElement?) {
        val rainbowInfo = RAINBOW_INFO_KEY[this]?.also {
            it.level = level
            it.color = color
        } ?: RainbowInfo(level, color).also { RAINBOW_INFO_KEY[this] = it }

        startElement?.let { rainbowInfo.startElement = it }
        endElement?.let { rainbowInfo.endElement = it }
    }

    companion object {
        val isIntelliJHaskellEnabled: Boolean by lazy {
            PluginManagerCore.getPlugin(
                    PluginId.getId("intellij.haskell"))?.isEnabled ?: false
        }
    }
}