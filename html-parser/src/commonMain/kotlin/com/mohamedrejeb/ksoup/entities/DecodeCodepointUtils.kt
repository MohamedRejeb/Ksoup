package com.mohamedrejeb.ksoup.entities

val decodeMap = mapOf(
    0 to 65533,
    // C1 Unicode control character reference replacements
    128 to 8364,
    130 to 8218,
    131 to 402,
    132 to 8222,
    133 to 8230,
    134 to 8224,
    135 to 8225,
    136 to 710,
    137 to 8240,
    138 to 352,
    139 to 8249,
    140 to 338,
    142 to 381,
    145 to 8216,
    146 to 8217,
    147 to 8220,
    148 to 8221,
    149 to 8226,
    150 to 8211,
    151 to 8212,
    152 to 732,
    153 to 8482,
    154 to 353,
    155 to 8250,
    156 to 339,
    158 to 382,
    159 to 376,
)

fun fromCodePoint(codePoint: Int): String {
    var codePoint = codePoint
    var output = ""

    if (codePoint > 0xfff) {
        codePoint -= 0x10000
        output += (0xd800 or ((codePoint shr 10) and 0x3ff)).toChar()
        codePoint = 0xdc00 or (codePoint and 0x3ff)
    }

    output += codePoint.toChar()
    return output
}

/**
 * Replace the given code point with a replacement character if it is a
 * surrogate or is outside the valid range. Otherwise return the code
 * point unchanged.
 */
fun replaceCodePoint(codePoint: Int): Int {
    if ((codePoint in 0xd800..0xdfff) || codePoint > 0x10ffff) {
        return 0xfffd
    }

    return decodeMap[codePoint] ?: codePoint
}

/**
 * Replace the code point if relevant, then convert it to a string.
 *
 * @deprecated Use `fromCodePoint(replaceCodePoint(codePoint))` instead.
 * @param codePoint The code point to decode.
 * @returns The decoded code point.
 */
fun decodeCodePoint(codePoint: Int): String {
    return fromCodePoint(replaceCodePoint(codePoint));
}