package com.github.izhangzhihao.rainbow.brackets

import com.github.izhangzhihao.rainbow.brackets.action.AbstractScopeHighlightingAction
import com.intellij.openapi.util.Key

object RainbowInfoStore {
    val RAINBOW_INFO_KEY: Key<RainbowInfo> = Key.create("RAINBOW_INFO")
    val HIGHLIGHTING_DISPOSER_KEY: Key<AbstractScopeHighlightingAction.HighlightingDisposer> = Key.create("HIGHLIGHTING_DISPOSER_KEY")
}