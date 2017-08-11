package com.github.krakdown.visitors

import com.github.krakdown.Node
import com.github.krakdown.NodeVisitor
import com.github.krakdown.block.node.*
import com.github.krakdown.inline.BoldStyleNode
import com.github.krakdown.inline.EmStyleNode
import com.github.krakdown.inline.PreformattedStyleNode
import org.w3c.dom.Document

/**
 * A node visitor which directly interacts with the browser DOM and produces a tree of elements as a result
 * of its work.
 *
 * Useful, if it is necessary to interract with the DOM in order to implement the acceptance of a node.
 */
open class AttachingHtmlVisitor(val document:Document) : NodeVisitor<org.w3c.dom.Node> {

    override fun accept(node: Node): org.w3c.dom.Node {
        if (node is HtmlNode) {
            return acceptHtml(node)
        }
        if (node is BlockQuoteNode) {
            return acceptBlockQuote(node)
        }
        if (node is CodeBlockNode) {
            return acceptCodeBlockNode(node)
        }
        if (node is HeaderNode) {
            return acceptHeaderNode(node)
        }
        if (node is TextNode) {
            return acceptTextNode(node)
        }
        if (node is OrderedListNode) {
            return acceptOrderedListNode(node)
        }
        if (node is UnorderedListNode) {
            return acceptUnorderedListNode(node)
        }
        if (node is ParagraphNode) {
            return acceptParagraphNode(node)
        }
        if (node is EmptyLinesNode) {
            return acceptEmptyLines(node)
        }
        if (node is BoldStyleNode) {
            return acceptBoldStyleNode(node)
        }
        if (node is EmStyleNode) {
            return acceptEmStyleNode(node)
        }
        if (node is PreformattedStyleNode) {
            return acceptPreformattedNode(node)
        }
        return acceptUnhandledNode(node)
    }

    private fun acceptPreformattedNode(node: PreformattedStyleNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "pre") { node.nodes }
    }

    private fun acceptEmStyleNode(node: EmStyleNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "em") { node.nodes }
    }

    private fun acceptBoldStyleNode(node: BoldStyleNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "b") { node.nodes }
    }

    private fun <T : Node> createElementWithChildren(node: T,
                                                     elementType: String,
                                                     producer: (T) -> Iterable<Node>): org.w3c.dom.Node {
        val result = document.createElement(elementType)
        producer(node).forEach { accept(it) }
        return result
    }

    private fun acceptEmptyLines(node: EmptyLinesNode): org.w3c.dom.Node {
        return document.createTextNode("")  // ugh, there is no notion of a null object , so this is the closest
                                                 // thing to represent it
    }

    private fun acceptParagraphNode(node: ParagraphNode): org.w3c.dom.Node {
        val result = document.createElement("p")
        result.appendChild(document.createTextNode(node.lines.joinToString("\n")))
        return result
    }

    private fun acceptUnorderedListNode(node: UnorderedListNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "ul") { it.items.flatMap { it.nodes } }
    }

    private fun acceptOrderedListNode(node: OrderedListNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "ol") { it.items.flatMap { it.nodes } }
    }

    private fun acceptTextNode(node: TextNode): org.w3c.dom.Node {
        return document.createTextNode(node.text)
    }

    private fun acceptHeaderNode(node: HeaderNode): org.w3c.dom.Node {
        val el = document.createElement("h${node.size}")
        el.appendChild(document.createTextNode(node.header))
        return el
    }

    private fun acceptBlockQuote(node: BlockQuoteNode): org.w3c.dom.Node {
        return createElementWithChildren(node, "blockquote") { it.nodes }
    }

    open fun acceptHtml(node: HtmlNode): org.w3c.dom.Node {
        val element = document.createElement(node.elType)
        node.attributes.forEach { element.setAttribute(it.key, it.value) }
        return element
    }

    open fun acceptUnhandledNode(node: Node): org.w3c.dom.Node {
        throw Exception("Unhandled node " + node)
    }

    private fun acceptCodeBlockNode(node: CodeBlockNode): org.w3c.dom.Node {
        val preEl = document.createElement("pre")
        val codeEl = document.createElement("code")
        if (node.language != "") {
            codeEl.setAttribute("language", node.language)
        }
        preEl.appendChild(codeEl)
        codeEl.textContent = node.lines.joinToString("\n")
        return preEl
    }
}