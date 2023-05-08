package com.mohamedrejeb.ksoup.parser

import com.mohamedrejeb.ksoup.tokenizer.Tokenizer

interface ParserOptions {

    /**
     * Indicates whether special tags (`<script>`, `<style>`, and `<title>`) should get special treatment
     * and if "empty" tags (eg. `<br>`) can have children.  If `false`, the content of special tags
     * will be text only. For feeds and other XML content (documents that don't consist of HTML),
     * set this to `true`.
     *
     * @default false
     */
    val xmlMode: Boolean get() = false

    /**
     * Decode entities within the document.
     *
     * @default true
     */
    val decodeEntities: Boolean get() = true

    /**
     * If set to true, all tags will be lowercased.
     *
     * @default !xmlMode
     */
    val lowerCaseTags: Boolean get() = !xmlMode

    /**
     * If set to `true`, all attribute names will be lowercased. This has noticeable impact on speed.
     *
     * @default !xmlMode
     */
    val lowerCaseAttributeNames: Boolean get() = !xmlMode

    /**
     * If set to true, CDATA sections will be recognized as text even if the xmlMode option is not enabled.
     * NOTE: If xmlMode is set to `true` then CDATA sections will always be recognized as text.
     *
     * @default xmlMode
     */
    val recognizeCDATA: Boolean get() = xmlMode

    /**
     * If set to `true`, self-closing tags will trigger the onclosetag event even if xmlMode is not set to `true`.
     * NOTE: If xmlMode is set to `true` then self-closing tags will always be recognized.
     *
     * @default xmlMode
     */
    val recognizeSelfClosing: Boolean get() = xmlMode

    /**
     * Allows the default tokenizer to be overwritten.
     */
    val tokenizer: Tokenizer? get() = null

    object Default : ParserOptions

}