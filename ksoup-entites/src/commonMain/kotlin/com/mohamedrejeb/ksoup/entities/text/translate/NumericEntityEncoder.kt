package com.mohamedrejeb.ksoup.entities.text.translate

/**
 * Translates code points to their XML numeric entity encoded value.
 *
 * Constructs a `NumericEntityEncoder` for the specified range. This is
 * the underlying method for the other constructors/builders. The `below`
 * and `above` boundaries are inclusive when `between` is
 * `true` and exclusive when it is `false`.
 *
 * @param range IntRange from lowest code point to highest code point
 * @param between whether to encode between the boundaries or outside them
 */
internal class NumericEntityEncoder private constructor(
    private val range: IntRange,
    private val between: Boolean
) : CodePointTranslator() {

    /**
     * Constructs a `NumericEntityEncoder` for all characters.
     */
    constructor() : this(0..Int.MAX_VALUE, true)

    override fun translate(codePoint: Int, stringBuilder: StringBuilder): Boolean {
        if (between != range.contains(codePoint)) {
            return false
        }

        stringBuilder.append("&#")
        stringBuilder.append(codePoint.toString(10))
        stringBuilder.append(';')
        return true
    }

    companion object {
        /**
         * Constructs a `NumericEntityEncoder` above the specified value (exclusive).
         *
         * @param codePoint above which to encode
         * @return The newly created `NumericEntityEncoder` instance
         */
        fun above(codePoint: Int): NumericEntityEncoder {
            return outsideOf(0, codePoint)
        }

        /**
         * Constructs a `NumericEntityEncoder` below the specified value (exclusive).
         *
         * @param codePoint below which to encode
         * @return The newly created `NumericEntityEncoder` instance
         */
        fun below(codePoint: Int): NumericEntityEncoder {
            return outsideOf(codePoint, Int.MAX_VALUE)
        }

        /**
         * Constructs a `NumericEntityEncoder` between the specified values (inclusive).
         *
         * @param codePointLow above which to encode
         * @param codePointHigh below which to encode
         * @return The newly created `NumericEntityEncoder` instance
         */
        fun between(codePointLow: Int, codePointHigh: Int): NumericEntityEncoder {
            return NumericEntityEncoder(codePointLow..codePointHigh, true)
        }

        /**
         * Constructs a `NumericEntityEncoder` outside of the specified values (exclusive).
         *
         * @param codePointLow below which to encode
         * @param codePointHigh above which to encode
         * @return The newly created `NumericEntityEncoder` instance
         */
        fun outsideOf(codePointLow: Int, codePointHigh: Int): NumericEntityEncoder {
            return NumericEntityEncoder(codePointLow..codePointHigh, false)
        }
    }
}