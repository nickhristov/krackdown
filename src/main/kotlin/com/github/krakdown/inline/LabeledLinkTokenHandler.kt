package com.github.krakdown.inline

class LabeledLinkTokenHandler : InlineTokenHandler {
    override fun handleToken(parser: InlineParser, index: Int, tokens: List<InlineToken>, result: MutableList<InlineNode>): Int {
        val token = tokens[index]
        if (token is LabeledLinkToken) {
            result.add(AnchorNode(token.label, token.url))
            return 1
        } else {
            return 0
        }
    }
}