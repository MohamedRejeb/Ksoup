package com.mohamedrejeb.ksoup.entities

import com.mohamedrejeb.ksoup.entities.text.translate.NumericEntityEncoder
import com.mohamedrejeb.ksoup.entities.text.translate.NumericEntityDecoder
import com.mohamedrejeb.ksoup.entities.text.translate.UnicodeUnpairedSurrogateRemover


/**
 * Encodes and decodes `String`s for HTML and XML.
 */
public object KsoupEntities {

    /**
     * Translator object for escaping XML 1.1.
     *
     * While [encodeXml] is the expected method of use, this
     * object allows the XML escaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val ENCODE_XML: StringTranslator = run {
        val encodeXml11Map = mapOf(
            "\u0000" to "",
            "\u000b" to "&#11;",
            "\u000c" to "&#12;",
            "\ufffe" to "",
            "\uffff" to "",
        )

        AggregateTranslator(
            LookupTranslator(EntityMaps.XMLEncode),
            LookupTranslator(encodeXml11Map),
            NumericEntityEncoder.between(0x1, 0x8),
            NumericEntityEncoder.between(0xe, 0x1f),
            NumericEntityEncoder.between(0x7f, 0x84),
            NumericEntityEncoder.between(0x86, 0x9f),
            UnicodeUnpairedSurrogateRemover()
        )
    }

    /**
     * Translator object for escaping HTML version 4.0.
     *
     * While [encodeHtml4] is the expected method of use, this
     * object allows the HTML escaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val ENCODE_HTML4: StringTranslator = AggregateTranslator(
        LookupTranslator(EntityMaps.HTML4Encode),
    )

    /**
     * Translator object for escaping HTML version 4.0.
     *
     * While [encodeHtml4] is the expected method of use, this
     * object allows the HTML escaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val ENCODE_HTML5: StringTranslator = AggregateTranslator(
        LookupTranslator(EntityMaps.HTML5Encode),
        NumericEntityEncoder.between(0x1, 0x8),
        NumericEntityEncoder.between(0xe, 0x1f),
        NumericEntityEncoder.between(0x7f, 0x84),
        NumericEntityEncoder.between(0x86, 0x9f),
    )

    /**
     * Translator object for unescaping encoded HTML 4.0.
     *
     * While [decodeHtml4] is the expected method of use, this
     * object allows the HTML unescaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val DECODE_HTML4: StringTranslator = AggregateTranslator(
        LookupTranslator(EntityMaps.HTML4Decode),
        NumericEntityDecoder()
    )

    /**
     * Translator object for unescaping decoded HTML 5.0.
     *
     * While [decodeHtml5] is the expected method of use, this
     * object allows the HTML unescaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val DECODE_HTML5: StringTranslator = AggregateTranslator(
        LookupTranslator(EntityMaps.HTML5Decode),
        NumericEntityDecoder(),
    )

    /**
     * Translator object for unescaping encoded XML.
     *
     * While [decodeXml] is the expected method of use, this
     * object allows the XML unescaping functionality to be used
     * as the foundation for a custom translator.
     */
    private val DECODE_XML: StringTranslator = AggregateTranslator(
        LookupTranslator(EntityMaps.XMLDecode),
        NumericEntityDecoder()
    )

    /**
     * Get a [Builder].
     * @param translator the text translator
     * @return [Builder]
     */
    private fun builder(translator: StringTranslator): Builder {
        return Builder(translator)
    }

