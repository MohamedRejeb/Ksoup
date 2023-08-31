package com.mohamedrejeb.ksoup.entities

/**
 * Translates a value using a lookup table.
 *
 * @param lookupMap Map<String, String> table of translator
 * mappings
 */
internal class LookupTranslator(lookupMap: List<Pair<String, String>>) : StringTranslator() {
    /** The mapping to be used in translation.  */
    private val lookupMap: MutableMap<String, String> = mutableMapOf()

    /** The first character of each key in the lookupMap.  */
    private val prefixSet: MutableSet<UShort> = mutableSetOf()

    /** The length of the shortest key in the lookupMap.  */
    private val shortest: Int

    /** The length of the longest key in the lookupMap.  */
    private val longest: Int

    init {
        var currentShortest = Int.MAX_VALUE
        var currentLongest = 0
        for ((key, value) in lookupMap) {
            this.lookupMap[key] = value
            prefixSet.add(key[0].code.toUShort())
            val sz = key.length
            if (sz < currentShortest) {
                currentShortest = sz
            }
            if (sz > currentLongest) {
                currentLongest = sz
            }
        }
        shortest = currentShortest
        longest = currentLongest
    }

    override fun translate(input: String, offset: Int, stringBuilder: StringBuilder): Int {
        // check if translation exists for the input at position offset
        if (prefixSet.contains(input[offset].code.toUShort())) {
            var max = longest
            if (offset + longest > input.length) {
                max = input.length - offset
            }
            // implement greedy algorithm by trying maximum match first
            for (i in max downTo shortest) {
                val subSeq = input.subSequence(offset, offset + i)
                val result = lookupMap[subSeq.toString()]
                if (result != null) {
                    stringBuilder.append(result)
                    return subSeq.length // return the length of the matching subSeq
                }
            }
        }
        return 0
    }
}