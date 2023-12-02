package com.mohamedrejeb.ksoup.entities.text.translate

import com.mohamedrejeb.ksoup.entities.StringTranslator

/**
 * Helper subclass to StringTranslator to allow for translations that
 * will replace up to one character at a time.
 */
internal abstract class CodePointTranslator : StringTranslator() {
    override fun translate(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        val codePoint: Int = input[offset].code
        val consumed = translate(codePoint, stringBuilder)
        return if (consumed) 1 else 0
    }

    /**
     * Translates the specified code point into another.
     *
     * @param codePoint Int character input to translate
     * @param stringBuilder StringBuilder to optionally push the translated output to
     * @return boolean as to whether translation occurred or not
     */
    abstract fun translate(codePoint: Int, stringBuilder: StringBuilder): Boolean
}