package com.mohamedrejeb.ksoup.entities.text.translate


/**
 * Helper subclass to StringTranslator to remove unpaired surrogates.
 */
internal class UnicodeUnpairedSurrogateRemover : CodePointTranslator() {
    override fun translate(codePoint: Int, stringBuilder: StringBuilder): Boolean {
        // If true, it is a surrogate. Write nothing and say we've translated. Otherwise return false, and don't translate it.
        return codePoint >= Char.MIN_SURROGATE.code && codePoint <= Char.MAX_SURROGATE.code
    }
}

