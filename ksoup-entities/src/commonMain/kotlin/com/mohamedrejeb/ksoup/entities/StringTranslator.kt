package com.mohamedrejeb.ksoup.entities

/**
 * An API for translating text.
 * Its core use is to escape and unescape text. Because escaping and unescaping
 * is completely contextual, the API does not present two separate signatures.
 */
internal abstract class StringTranslator {
    /**
     * Helper for non-StringBuilder usage.
     * @param input String to be translated
     * @return String output of translation
     */
    fun translate(input: String): String {
        val stringBuilder = StringBuilder(input.length * 2)
        translate(input, stringBuilder)
        return stringBuilder.toString()
    }

    /**
     * Translate a set of code points, represented by an int index into a String,
     * into another set of code points. The number of code points consumed must be returned.
     *
     * @param input String that is being translated
     * @param offset Int representing the current point of translation
     * @param stringBuilder StringBuilder to translate the text to
     * @return int count of code points consumed
     */
    abstract fun translate(input: String, offset: Int, stringBuilder: StringBuilder): Int

    /**
     * Translate an input onto a StringBuilder. This is intentionally final as its algorithm is
     * tightly coupled with the abstract method of this class.
     *
     * @param input String that is being translated
     * @param stringBuilder StringBuilder to translate the text to
     */
    private fun translate(input: String, stringBuilder: StringBuilder) {
        var pos = 0
        val len = input.length
        while (pos < len) {
            val consumed = translate(input, pos, stringBuilder)
            if (consumed == 0) {
                // inlined implementation of Character.toChars(Character.codePointAt(input, pos))
                // avoids allocating temp char arrays and duplicate checks
                val c1 = input[pos]
                stringBuilder.append(c1)
                pos++
                if (c1.isHighSurrogate() && pos < len) {
                    val c2 = input[pos]
                    if (c2.isLowSurrogate()) {
                        stringBuilder.append(c2)
                        pos++
                    }
                }
                continue
            }
            // contract with translators is that they have to understand code points,
            // and they just took care of a surrogate pair
            for (pt in 0 until consumed) {
                pos++
            }
        }
    }

    /**
     * Helper method to create a merger of this translator with another set of
     * translators. Useful in customizing the standard functionality.
     *
     * @param translators StringTranslator array of translators to merge with this one
     * @return StringTranslator merging this translator with the others
     */
    public fun with(vararg translators: StringTranslator): StringTranslator {
        val newArray = arrayOf(this, *translators)
        return AggregateTranslator(*newArray)
    }
}