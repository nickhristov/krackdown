package com.github.krakdown.block.node

import com.github.krakdown.Node
import com.github.krakdown.inline.InlineNode

data class HtmlNode (val elType: String, val attributes : Map<String, String>) : Node()
data class HeaderNode(val size: Int, val header: String) : Node()
data class BlockQuoteNode (val nodes: List<Node>) : Node()
data class CodeBlockNode(val lines: List<String>, var language: String) : Node()
data class EmptyLinesNode(val number: Int) : Node()
data class ParagraphNode(val lines: List<String>) : Node()

data class ListNodeItem(var nodes: List<Node>, var loose: Boolean = false)
data class TextNode(var text: String) : InlineNode()
abstract class ListNode(open val items: List<ListNodeItem>) : Node()

data class OrderedListNode(override val items: List<ListNodeItem>, var start: Int = 0) : ListNode(items)
data class UnorderedListNode(override val items: List<ListNodeItem>) : ListNode(items)