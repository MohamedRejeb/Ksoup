package com.mohamedrejeb.ksoup.html.parser

import com.mohamedrejeb.ksoup.html.tokenizer.KsoupTokenizer
import com.mohamedrejeb.ksoup.html.tokenizer.KsoupTokenizerCallbacks

public class KsoupHtmlParser(
    public val handler: KsoupHtmlHandler = KsoupHtmlHandler.Default,
    public val options: KsoupHtmlOptions = KsoupHtmlOptions.Default,
    public val callbacks: KsoupTokenizerCallbacks = KsoupTokenizerCallbacks.Default,
) {

    /** The start index of the last event. */
    private var startIndex: Int = 0
    /** The end index of the last event. */
    private var endIndex: Int = 0
    /**
     * Store the start index of the current open tag,
     * so we can update the start index for attributes.
     */
    private var openTagStart = 0

    private var tagName = ""
    private var attribName = ""
    private var attribValue = ""
    private var attribs: MutableMap<String, String>? = null
    private val stack = mutableListOf<String>()
    private val foreignContext = mutableListOf<Boolean>()

    private val buffers = mutableListOf<String>()
    private var bufferOffset = 0
    /** The index of the last written buffer. Used when resuming after a `pause()`. */
    private var writeIndex = 0
    /** Indicates whether the parser has finished running / `.end` has been called. */
    private var ended = false

    private val lowerCaseTagNames get() = options.lowerCaseTags
    private val lowerCaseAttributeNames get() = options.lowerCaseAttributeNames

    // Tokenizer events
    private val ksoupTokenizerCallbacks = object : KsoupTokenizerCallbacks {

        override fun onText(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onText(start, endIndex)
            val data = this@KsoupHtmlParser.getSlice(start, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex - 1
            this@KsoupHtmlParser.handler.onText(data)
            this@KsoupHtmlParser.startIndex = endIndex
        }

        override fun onTextEntity(codepoint: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onTextEntity(codepoint, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex - 1
            this@KsoupHtmlParser.handler.onText(Char(codepoint).toString())
            this@KsoupHtmlParser.startIndex = endIndex
        }

        private fun isVoidElement(name: String): Boolean {
            return !this@KsoupHtmlParser.options.xmlMode && name in voidElements
        }

        override fun onOpenTagName(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onOpenTagName(start, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex

            var name = this@KsoupHtmlParser.getSlice(start, endIndex)

            if (this@KsoupHtmlParser.lowerCaseTagNames) {
                name = name.lowercase()
            }

            this.emitOpenTag(name)
        }

        private fun emitOpenTag(name: String) {
            this@KsoupHtmlParser.openTagStart = this@KsoupHtmlParser.startIndex
            this@KsoupHtmlParser.tagName = name

            val impliesClose = openImpliesClose[name]

            if (!this@KsoupHtmlParser.options.xmlMode && impliesClose != null) {
                while (
                    this@KsoupHtmlParser.stack.isNotEmpty() &&
                    impliesClose.contains(this@KsoupHtmlParser.stack.last())
                ) {
                    val element = this@KsoupHtmlParser.stack.removeLast()

                    this@KsoupHtmlParser.handler.onCloseTag(element, true)
                }
            }
            if (!this.isVoidElement(name)) {
                this@KsoupHtmlParser.stack.add(name)

                if (name in foreignContextElements) {
                    this@KsoupHtmlParser.foreignContext.add(true)
                } else if (name in htmlIntegrationElements) {
                    this@KsoupHtmlParser.foreignContext.add(false)
                }
            }
            this@KsoupHtmlParser.handler.onOpenTagName(name)
            this@KsoupHtmlParser.attribs = mutableMapOf()
        }

        private fun endOpenTag(isImplied: Boolean) {
            this@KsoupHtmlParser.startIndex = this@KsoupHtmlParser.openTagStart

            this@KsoupHtmlParser.attribs?.let {
                this@KsoupHtmlParser.handler.onOpenTag(this@KsoupHtmlParser.tagName, it, isImplied)
                this@KsoupHtmlParser.attribs = null
            }
            if (this.isVoidElement(this@KsoupHtmlParser.tagName)) {
                this@KsoupHtmlParser.handler.onCloseTag(this@KsoupHtmlParser.tagName, true)
            }

            this@KsoupHtmlParser.tagName = ""
        }

        override fun onOpenTagEnd(endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onOpenTagEnd(endIndex)
            this@KsoupHtmlParser.endIndex = endIndex
            this.endOpenTag(false)

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onCloseTag(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onCloseTag(start, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex

            var name = this@KsoupHtmlParser.getSlice(start, endIndex)

            if (this@KsoupHtmlParser.lowerCaseTagNames) {
                name = name.lowercase()
            }

            if (
                name in foreignContextElements &&
                name in htmlIntegrationElements
            ) {
                this@KsoupHtmlParser.foreignContext.removeLast()
            }

            if (!this.isVoidElement(name)) {
                val pos = this@KsoupHtmlParser.stack.lastIndexOf(name)

                if (pos != -1) {
                    var count = this@KsoupHtmlParser.stack.size - pos
                    while (count-- > 0) {
                        val element = this@KsoupHtmlParser.stack.removeLast()
                        this@KsoupHtmlParser.handler.onCloseTag(element, count != 0)
                    }
                } else if (!this@KsoupHtmlParser.options.xmlMode && name == "p") {
                    // Implicit open before close
                    this.emitOpenTag("p")
                    this.closeCurrentTag(true)
                }
            } else if (!this@KsoupHtmlParser.options.xmlMode && name == "br") {
                // We can't use `emitOpenTag` for implicit open, as `br` would be implicitly closed.
                this@KsoupHtmlParser.handler.onOpenTagName("br")
                this@KsoupHtmlParser.handler.onOpenTag("br", emptyMap(), true)
                this@KsoupHtmlParser.handler.onCloseTag("br", false)
            }

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onSelfClosingTag(endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onSelfClosingTag(endIndex)
            this@KsoupHtmlParser.endIndex = endIndex
            if (
                this@KsoupHtmlParser.options.xmlMode ||
                this@KsoupHtmlParser.options.recognizeSelfClosing ||
                this@KsoupHtmlParser.foreignContext.lastOrNull() == true
            ) {
                this.closeCurrentTag(false)

                // Set the start index for the next node
                this@KsoupHtmlParser.startIndex = endIndex + 1
            } else {
                // Ignore the fact that this tag is self-closing
                this.onOpenTagEnd(endIndex)
            }
        }

        private fun closeCurrentTag(isOpenImplied: Boolean) {
            val name = this@KsoupHtmlParser.tagName
            this.endOpenTag(isOpenImplied)

            // Self-closing tags will be on the top of the stack
            if (this@KsoupHtmlParser.stack.size > 0 && this@KsoupHtmlParser.stack[this@KsoupHtmlParser.stack.size - 1] == name) {
                // If the opening tag isn't implied, the closing tag has to be implied.
                this@KsoupHtmlParser.handler.onCloseTag(name, !isOpenImplied)
                this@KsoupHtmlParser.stack.removeLast()
            }
        }

        override fun onAttribName(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onAttribName(start, endIndex)
            this@KsoupHtmlParser.startIndex = start
            val name = this@KsoupHtmlParser.getSlice(start, endIndex)

            this@KsoupHtmlParser.attribName = if (this@KsoupHtmlParser.lowerCaseAttributeNames) {
                name.lowercase()
            } else {
                name
            }
        }

        override fun onAttribData(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onAttribData(start, endIndex)
            this@KsoupHtmlParser.attribValue += this@KsoupHtmlParser.getSlice(start, endIndex)
        }

        override fun onAttribEntity(codepoint: Int) {
            this@KsoupHtmlParser.callbacks.onAttribEntity(codepoint)
            this@KsoupHtmlParser.attribValue += Char(codepoint)
        }

        override fun onAttribEnd(quote: QuoteType, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onAttribEnd(quote, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex

            this@KsoupHtmlParser.handler.onAttribute(
                name = this@KsoupHtmlParser.attribName,
                value = this@KsoupHtmlParser.attribValue,
                quote = when (quote) {
                    QuoteType.Double -> "\""
                    QuoteType.Single -> "'"
                    else -> null
                }
            )

            this@KsoupHtmlParser.attribs?.let {
                it[this@KsoupHtmlParser.attribName] = this@KsoupHtmlParser.attribValue
            }
            this@KsoupHtmlParser.attribValue = ""
        }

        private fun getInstructionName(value: String): String {
            val index = reNameEnd.find(value)?.range?.start ?: -1

            var name = if (index < 0) {
                value
            } else {
                value.substring(0, index)
            }

            if (this@KsoupHtmlParser.lowerCaseTagNames) {
                name = name.lowercase()
            }

            return name
        }

        override fun onDeclaration(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onDeclaration(start, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex
            val value = this@KsoupHtmlParser.getSlice(start, endIndex)

            val name = this.getInstructionName(value)
            this@KsoupHtmlParser.handler.onProcessingInstruction(name, value)

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onProcessingInstruction(start: Int, endIndex: Int) {
            this@KsoupHtmlParser.callbacks.onProcessingInstruction(start, endIndex)
            this@KsoupHtmlParser.endIndex = endIndex
            val value = this@KsoupHtmlParser.getSlice(start, endIndex)

            val name = this.getInstructionName(value)
            this@KsoupHtmlParser.handler.onProcessingInstruction(name, value)

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onComment(start: Int, endIndex: Int, offset: Int) {
            this@KsoupHtmlParser.callbacks.onComment(start, endIndex, offset)
            this@KsoupHtmlParser.endIndex = endIndex

            this@KsoupHtmlParser.handler.onComment(this@KsoupHtmlParser.getSlice(start, endIndex - offset))
            this@KsoupHtmlParser.handler.onCommentEnd()

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onCData(start: Int, endIndex: Int, offset: Int) {
            this@KsoupHtmlParser.callbacks.onCData(start, endIndex, offset)
            this@KsoupHtmlParser.endIndex = endIndex
            val value = this@KsoupHtmlParser.getSlice(start, endIndex - offset)

            if (this@KsoupHtmlParser.options.xmlMode || this@KsoupHtmlParser.options.recognizeCDATA) {
                this@KsoupHtmlParser.handler.onCDataStart()
                this@KsoupHtmlParser.handler.onText(value)
                this@KsoupHtmlParser.handler.onCDataEnd()
            } else {
                this@KsoupHtmlParser.handler.onComment("[CDATA[$value]]")
                this@KsoupHtmlParser.handler.onCommentEnd()
            }

            // Set the start index for the next node
            this@KsoupHtmlParser.startIndex = endIndex + 1
        }

        override fun onEnd() {
            this@KsoupHtmlParser.callbacks.onEnd()
            // Set the end index for all remaining tags
            this@KsoupHtmlParser.endIndex = this@KsoupHtmlParser.startIndex
            this@KsoupHtmlParser.stack.indices.forEach { i ->
                val index = this@KsoupHtmlParser.stack.lastIndex - i
                this@KsoupHtmlParser.handler.onCloseTag(this@KsoupHtmlParser.stack[index], true)
            }
            this@KsoupHtmlParser.handler.onEnd()
        }
    }

    private val ksoupTokenizer = KsoupTokenizer(
        options = options,
        callbacks = ksoupTokenizerCallbacks
    )

    /**
     * Resets the parser to a blank state, ready to parse a new HTML document
     */
    public fun reset() {
        this.handler.onReset()
        this.ksoupTokenizer.reset()
        this.tagName = ""
        this.attribName = ""
        this.attribValue = ""
        this.attribs = null
        this.stack.clear()
        this.startIndex = 0
        this.endIndex = 0
        this.handler.onParserInit(this)
        this.buffers.clear()
        this.bufferOffset = 0
        this.writeIndex = 0
        this.ended = false
    }

    /**
     * Resets the parser, then parses a complete document and
     * pushes it to the handler.
     *
     * @param data Document to parse.
     */
    public fun parseComplete(data: String) {
        this.reset()
        this.end(data)
    }

    private fun getSlice(
        start: Int,
        end: Int
    ): String {
        while (start - this.bufferOffset >= this.buffers.first().length) {
            this.shiftBuffer()
        }

        var slice = this.buffers.first().substring(
            start - this.bufferOffset,
            end - this.bufferOffset
        )

        while (end - this.bufferOffset > this.buffers.first().length) {
            this.shiftBuffer()
            slice += this.buffers.first().substring(0, end - this.bufferOffset)
        }

        return slice
    }

    private fun shiftBuffer() {
        this.bufferOffset += this.buffers.first().length
        this.writeIndex--
        this.buffers.removeFirst()
    }

    /**
     * Parses a chunk of data and calls the corresponding callbacks.
     *
     * @param chunk Chunk to parse.
     */
    public fun write(chunk: String) {
        if (this.ended) {
            this.handler.onError(Exception(".write() after done!"))
            return
        }

        this.buffers.add(chunk)
        if (this.ksoupTokenizer.running) {
            this.ksoupTokenizer.write(chunk)
            this.writeIndex++
        }
    }

    /**
     * Parses the end of the buffer and clears the stack, calls onend.
     *
     * @param chunk Optional final chunk to parse.
     */
    public fun end(chunk: String? = null) {
        if (this.ended) {
            this.handler.onError(Exception(".end() after done!"))
            return
        }

        chunk?.let { this.write(it) }
        this.ended = true
        this.ksoupTokenizer.end()
    }

    /**
     * Pauses parsing. The parser won't emit events until `resume` is called.
     */
    public fun pause() {
        this.ksoupTokenizer.pause()
    }

    /**
     * Resumes parsing after `pause` was called.
     */
    public fun resume() {
        this.ksoupTokenizer.resume()

        while (
            this.ksoupTokenizer.running &&
            this.writeIndex < this.buffers.size
        ) {
            this.ksoupTokenizer.write(this.buffers[this.writeIndex++])
        }

        if (this.ended) this.ksoupTokenizer.end()
    }

    public enum class QuoteType {
        NoValue,
        Unquoted,
        Single,
        Double,
    }

    private companion object {
        private val formTags = setOf(
            "input",
            "option",
            "optgroup",
            "select",
            "button",
            "datalist",
            "textarea",
        )
        private val pTag = setOf("p")
        private val tableSectionTags = setOf("thead", "tbody")
        private val ddtTags = setOf("dt", "dd")
        private val rtpTags = setOf("rt", "rp")

        private val openImpliesClose = mapOf(
            "tr" to setOf("tr", "th", "td"),
            "th" to setOf("th"),
            "td" to setOf("thead", "th", "td"),
            "body" to setOf("head", "link", "script"),
            "li" to setOf("li"),
            "p" to pTag,
            "h1" to pTag,
            "h2" to pTag,
            "h3" to pTag,
            "h4" to pTag,
            "h5" to pTag,
            "h6" to pTag,
            "select" to formTags,
            "input" to formTags,
            "output" to formTags,
            "button" to formTags,
            "datalist" to formTags,
            "textarea" to formTags,
            "option" to setOf("option"),
            "optgroup" to setOf("optgroup", "option"),
            "dd" to ddtTags,
            "dt" to ddtTags,
            "address" to pTag,
            "article" to pTag,
            "aside" to pTag,
            "blockquote" to pTag,
            "details" to pTag,
            "div" to pTag,
            "dl" to pTag,
            "fieldset" to pTag,
            "figcaption" to pTag,
            "figure" to pTag,
            "footer" to pTag,
            "form" to pTag,
            "header" to pTag,
            "hr" to pTag,
            "main" to pTag,
            "menu" to pTag,
            "nav" to pTag,
            "ol" to pTag,
            "pre" to pTag,
            "section" to pTag,
            "table" to pTag,
            "ul" to pTag,
            "rt" to rtpTags,
            "rp" to rtpTags,
            "tbody" to tableSectionTags,
            "tfoot" to tableSectionTags,
        )

        private val voidElements = setOf(
            "area",
            "base",
            "basefont",
            "br",
            "col",
            "command",
            "embed",
            "frame",
            "hr",
            "img",
            "input",
            "isindex",
            "keygen",
            "link",
            "meta",
            "param",
            "source",
            "track",
            "wbr",
        )

        private val foreignContextElements = setOf(
            "math",
            "svg",
        )

        private val htmlIntegrationElements = setOf(
            "mi",
            "mo",
            "mn",
            "ms",
            "mtext",
            "annotation-xml",
            "foreignobject",
            "desc",
            "title",
        )

        private val reNameEnd = Regex("\\s|/")
    }

}