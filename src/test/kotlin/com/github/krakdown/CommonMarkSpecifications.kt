package com.github.krakdown

import com.github.krakdown.visitors.HtmlVisitor
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.test.assertEquals

@Suppress("unused")
@RunWith(JUnitPlatform::class)
class CommonMarkSpecifications : Spek({

    fun load(filename: String) : List<String> {
        javaClass.classLoader.getResourceAsStream(filename).use {
            val bufferedreader = BufferedReader(InputStreamReader(it))
            val lines = bufferedreader.readLines()
            bufferedreader.close()
            return lines
        }
    }

    fun serializeToHtml(nodes : List<Node>) :String {
        val visitor = HtmlVisitor()
        nodes.forEach { it.visit(visitor) }
        return visitor.content
    }

    given("common-mark specifications") {
        val parser = createBlockParser()
        val lines = load("commonmark.spec")
        val tests = parseTests(lines)
        for((title, markdown, html) in tests) {
            it(title) {
                val nodes = parser.parse(markdown)
                val generatedHtml = serializeToHtml(nodes)
                assertEquals(html, generatedHtml)
            }

        }
    }

})

fun parseTests(lines: List<String>): List<SpecTest> {
    var state = ParseState.TITLE
    val markdown : MutableList<String> = ArrayList<String>()
    var html : MutableList<String> = ArrayList<String>()
    var title = ""
    val result = ArrayList<SpecTest>()

    for (line in lines) {

        if (line == "===================================++++++++++++++++++++++++++") {
            if (state == ParseState.HTML) {
                // we were processing a previous node before
                val (splitTitle, strippedHtml) = separateTitleFromHtml(html)
                html = strippedHtml
                result.add(SpecTest(title, java.lang.String.join("\n", markdown), java.lang.String.join("\n", html)))
                title = splitTitle
                html.clear()
                markdown.clear()
            }
            state = ParseState.MARKDOWN
            continue
        }
        if (line == "-----------------------------------++++++++++++++++++++++++++") {
            state = ParseState.HTML
            continue
        }

        if (state == ParseState.HTML) {
            html.add(line)
        }
        if (state == ParseState.MARKDOWN) {
            markdown.add(line)
        }
        if (state == ParseState.TITLE) {
            title += line
        }
    }
    if (markdown.isNotEmpty() && html.isNotEmpty()) {
        val (_, strippedHtml) = separateTitleFromHtml(html)
        html = strippedHtml
        result.add(SpecTest(title, java.lang.String.join("\n", markdown), java.lang.String.join("\n", html)))
    }
    return result
}

fun separateTitleFromHtml(html: List<String>): TitleAndHtml {
    var title = ""
    var idx = (html.size-1)
    var foundEmpty = false
    for (i in (0..(html.size-1)).reversed()) {
        if (html[i] == "") {
            foundEmpty = true
        } else if (foundEmpty) {
            break
        }
        title = html[i] + title
        idx = i
    }
    return TitleAndHtml(title, html.subList(0, idx).toMutableList())
}

enum class ParseState {
    TITLE, MARKDOWN, HTML
}
data class TitleAndHtml(val title: String, val html: MutableList<String>)

data class SpecTest(val title : String, val markdown:String, val html: String)