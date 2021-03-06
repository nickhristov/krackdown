package com.github.krakdown.inline

abstract class ForwardSeekingHandler : InlineTokenHandler {
    final override fun handleToken(parser: InlineParser, index: Int, tokens: List<InlineToken>, result:MutableList<InlineNode>): Int {
        val token = tokens[index]
        var endIdx = index
        if (matchToken(token)) {
            if (index == (tokens.size -1 )) {
                // token at the end of input
                // convert to plain text
                result.addAll(parser.parse(listOf(toInlineText(token))))
                endIdx = index + 1
            } else {
                endIdx = scanForwardMatching(tokens, index + 1, token)
                if (endIdx == 0) {
                    // emptry bold block , no-op
                } else if (endIdx < 0) {
                    // no match, take the rest of the input
                    endIdx = tokens.size - 1
                } else {
                    result.add(makeNode(token, parseSubNodes(parser, tokens.subList(index+1, endIdx))))
                }
                return endIdx + 1 -index
            }
        }
        return endIdx - index
    }

    open fun parseSubNodes(parser: InlineParser, tokens: List<InlineToken>) : List<InlineNode> {
        return parser.parse(tokens)
    }

    abstract fun toInlineText(token: InlineToken): InlineToken

    abstract fun makeNode(token: InlineToken, embeddedNodes: List<InlineNode>): InlineNode

    abstract fun matchToken(token: InlineToken): Boolean

    private fun scanForwardMatching(input: List<InlineToken>, start: Int, token: InlineToken): Int {
        for (idx in start .. (input.size-1)) {
            if (token == input[idx]) {
                return idx
            }
        }
        return -1
    }
}