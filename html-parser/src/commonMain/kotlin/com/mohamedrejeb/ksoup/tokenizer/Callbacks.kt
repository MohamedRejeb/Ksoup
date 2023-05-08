package com.mohamedrejeb.ksoup.tokenizer

interface Callbacks {

    fun onAttribData(
        start: Int,
        endIndex: Int
    )

    fun onAttribEntity(codepoint: Int)

    fun onAttribEnd(quote: QuoteType, endIndex: Int)

    fun onAttribName(start: Int, endIndex: Int)

    fun onCData(start: Int, endIndex: Int, offset: Int)

    fun onCloseTag(start: Int, endIndex: Int)

    fun onComment(start: Int, endIndex: Int, offset: Int)

    fun onDeclaration(start: Int, endIndex: Int)

    fun onEnd()

    fun onOpenTagEnd(endIndex: Int)

    fun onOpenTagName(start: Int, endIndex: Int)

    fun onProcessingInstruction(start: Int, endIndex: Int)

    fun onSelfClosingTag(endIndex: Int)

    fun onText(start: Int, endIndex: Int)

    fun onTextEntity(
        codepoint: Int,
        endIndex: Int
    )

}