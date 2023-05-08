package com.mohamedrejeb.ksoup.parser

import com.mohamedrejeb.ksoup.tokenizer.Callbacks
import com.mohamedrejeb.ksoup.tokenizer.QuoteType
import com.mohamedrejeb.ksoup.tokenizer.Tokenizer
import com.mohamedrejeb.ksoup.entities.fromCodePoint

class Parser internal constructor(
    private val cbs: Handler,
    private val options: ParserOptions,
    private val tokenizer: Tokenizer
): Callbacks {

    /** The start index of the last event. */
    var startIndex = 0
    /** The end index of the last event. */
    var endIndex = 0
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

    constructor(
        cbs: Handler = Handler.Default,
        options: ParserOptions = ParserOptions.Default
    ) : this(
        cbs = cbs,
        options = options,
        tokenizer = options.tokenizer ?: Tokenizer(
            // TODO()
        )
    )

    // Tokenizer events

    override fun onText(start: Int, endIndex: Int) {
        val data = this.getSlice(start, endIndex)
        this.endIndex = endIndex - 1
        this.cbs.onText(data)
        this.startIndex = endIndex
    }

    override fun onTextEntity(codepoint: Int, endIndex: Int) {
        this.endIndex = endIndex - 1
        this.cbs.onText(fromCodePoint(codepoint))
        this.startIndex = endIndex
    }

    private fun isVoidElement(name: String): Boolean {
        return !this.options.xmlMode && name in voidElements
    }

    override fun onOpenTagName(start: Int, endIndex: Int) {
        this.endIndex = endIndex

        var name = this.getSlice(start, endIndex)

        if (this.lowerCaseTagNames) {
            name = name.toLowerCase()
        }

        this.emitOpenTag(name)
    }

    private fun emitOpenTag(name: String) {
        this.openTagStart = this.startIndex
        this.tagName = name

        if (!this.options.xmlMode) {
            while (
                this.stack.isNotEmpty() &&
                openImpliesClose.containsKey(this.stack.last())
            ) {
                val element = this.stack.removeLast()
                this.cbs.onCloseTag(element, true)
            }
        }
        if (!this.isVoidElement(name)) {
            this.stack.add(name)
            if (name in foreignContextElements) {
                this.foreignContext.add(true)
            } else if (name in htmlIntegrationElements) {
                this.foreignContext.add(false)
            }
        }
        this.cbs.onOpenTagName(name)
        this.attribs = mutableMapOf()
    }

    private fun endOpenTag(isImplied: Boolean) {
        this.startIndex = this.openTagStart

        this.attribs?.let {
            this.cbs.onOpenTag(this.tagName, it, isImplied)
            this.attribs = null
        }
        if (this.isVoidElement(this.tagName)) {
            this.cbs.onCloseTag(this.tagName, true)
        }

        this.tagName = ""
    }

    override fun onOpenTagEnd(endIndex: Int) {
        this.endIndex = endIndex
        this.endOpenTag(false)

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onCloseTag(start: Int, endIndex: Int) {
        this.endIndex = endIndex

        var name = this.getSlice(start, endIndex)

        if (this.lowerCaseTagNames) {
            name = name.lowercase()
        }

        if (
            name in foreignContextElements &&
            name in htmlIntegrationElements
        ) {
            this.foreignContext.removeLast()
        }

        if (!this.isVoidElement(name)) {
            val pos = this.stack.lastIndexOf(name)

            if (pos != -1) {
                var count = this.stack.size - pos
                while (count-- > 0) {
                    val element = this.stack.removeLast()
                    this.cbs.onCloseTag(element, count != 0)
                }
            } else if (!this.options.xmlMode && name == "p") {
                this.emitOpenTag("p")
                this.closeCurrentTag(true)
            }
        } else if (!this.options.xmlMode && name == "br") {
            this.cbs.onOpenTagName("br")
            this.cbs.onOpenTag("br", emptyMap(), true)
            this.cbs.onCloseTag("br", false)
        }

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onSelfClosingTag(endIndex: Int) {
        this.endIndex = endIndex
        if (
            this.options.xmlMode ||
            this.options.recognizeSelfClosing ||
            this.foreignContext.last()
        ) {
            this.closeCurrentTag(false)

            // Set the start index for the next node
            this.startIndex = endIndex + 1
        } else {
            // Ignore the fact that this tag is self-closing
            this.onOpenTagEnd(endIndex)
        }
    }

    private fun closeCurrentTag(isOpenImplied: Boolean) {
        val name = this.tagName
        this.endOpenTag(isOpenImplied)

        // Self-closing tags will be on the top of the stack
        if (this.stack[this.stack.size - 1] == name) {
            // If the opening tag isn't implied, the closing tag has to be implied.
            this.cbs.onCloseTag(name, !isOpenImplied);
            this.stack.removeLast()
        }
    }

    override fun onAttribName(start: Int, endIndex: Int) {
        this.startIndex = start
        val name = this.getSlice(start, endIndex)

        this.attribName = if (this.lowerCaseAttributeNames) {
            name.lowercase()
        } else {
            name
        }
    }

    override fun onAttribData(start: Int, endIndex: Int) {
        this.attribValue += this.getSlice(start, endIndex)
    }

    override fun onAttribEntity(codepoint: Int) {
        this.attribValue += fromCodePoint(codepoint)
    }

    override fun onAttribEnd(quote: QuoteType, endIndex: Int) {
        this.endIndex = endIndex

        this.cbs.onAttribute(
            name = this.attribName,
            value = this.attribValue,
            quote = when (quote) {
                QuoteType.Double -> "\""
                QuoteType.Single -> "'"
                else -> null
            }
        )

        this.attribs?.let {
            it[this.attribName] = this.attribValue
        }
        this.attribValue = ""
    }

    private fun getInstructionName(value: String): String {
        val index = reNameEnd.find(value)?.range?.start ?: -1

        var name = if (index < 0) {
            value
        } else {
            value.substring(0, index)
        }

        if (this.lowerCaseTagNames) {
            name = name.lowercase()
        }

        return name
    }

    override fun onDeclaration(start: Int, endIndex: Int) {
        this.endIndex = endIndex
        val value = this.getSlice(start, endIndex)

        val name = this.getInstructionName(value)
        this.cbs.onProcessingInstruction(name, value)

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onProcessingInstruction(start: Int, endIndex: Int) {
        this.endIndex = endIndex
        val value = this.getSlice(start, endIndex)

        val name = this.getInstructionName(value)
        this.cbs.onProcessingInstruction(name, value)

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onComment(start: Int, endIndex: Int, offset: Int) {
        this.endIndex = endIndex

        this.cbs.onComment(this.getSlice(start, endIndex - offset))
        this.cbs.onCommentEnd()

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onCData(start: Int, endIndex: Int, offset: Int) {
        this.endIndex = endIndex
        val value = this.getSlice(start, endIndex - offset)

        if (this.options.xmlMode ||this.options.recognizeCDATA) {
            this.cbs.onCDataStart()
            this.cbs.onText(value)
            this.cbs.onCDataEnd()
        } else {
            this.cbs.onComment("[CDATA[$value]]")
            this.cbs.onCommentEnd()
        }

        // Set the start index for the next node
        this.startIndex = endIndex + 1
    }

    override fun onEnd() {
        // Set the end index for all remaining tags
        this.endIndex = this.startIndex
        this.stack.indices.forEach { i ->
            val index = this.stack.lastIndex - i
            this.cbs.onCloseTag(this.stack[index], true)
        }
        this.cbs.onEnd()
    }

    /**
     * Resets the parser to a blank state, ready to parse a new HTML document
     */
    fun reset() {
        this.cbs.onReset()
        this.tokenizer.reset()
        this.tagName = ""
        this.attribName = ""
        this.attribValue = ""
        this.attribs = null
        this.stack.clear()
        this.startIndex = 0
        this.endIndex = 0
        this.cbs.onParserInit(this)
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
    fun parseComplete(data: String) {
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
    fun write(chunk: String) {
        if (this.ended) {
            this.cbs.onError(Exception(".write() after done!"))
            return
        }

        this.buffers.add(chunk)
        if (this.tokenizer.running) {
            this.tokenizer.write(chunk)
            this.writeIndex++
        }
    }

    /**
     * Parses the end of the buffer and clears the stack, calls onend.
     *
     * @param chunk Optional final chunk to parse.
     */
    fun end(chunk: String?) {
        if (this.ended) {
            this.cbs.onError(Exception(".end() after done!"))
            return
        }

        chunk?.let { this.write(it) }
        this.ended = true
        this.tokenizer.end()
    }

    /**
     * Pauses parsing. The parser won't emit events until `resume` is called.
     */
    fun pause() {
        this.tokenizer.pause();
    }

    /**
     * Resumes parsing after `pause` was called.
     */
    fun resume() {
        this.tokenizer.resume();

        while (
            this.tokenizer.running &&
            this.writeIndex < this.buffers.length
        ) {
            this.tokenizer.write(this.buffers[this.writeIndex++]);
        }

        if (this.ended) this.tokenizer.end();
    }

    /**
     * Alias of `write`, for backwards compatibility.
     *
     * @param chunk Chunk to parse.
     * @deprecated
     */
    fun parseChunk(chunk: String) {
        this.write(chunk);
    }

    /**
     * Alias of `end`, for backwards compatibility.
     *
     * @param chunk Optional final chunk to parse.
     * @deprecated
     */
    fun done(chunk: String?) {
        this.end(chunk);
    }

    companion object {
        val formTags = setOf(
            "input",
            "option",
            "optgroup",
            "select",
            "button",
            "datalist",
            "textarea",
        )
        val pTag = setOf("p")
        val tableSectionTags = setOf("thead", "tbody")
        val ddtTags = setOf("dt", "dd")
        val rtpTags = setOf("rt", "rp")

        val openImpliesClose = mapOf(
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

        val voidElements = setOf(
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

        val foreignContextElements = setOf(
            "math",
            "svg",
        )

        val htmlIntegrationElements = setOf(
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

        val reNameEnd = Regex("\\s|/")
    }

}