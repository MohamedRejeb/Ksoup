package com.mohamedrejeb.ksoup

import com.mohamedrejeb.ksoup.entities.Maps
import com.mohamedrejeb.ksoup.parser.Handler
import com.mohamedrejeb.ksoup.parser.Parser
import com.mohamedrejeb.ksoup.scripts.trie.encodeTrie
import com.mohamedrejeb.ksoup.scripts.trie.getTrie
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonTest {

    @Test
    fun htmlTest() {
        val openTagList = mutableListOf<String>()
        var string = ""
        val parser = Parser(
            cbs = object : Handler {
                override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                    println("Open tag: $name")
                    println("Attributes: $attributes")
                    println("Implied: $isImplied")
                    openTagList.add(name)
                }

                override fun onText(text: String) {
                    println("Text: \"$text\"")
                    val start = if (text.startsWith(" ")) " " else ""
                    val end = if (text.endsWith(" ")) " " else ""
                    string += start + text.trim() + end
                }

                override fun onCloseTag(name: String, isImplied: Boolean) {
                    println("Close tag: $name")
                    println("Implied: $isImplied")
                }
            }
        )
        parser.write(
            """
                <html>
                    <head>
                        <title>Test</title>
                    </head>
                    <body>
                        <header>
                            <p>Header <span>Mohamed Rejeb</span></p>
                        </header>
                        
                        <div>
                            <p>Test</p>
                        </div>
                        
                        <footer>
                            <p>Footer <span>Copyrights</span></p>
                        </footer>
                    </body>
                </html>
            """.trimIndent()
        )
        parser.end()

        assertEquals(
            string,
            "TestHeaderMohamed RejebTestFooter Copyrights"
        )
        assertEquals(
            openTagList,
            listOf(
                "html",
                "head",
                "title",
                "body",
                "header",
                "p",
                "span",
                "div",
                "p",
                "footer",
                "p",
                "span"
            )
        )
    }

    @Test
    fun simpleTestExample() {
        assertTrue(Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }

    @Test
    fun otherTestExample() {
        val name: String = "html"
        val map = Maps.Entities.map
        val legacy = Maps.Legacy.map

//        val encoded = encodeTrie(getTrie(map, legacy))
//        val stringified = encoded.map { Char(it) }.joinToString()

        println(getTrie(map, legacy))

        assertTrue(Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }

    @Test
    fun testExample() {
        val name: String = "html"
//        val map = Maps.Entities.map
//        val legacy = Maps.Legacy.map
        val map = Maps.Xml.map
        val legacy = emptyMap<String, String>()

        val encoded = encodeTrie(getTrie(map, legacy))
//        Char(encoded)
//        val stringified = encoded.map { Char(it) }.joinToString()

        println(encoded)

        assertTrue(Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun testExample2() {
        val name: String = "html"
        val map = Maps.Xml.map
        val legacy = emptyMap<String, String>()

        val encoded = encodeTrie(getTrie(map, legacy))
        val stringified = encoded.map { Char(it) }.joinToString()
            .replace(Regex("[^\\x20-\\x7e]")) { matchResult ->
                "\\u${matchResult.value[0].code.toString(16).padStart(4, '0')}"
            }
            .replace("\\u0000", "\\0")
            .replace(Regex("\\\\u00([\\da-f]{2})")) { matchResult ->
                "\\x${matchResult.value.substring(3)}"
            }

        println("""
            ${
                stringified.map { it.code.toUShort() }.toUShortArray()
            }
        """.trimIndent())

//        println("""
//            mapOf(
//                ${map.map { (key, value) -> "\"$key\" to \"$value\"" }.joinToString(", ")}
//            )
//        """.trimIndent())

        assertTrue(true)
    }
}