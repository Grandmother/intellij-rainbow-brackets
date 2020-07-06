package com.github.izhangzhihao.rainbow.brackets.provider

import com.goide.template.GoTemplateTypes.*
import com.intellij.lang.BracePair

class GoTemplateProvider : PairedBraceProvider {

    override val pairs: List<BracePair> = PAIRS

    companion object {
        private val PAIRS: List<BracePair> = listOf(
                BracePair(LDOUBLE_BRACE, RDOUBLE_BRACE, true),
                BracePair(LPAREN, RPAREN, true)
        )
    }
}