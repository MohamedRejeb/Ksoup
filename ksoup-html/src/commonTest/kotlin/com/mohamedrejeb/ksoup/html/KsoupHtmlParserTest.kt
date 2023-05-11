package com.mohamedrejeb.ksoup.html

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import kotlin.test.Test
import kotlin.test.assertEquals

class KsoupHtmlParserTest {

    private fun runHtmlTest(
        input: String,
        options: KsoupHtmlOptions = KsoupHtmlOptions.Default,
        expectedString: String? = null,
        expectedOpenTags: List<String>,
    ) {
        val openTagsList = mutableListOf<String>()
        val unclosedTagList = mutableListOf<String>()
        var string = ""
        val ksoupHtmlParser = KsoupHtmlParser(
            cbs = object : KsoupHtmlHandler {
                override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                    openTagsList.add(name)
                    unclosedTagList.add(name)
                }

                override fun onText(text: String) {
                    if (text.isBlank()) return
                    if (unclosedTagList.lastOrNull() == "style") return
                    if (unclosedTagList.lastOrNull() == "script") return
                    if (unclosedTagList.lastOrNull() == "link") return
                    if (unclosedTagList.lastOrNull() == "meta") return

//                    string += KsoupEntities.decodeHtml5(text)
                    string += text
                }

                override fun onCloseTag(name: String, isImplied: Boolean) {
                    assertEquals(
                        unclosedTagList.removeLast(),
                        name
                    )
                }
            },
            options = options
        )
        ksoupHtmlParser.write(
            input
        )
        ksoupHtmlParser.end()


        assertEquals(
            expectedOpenTags,
            openTagsList
        )
        expectedString?.let {
            assertEquals(
                it,
                string
            )
        }
    }

    @Test
    fun testEmptyHTML() {
        runHtmlTest(
            input = "",
            expectedOpenTags = emptyList(),
        )
    }

    @Test
    fun testSelfClosingTag() {
        runHtmlTest(
            input = "<img src=\"image.jpg\" alt=\"Image\">",
            expectedString = "",
            expectedOpenTags = listOf("img"),
        )
    }

    @Test
    fun testSimpleParagraph() {
        runHtmlTest(
            input = "<p>Hello, world!</p>",
            expectedString = "Hello, world!",
            expectedOpenTags = listOf("p"),
        )
    }

    @Test
    fun testNestedElements() {
        runHtmlTest(
            input =
                """
                    <div>
                        <p>Nested paragraph</p>
                        <ul>
                            <li>Item 1</li>
                            <li>Item 2</li>
                        </ul>
                    </div>
                """.trimIndent(),
            expectedString = "Nested paragraphItem 1Item 2",
            expectedOpenTags = listOf("div", "p", "ul", "li", "li"),
        )
    }

    @Test
    fun testParallelElements() {
        runHtmlTest(
            input =
                """
                    <div>
                        <h1>Title</h1>
                        <p>Paragraph 1</p>
                        <p>Paragraph 2</p>
                    </div>
                """.trimIndent(),
            expectedString = "TitleParagraph 1Paragraph 2",
            expectedOpenTags = listOf("div", "h1", "p", "p"),
        )
    }

    @Test
    fun testComplexStructureWithVariousElements() {
        runHtmlTest(
            input =
                """
                    <div>
                        <header>
                            <h1>Title</h1>
                        </header>
                        <nav>
                            <ul>
                                <li>Home</li>
                                <li>About</li>
                                <li>Contact</li>
                            </ul>
                        </nav>
                        <section>
                            <h2>Content Section</h2>
                            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
                            <div>
                                <h3>Subsection</h3>
                                <p>Subsection content</p>
                            </div>
                        </section>
                        <footer>
                            <p>&copy; 2023 My Website</p>
                        </footer>
                    </div>
                """.trimIndent(),
            expectedString = "TitleHomeAboutContactContent SectionLorem ipsum dolor sit amet, consectetur adipiscing elit.SubsectionSubsection content© 2023 My Website",
            expectedOpenTags = listOf(
                "div", "header", "h1", "nav", "ul", "li", "li", "li", "section", "h2",
                "p", "div", "h3", "p", "footer", "p",
            ),
        )
    }

    @Test
    fun testMultipleNestedElements() {
        runHtmlTest(
            input = """
            <div>
                <header>
                    <h1>Title</h1>
                    <p>Subtitle</p>
                </header>
                <main>
                    <section>
                        <h2>Section Title</h2>
                        <p>Section Content</p>
                    </section>
                    <section>
                        <h2>Another Section</h2>
                        <p>More Content</p>
                    </section>
                </main>
                <footer>
                    <p>&copy; 2023 My Website</p>
                </footer>
            </div>
        """.trimIndent(),
            expectedString = "TitleSubtitleSection TitleSection ContentAnother SectionMore Content© 2023 My Website",
            expectedOpenTags = listOf(
                "div", "header", "h1", "p", "main", "section", "h2", "p",
                "section", "h2", "p", "footer", "p"
            ),
        )
    }

    @Test
    fun testHTMLWithInlineJavaScriptAndCSS() {
        runHtmlTest(
            input = """
            <html>
                <head>
                    <title>My Web Page</title>
                    <style>
                        .my-class {
                            color: blue;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="my-class">Welcome!</h1>
                    <p>This is my web page.</p>
                    <script>
                        function showMessage() {
                            alert('Hello, world!');
                        }
                    </script>
                </body>
            </html>
        """.trimIndent(),
            expectedString = "My Web PageWelcome!This is my web page.",
            expectedOpenTags = listOf(
                "html", "head", "title", "style", "body", "h1", "p", "script"
            ),
        )
    }

    @Test
    fun entityTest() {
        assertEquals(
            "<p>asdf & ÿ ü '</p>",
            KsoupEntities.decodeHtml5("<p>asdf &amp; &yuml; &uuml; &apos;</p>")
        )
    }

    @Test
    fun entityTest2() {
        runHtmlTest(
            input = "<p>&ampa hhh &amp fsdfsdf &amp</p>",
            expectedOpenTags = listOf("p"),
            expectedString = "&ampa hhh &amp fsdfsdf &amp",
        )
    }
}