    // HTML and XML
    /**
     * Encodes the characters in a `String` using HTML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" &amp; "butter"`
     *      // result
     *      `&quot;bread&quot; &amp;amp; &quot;butter&quot;`.
     *
     *
     * Supports all known HTML 4.0 entities, including funky accents.
     * Note that the commonly used apostrophe encode character (&amp;apos;)
     * is not a legal entity and so is not supported. If you want to support this encode character use [encodeHtml5]
     *
     * @param input  the `String` to encode, may be null
     * @return a new encoded `String`, `null` if null string input
     *
     * @see <a href="https://www.hotwired.lycos.com/webmonkey/reference/special_characters">ISO Entities</a>
     *
     * @see <a href="https://www.hotwired.lycos.com/webmonkey/reference/special_characters">HTML 3.2 Character Entities for ISO Latin-1</a>
     *
     * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity references</a>
     *
     * @see <a href="http://www.w3.org/TR/html401/charset.html.h-5.3">HTML 4.01 Character References</a>
     *
     * @see <a href="http://www.w3.org/TR/html401/charset.html.code-position">HTML 4.01 Code positions</a>
     *
     */
    public fun encodeHtml4(input: String): String {
        return ENCODE_HTML4.translate(input)
    }

    /**
     * Encodes the characters in a `String` using HTML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" &amp; "butter"`
     *      // result
     *      `&quot;bread&quot; &amp;amp; &quot;butter&quot;`.
     *
     * Supports all known HTML 4.0 entities, including funky accents.
     * Support for the commonly used apostrophe encode character (&amp;apos;)
     *
     * @param input  the `String` to encode
     * @return a new encoded `String`
     */
    public fun encodeHtml5(input: String): String {
        return ENCODE_HTML5.translate(input)
    }

    /**
     * Encodes the characters in a `String` using HTML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" &amp; "butter"`
     *      // result
     *      `&quot;bread&quot; &amp;amp; &quot;butter&quot;`.
     *
     * Supports all known HTML 4.0 entities, including funky accents.
     * Support that the commonly used apostrophe encode character (&amp;apos;)
     *
     * @param input String that is being translated
     * @param offset Int representing the current point of translation
     * @param stringBuilder StringBuilder to translate the text to
     * @return int count of code points consumed
     */
    public fun encodeHtml5(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        return ENCODE_HTML5.translate(
            input = input,
            offset = offset,
            stringBuilder = stringBuilder
        )
    }

    /**
     * Encodes the characters in a `String` using HTML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" &amp; "butter"`
     *      // result
     *      `&quot;bread&quot; &amp;amp; &quot;butter&quot;`.
     *
     * Supports all known HTML 4.0 entities, including funky accents.
     * Support for the commonly used apostrophe encode character (&amp;apos;)
     *
     * @param input  the `String` to encode
     * @return a new encoded `String`
     */
    public fun encodeHtml(input: String): String {
        return encodeHtml5(input)
    }

    /**
     * Encodes the characters in a `String` using HTML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" &amp; "butter"`
     *      // result
     *      `&quot;bread&quot; &amp;amp; &quot;butter&quot;`.
     *
     * Supports all known HTML 4.0 entities, including funky accents.
     * Support for the commonly used apostrophe encode character (&amp;apos;)
     *
     * @param input String that is being translated
     * @param offset Int representing the current point of translation
     * @param stringBuilder StringBuilder to translate the text to
     * @return int count of code points consumed
     */
    public fun encodeHtml(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        return encodeHtml5(
            input = input,
            offset = offset,
            stringBuilder = stringBuilder
        )
    }

    /**
     * Encodes the characters in a `String` using XML entities.
     *
     * For example:
     *
     *      // input
     *      `"bread" & "butter"` =&gt;
     *      // result
     *      `&quot;bread&quot; &amp; &quot;butter&quot;`.
     *
     * XML 1.1 can represent certain control characters, but it cannot represent
     * the null byte or unpaired Unicode surrogate code points, even after escaping.
     *
     * The returned string can be inserted into a valid XML 1.1 document. Do not
     * use it for XML 1.0 documents.
     *
     * @param input  the `String` to encode
     * @return a new encoded `String`
     * @see [decodeXml]
     */
    public fun encodeXml(input: String): String {
        return ENCODE_XML.translate(input)
    }

