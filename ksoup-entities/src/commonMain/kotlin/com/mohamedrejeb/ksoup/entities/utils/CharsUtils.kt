package com.mohamedrejeb.ksoup.entities.utils

internal object CharsUtils {

    /**
     * Converts the specified character (Unicode code point) to its
     * UTF-16 representation stored in a char array.
     * If
     * the specified code point is a BMP (Basic Multilingual Plane or
     * Plane 0) value, the resulting char array has
     * the same value as [codePoint].
     * If the specified code
     * point is a supplementary code point, the resulting
     * char array has the corresponding surrogate pair.
     *
     * @param  codePoint a Unicode code point
     * @return a char array having
     *         codePoint's UTF-16 representation.
     * @throws IllegalArgumentException if the specified
     * {@code codePoint} is not a valid Unicode code point.
     */
    fun toChars(codePoint: Int): CharArray {
        return if (isBmpCodePoint(codePoint)) {
            charArrayOf(codePoint.toChar())
        } else if (isValidCodePoint(codePoint)) {
            val result = CharArray(2)
            toSurrogates(codePoint, result, 0)
            result
        } else {
            throw IllegalArgumentException()
        }
    }

    /**
     * Determines whether the specified character (Unicode code point)
     * is in the <a href="#BMP">Basic Multilingual Plane (BMP)</a>.
     * Such code points can be represented using a single {@code char}.
     *
     * @param  codePoint the character (Unicode code point) to be tested
     * @return {@code true} if the specified code point is between
     *         {@link #MIN_VALUE} and {@link #MAX_VALUE} inclusive;
     *         {@code false} otherwise.
     */
    private fun isBmpCodePoint(codePoint: Int): Boolean {
        return codePoint ushr 16 == 0
        // Optimized form of:
        //     codePoint >= MIN_VALUE && codePoint <= MAX_VALUE
        // We consistently use logical shift (>>>) to facilitate
        // additional runtime optimizations.
    }

    /**
     * Determines whether the specified code point is a valid
     * [
 * Unicode code point value](http://www.unicode.org/glossary/#code_point).
     *
     * @param  codePoint the Unicode code point to be tested
     * @return `true` if the specified code point value is between
     * [Char.MIN_VALUE] and
     * [Char.MAX_VALUE] inclusive;
     * `false` otherwise.
     */
    private fun isValidCodePoint(codePoint: Int): Boolean {
        // Optimized form of:
        //     codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT
        val plane = codePoint ushr 16
        return plane < ((MAX_CODE_POINT + 1) ushr 16)
    }

    private fun toSurrogates(codePoint: Int, dst: CharArray, index: Int) {
        // We write elements "backwards" to guarantee all-or-nothing
        dst[index + 1] = lowSurrogate(codePoint)
        dst[index] = highSurrogate(codePoint)
    }

    /**
     * Returns the leading surrogate (a
     * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">high surrogate code unit</a> of the
     * <a href="http://www.unicode.org/glossary/#surrogate_pair">surrogate</a>
     * representing the specified supplementary character (Unicode
     * code point) in the UTF-16 encoding.  If the specified character
     * is not a supplementary character,
     * an unspecified `char` is returned.
     *
     *
     * If
     * [isSupplementaryCodePoint(x)][isSupplementaryCodePoint]
     * is `true`, then
     * [isHighSurrogate][.isHighSurrogate]`(highSurrogate(x))` and
     * [toCodePoint][.toCodePoint]`(highSurrogate(x), `[lowSurrogate][.lowSurrogate]`(x)) == x`
     * are also always `true`.
     *
     * @param   codePoint a supplementary character (Unicode code point)
     * @return  the leading surrogate code unit used to represent the
     * character in the UTF-16 encoding
     */
    private fun highSurrogate(codePoint: Int): Char {
        return ((codePoint ushr 10)
                + (Char.MIN_HIGH_SURROGATE.code - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10))).toChar()
    }

    /**
     * Returns the trailing surrogate (a
     * [
 * low surrogate code unit](http://www.unicode.org/glossary/#low_surrogate_code_unit)) of the
     * [
 * surrogate pair](http://www.unicode.org/glossary/#surrogate_pair)
     * representing the specified supplementary character (Unicode
     * code point) in the UTF-16 encoding.  If the specified character
     * is not a
     * [supplementary character](Character.html#supplementary),
     * an unspecified `char` is returned.
     *
     *
     * If
     * [isSupplementaryCodePoint(x)][.isSupplementaryCodePoint]
     * is `true`, then
     * [isLowSurrogate][.isLowSurrogate]`(lowSurrogate(x))` and
     * [toCodePoint][.toCodePoint]`(`[highSurrogate][.highSurrogate]`(x), lowSurrogate(x)) == x`
     * are also always `true`.
     *
     * @param   codePoint a supplementary character (Unicode code point)
     * @return  the trailing surrogate code unit used to represent the
     * character in the UTF-16 encoding
     */
    private fun lowSurrogate(codePoint: Int): Char {
        return ((codePoint and 0x3ff) + Char.MIN_LOW_SURROGATE.code).toChar()
    }

    /**
     * The minimum value of a
     * [Unicode supplementary code point](http://www.unicode.org/glossary/#supplementary_code_point), constant `U+10000`.
     */
    private const val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000

    /**
     * The maximum value of a
     * <a href="http://www.unicode.org/glossary/#code_point">
     * Unicode code point</a>, constant {@code U+10FFFF}.
     */
    private const val MAX_CODE_POINT = 0X10FFFF

}