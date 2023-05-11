package com.mohamedrejeb.ksoup.entities


/**
 * Executes a sequence of translators one after the other. Execution ends whenever
 * the first translator consumes code points from the input.
 */
internal class AggregateTranslator() : StringTranslator() {
    /**
     * Translator list.
     */
    private val translators: MutableList<StringTranslator> = mutableListOf()

    /**
     * Specify the translators to be used at creation time.
     *
     * @param translators StringTranslator array to aggregate
     */
    constructor(vararg translators: StringTranslator) : this() {
        this.translators.addAll(translators)
    }

    /**
     * The first translator to consume code points from the input is the 'winner'.
     * Execution stops with the number of consumed code points being returned.
     *
     * @param input String that is being translated
     * @param index int representing the current point of translation
     * @param stringBuilder StringBuilder to translate the text to
     */
    override fun translate(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        for (translator in translators) {
            val consumed = translator.translate(input, offset, stringBuilder)
            if (consumed != 0) {
                return consumed
            }
        }
        return 0
    }
}