    /**
     * Decodes a string containing entity encodes to a string
     * containing the actual Unicode characters corresponding to the
     * encoding. Supports HTML 4.0 entities.
     *
     * Example:
     *
     *     // input
     *     `"&lt;Fran&ccedil;ais&gt;"`
     *     // output
     *     `"<Fran�ais>"`
     *
     * If an entity is unrecognized, it is left alone, and inserted
     * verbatim into the result string.
     *
     * Example:
     *
     *      // input
     *      `"&gt;&zzzz;x"`
     *      // output
     *      `">&zzzz;x"`.
     *
     * @param input  the `String` to decode
     * @return a new decoded `String`
     * @see [encodeHtml4]
     */
    public fun decodeHtml4(input: String): String {
        return DECODE_HTML4.translate(input)
    }

    /**
     * Decodes a string containing entity encodes to a string
     * containing the actual Unicode characters corresponding to the
     * encoding.
     * Supports HTML 4.0 entities.
     *
     * Example:
     *
     *     // input
     *     `"&lt;Fran&ccedil;ais&gt;"`
     *     // output
     *     `"<Fran�ais>"`
     *
     * If an entity is unrecognized, it is left alone, and inserted
     * verbatim into the result string.
     *
     * Example:
     *
     *     // input
     *     `"&gt;&zzzz;x"` will
     *     // output
     *     `">&zzzz;x"`.
     *
     * @param input  the `String` to decode, may be null
     * @return a new decoded `String`, `null` if null string input
     */
    public fun decodeHtml5(input: String): String {
        return DECODE_HTML5.translate(input)
    }

    /**
     * Decodes a string containing entity encodes to a string
     * containing the actual Unicode characters corresponding to the
     * encoding.
     * Supports HTML 4.0 entities.
     *
     * Example:
     *
     *     // input
     *     `"&lt;Fran&ccedil;ais&gt;"`
     *     // output
     *     `"<Fran�ais>"`
     *
     * If an entity is unrecognized, it is left alone, and inserted
     * verbatim into the result string.
     *
     * Example:
     *
     *     // input
     *     `"&gt;&zzzz;x"` will
     *     // output
     *     `">&zzzz;x"`.
     *
     * @param input  the `String` to decode, may be null
     * @return a new decoded `String`, `null` if null string input
     */
    public fun decodeHtml(input: String): String {
        return decodeHtml5(input)
    }

    /**
     * Decodes a string containing XML entity encodes to a string
     * containing the actual Unicode characters corresponding to the
     * encoding.
     *
     *
     * Supports only the five basic XML entities (gt, lt, quot, amp, apos).
     * Does not support DTDs or external entities.
     *
     *
     * Note that numerical \\u Unicode codes are decoded to their respective
     * Unicode characters. This may change in future releases.
     *
     * @param input the `String` to decode
     * @return a new decoded `String`
     * @see [encodeXml]
     */
    public fun decodeXml(input: String): String {
        return DECODE_XML.translate(input)
    }

    /**
     * Convenience wrapper for [StringBuilder] providing encode methods.
     *
     *
     * Example:
     *
     *  ```kotlin
     *  Builder(ENCODE_HTML4)
     *      .append("&lt;p&gt;")
     *      .encode("This is paragraph 1 and special chars like &amp; get encoded.")
     *      .append("&lt;/p&gt;&lt;p&gt;")
     *      .encode("This is paragraph 2 &amp; more...")
     *      .append("&lt;/p&gt;")
     *      .toString()
     *  ```
     *
     *
     */
    private class Builder internal constructor(
        private val translator: StringTranslator
    ) {
        /**
         * StringBuilder to be used in the Builder class.
         */
        private val sb: StringBuilder = StringBuilder()

        /**
         * Literal append, no escaping being done.
         *
         * @param input the String to append
         * @return `this`, to enable chaining
         */
        public fun append(input: String): Builder {
            sb.append(input)
            return this
        }

        /**
         * Encodes `input` according to the given [StringTranslator].
         *
         * @param input the String to encode
         * @return `this`, to enable chaining
         */
        public fun encode(input: String): Builder {
            sb.append(translator.translate(input))
            return this
        }

        /**
         * Return the encoded string.
         *
         * @return The encoded string
         */
        override fun toString(): String {
            return sb.toString()
        }
    }

}