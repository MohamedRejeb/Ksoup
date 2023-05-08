package com.mohamedrejeb.ksoup.parser

interface Handler {

    fun onParserInit(parser: Parser) {}

    /**
     * Reset the handler back to starting state
     */
    fun onReset() {}

    /**
     * Signal that the parser is done parsing the document
     */
    fun onEnd() {}

    fun onError(error: Exception) {}

    fun onCloseTag(name: String, isImplied: Boolean) {}

    fun onOpenTagName(name: String) {}

    /**
     *
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @param quote The quotes used around the attribute. `null` if the attribute has no quotes around the value or if the attribute has no value.
     */
    fun onAttribute(
        name: String,
        value: String,
        quote: String? = null,
    ) {}

    fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {}

    fun onText(text: String) {}

    fun onComment(comment: String) {}

    fun onCDataStart() {}

    fun onCDataEnd() {}

    fun onCommentEnd() {}

    fun onProcessingInstruction(
        name: String,
        data: String,
    ) {}

    object Default : Handler

}