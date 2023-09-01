package com.mohamedrejeb.ksoup.entities.text.translate

import com.mohamedrejeb.ksoup.entities.StringTranslator
import com.mohamedrejeb.ksoup.entities.utils.CharsUtils


/**
 * Translates XML numeric entities of the form &amp;#\[xX]?\d+;? to
 * the specific code point.
 *
 * Note that the semicolon is optional.
 *
 * Creates a UnicodeDecoder.
 *
 * The constructor takes a list of options, only one type of which is currently
 * available (whether to allow, error or ignore the semicolon on the end of a
 * numeric entity to being missing).
 *
 * For example, to support numeric entities without a ';':
 * new NumericEntityDecoder(NumericEntityDecoder.OPTION.semiColonOptional)
 * and to throw an IllegalArgumentException when they're missing:
 * new NumericEntityDecoder(NumericEntityDecoder.OPTION.errorIfNoSemiColon)
 *
 * Note that the default behavior is to ignore them.
 *
 * @param options to apply to this decoder
 */
internal class NumericEntityDecoder(vararg options: Option) : StringTranslator() {
    /** Enumerates NumericEntityDecoder options for unescaping.  */
    enum class Option {
        /**
         * Requires a semicolon.
         */
        SemiColonRequired,

        /**
         * Does not require a semicolon.
         */
        SemiColonOptional,

        /**
         * Throws an exception if a semicolon is missing.
         */
        ErrorIfNoSemiColon
    }

    /** EnumSet of OPTIONS, given from the constructor, read-only.  */
    private val options: Set<Option> = if (options.isEmpty()) DEFAULT_OPTIONS else setOf(*options)

    /**
     * Tests whether the passed in option is currently set.
     *
     * @param option to check state of
     * @return whether the option is set
     */
    private fun isSet(option: Option): Boolean {
        return options.contains(option)
    }

    override fun translate(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        val seqEnd = input.length
        // Uses -2 to ensure there is something after the &#
        if (input[offset] == '&' && offset < seqEnd - 2 && input[offset + 1] == '#') {
            var start = offset + 2
            var isHex = false
            val firstChar = input[start]

            if (firstChar == 'x' || firstChar == 'X') {
                start++
                isHex = true

                // Check there's more than just an x after the &#
                if (start == seqEnd) {
                    return 0
                }
            }
            var end = start
            // Note that this supports character codes without a ; on the end
            while (
                end < seqEnd && (
                    input[end] in '0'..'9'
                    || input[end] in 'a'..'f'
                    || input[end] in 'A'..'F'
                )
            ) {
                end++
            }

            val semiNext = end != seqEnd && input[end] == ';'

            if (!semiNext) {
                if (isSet(Option.SemiColonRequired)) {
                    return 0
                }
                if (isSet(Option.ErrorIfNoSemiColon)) {
                    throw IllegalArgumentException("Semi-colon required at end of numeric entity")
                }
            }
            val entityValue: Int = try {
                if (isHex) {
                    input.subSequence(start, end).toString().toInt(16)
                } else {
                    input.subSequence(start, end).toString().toInt(10)
                }
            } catch (e: NumberFormatException) {
                return 0
            }

            if (entityValue > 0xFFFF) {
                CharsUtils
                    .toChars(entityValue)
                    .forEach { stringBuilder.append(it) }
            } else {
                stringBuilder.append(Char(entityValue))
            }
            return 2 + end - start + (if (isHex) 1 else 0) + if (semiNext) 1 else 0
        }
        return 0
    }

    companion object {
        /** Default options.  */
        private val DEFAULT_OPTIONS: Set<Option> = setOf(
            Option.SemiColonRequired
        )


    }
}