package com.mohamedrejeb.ksoup

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import kotlin.test.Test
import kotlin.test.assertEquals

class KsoupEntitiesTest {

    fun testDecodeHtml5(input: String, expectedOutput: String) {
        val decoded = KsoupEntities.decodeHtml5(input)
        assertEquals(
            expectedOutput,
            decoded
        )
    }

    fun testEncodeHtml5(input: String, expectedOutput: String) {
        val encoded = KsoupEntities.encodeHtml5(input)
        assertEquals(
            expectedOutput,
            encoded
        )
    }

    // Decode Test
    @Test
    fun testDecodeHtml5_1() {
        testDecodeHtml5("<p>asdf &amp; &yuml; &uuml; &apos;</p>", "<p>asdf & ÿ ü '</p>")
    }

    @Test
    fun testDecodeHtml5_2() {
        testDecodeHtml5("Hello &lt;World&gt;!", "Hello <World>!")
    }

    @Test
    fun testDecodeHtml5_3() {
        testDecodeHtml5("The price is &#36;10.", "The price is $10.")
    }

    @Test
    fun testDecodeHtml5_4() {
        testDecodeHtml5("&lt;img src=&quot;image.jpg&quot;&gt;", "<img src=\"image.jpg\">")
    }

    @Test
    fun testDecodeHtml5_5() {
        testDecodeHtml5("&nbsp;&nbsp;&nbsp;", "   ")
    }

    @Test
    fun testDecodeHtml5_6() {
        testDecodeHtml5("&Aring;", "Å")
    }

    @Test 
    fun pruneBasicLatin() {
        testDecodeHtml5("&#42;", "*")
    }

    @Test 
    fun pruneLatin1Supplement() {
        testDecodeHtml5("&#174;", "®")
    }

    @Test 
    fun pruneLatinExtendedA() {
        testDecodeHtml5("&#288;", "Ġ")
    }

    @Test 
    fun pruneLatinExtendedB() {
        testDecodeHtml5("&#557;", "ȭ")
    }

    @Test 
    fun pruneIPAExtensions() {
        testDecodeHtml5("&#608;", "ɠ")
    }

    @Test 
    fun pruneSpacingModifierLetters() {
        testDecodeHtml5("&#743;", "˧")
    }

    @Test 
    fun pruneCombiningDiacriticalMarks() {
        assertEquals(expected = """͠""", actual = KsoupEntities.decodeHtml("&#864;"))
    }

    @Test 
    fun pruneGreek() {
        assertEquals(expected = "Ϯ", actual = KsoupEntities.decodeHtml("&#1006;"))
    }

    @Test 
    fun pruneCyrillic() {
        assertEquals(expected = "Ц", actual = KsoupEntities.decodeHtml("&#1062;"))
    }

    @Test 
    fun pruneHebrew() {
        assertEquals(expected = """ױ""", actual = KsoupEntities.decodeHtml("&#1521;"))
    }

    @Test 
    fun pruneArabic() {
        assertEquals(expected = "ب", actual = KsoupEntities.decodeHtml("&#1576;"))
    }

    @Test 
    fun pruneSyriac() {
        assertEquals(expected = """܈""", actual = KsoupEntities.decodeHtml("&#1800;"))
    }

    @Test 
    fun pruneThaana() {
        assertEquals(expected = """ޖ""", actual = KsoupEntities.decodeHtml("&#1942;"))
    }

    @Test
    fun pruneDevanagari() {
        assertEquals(expected = """औ""", actual = KsoupEntities.decodeHtml("&#2324;"))
    }

    @Test 
    fun pruneBengali() {
        assertEquals(expected = """৺""", actual = KsoupEntities.decodeHtml("&#2554;"))
    }

    @Test 
    fun pruneGurmukhi() {
        assertEquals(expected = """ਆ""", actual = KsoupEntities.decodeHtml("&#2566;"))
    }

    @Test 
    fun pruneGujarati() {
        assertEquals(expected = """ઈ""", actual = KsoupEntities.decodeHtml("&#2696;"))
    }

    @Test 
    fun pruneOriya() {
        assertEquals(expected = """୯""", actual = KsoupEntities.decodeHtml("&#2927;"))
    }

    @Test 
    fun pruneTamil() {
        assertEquals(expected = """௫""", actual = KsoupEntities.decodeHtml("&#3051;"))
    }

    @Test 
    fun pruneTelugu() {
        assertEquals(expected = """౭""", actual = KsoupEntities.decodeHtml("&#3181;"))
    }

    @Test 
    fun pruneKannada() {
        assertEquals(expected = """೯""", actual = KsoupEntities.decodeHtml("&#3311;"))
    }

    @Test 
    fun pruneMalayalam() {
        assertEquals(expected = """ഗ""", actual = KsoupEntities.decodeHtml("&#3351;"))
    }

    @Test 
    fun pruneSinhala() {
        assertEquals(expected = """ඊ""", actual = KsoupEntities.decodeHtml("&#3466;"))
    }

    @Test 
    fun pruneThai() {
        assertEquals(expected = """ค""", actual = KsoupEntities.decodeHtml("&#3588;"))
    }

    @Test 
    fun pruneLao() {
        assertEquals(expected = """ຖ""", actual = KsoupEntities.decodeHtml("&#3734;"))
    }

    @Test 
    fun pruneTibetan() {
        assertEquals(expected = """࿏""", actual = KsoupEntities.decodeHtml("&#4047;"))
    }

    @Test 
    fun pruneMyanmar() {
        assertEquals(expected = """ည""", actual = KsoupEntities.decodeHtml("&#4106;"))
    }

    @Test 
    fun pruneGeorgian() {
        assertEquals(expected = """Ⴂ""", actual = KsoupEntities.decodeHtml("&#4258;"))
    }

    @Test 
    fun pruneLatinExtendedAdditional() {
        assertEquals(expected = "Ỹ", actual = KsoupEntities.decodeHtml("&#7928;"))
    }

    @Test 
    fun pruneGreekExtended() {
        assertEquals(expected = "ἁ", actual = KsoupEntities.decodeHtml("&#7937;"))
    }

    @Test 
    fun pruneGeneralPunctuation() {
        assertEquals(expected = "‒", actual = KsoupEntities.decodeHtml("&#8210;"))
    }

    @Test 
    fun pruneSuperscriptsAndSubscripts() {
        assertEquals(expected = "⁾", actual = KsoupEntities.decodeHtml("&#8318;"))
    }

    @Test 
    fun pruneCurrencySymbols() {
        assertEquals(expected = "₦", actual = KsoupEntities.decodeHtml("&#8358;"))
    }

    @Test 
    fun pruneCombiningMarksForSymbols() {
        assertEquals(expected = """⃝""", actual = KsoupEntities.decodeHtml("&#8413;"))
    }

    @Test 
    fun pruneLetterLikeSymbols() {
        assertEquals(expected = "℈", actual = KsoupEntities.decodeHtml("&#8456;"))
    }

    @Test 
    fun pruneNumberForms() {
        assertEquals(expected = "⅛", actual = KsoupEntities.decodeHtml("&#8539;"))
    }

    @Test 
    fun pruneArrows() {
        assertEquals(expected = "↘", actual = KsoupEntities.decodeHtml("&#8600;"))
    }

    @Test 
    fun pruneMathematicalOperators() {
        assertEquals(expected = "∂", actual = KsoupEntities.decodeHtml("&#8706;"))
    }

    @Test 
    fun pruneMiscellaneousTechnical() {
        assertEquals(expected = """⌘""", actual = KsoupEntities.decodeHtml("&#8984;"))
    }

    @Test 
    fun pruneEnclosedAlphanumerics() {
        assertEquals(expected = "⑩", actual = KsoupEntities.decodeHtml("&#9321;"))
    }

    @Test 
    fun pruneGeometricShapes() {
        assertEquals(expected = "◂", actual = KsoupEntities.decodeHtml("&#9666;"))
    }

    @Test 
    fun pruneMiscellaneousSymbols() {
        assertEquals(expected = "☇", actual = KsoupEntities.decodeHtml("&#9735;"))
    }

    @Test 
    fun pruneDingbats() {
        assertEquals(expected = "➼", actual = KsoupEntities.decodeHtml("&#10172;"))
    }

    @Test 
    fun pruneCjkRadicalsSupplement() {
        assertEquals(expected = """⺉""", actual = KsoupEntities.decodeHtml("&#11913;"))
    }

    @Test 
    fun pruneKangxiRadicals() {
        assertEquals(expected = "⼉", actual = KsoupEntities.decodeHtml("&#12041;"))
    }

    @Test 
    fun pruneCjkSymbolsAndPunctuation() {
        assertEquals(expected = "〆", actual = KsoupEntities.decodeHtml("&#12294;"))
    }

    @Test 
    fun pruneHiragana() {
        assertEquals(expected = """ゔ""", actual = KsoupEntities.decodeHtml("&#12436;"))
    }

    @Test 
    fun pruneKatakana() {
        assertEquals(expected = "オ", actual = KsoupEntities.decodeHtml("&#12458;"))
    }

    @Test 
    fun pruneHalfWidthAndFullWidthForms() {
        assertEquals(expected = """￫""", actual = KsoupEntities.decodeHtml("&#65515;"))
    }

    @Test 
    fun pruneSomeKindOfChinese() {
        assertEquals(expected = """不""", actual = KsoupEntities.decodeHtml("&#19981;"))
    }

    @Test
    fun pruneSomeKindOfAlphabet() {
        assertEquals(expected = """𝟸""", actual = KsoupEntities.decodeHtml("&#120824;"))
    }

    @Test
    fun pruneRegionalIndicatorSymbolLetter() {
        assertEquals(expected = """🇮""", actual = KsoupEntities.decodeHtml("&#127470;"))
    }

    @Test
    fun pruneEmojis() {
        assertEquals(expected = """🌍""", actual = KsoupEntities.decodeHtml("&#127757;"))
    }

    @Test
    fun mountainEmoji() {
        assertEquals(expected = """🗻""", actual = KsoupEntities.decodeHtml("&#128507;"))
    }

    @Test
    fun statusEmoji() {
        assertEquals(expected = """🗿""", actual = KsoupEntities.decodeHtml("&#128511;"))
    }

    @Test
    fun smileEmoji() {
        assertEquals(expected = """😀""", actual = KsoupEntities.decodeHtml("&#128512;"))
    }

    @Test
    fun happyEmoji() {
        assertEquals(expected = """😁""", actual = KsoupEntities.decodeHtml("&#128513;"))
    }

    @Test
    fun laughEmoji() {
        assertEquals(expected = """😂""", actual = KsoupEntities.decodeHtml("&#128514;"))
    }

    // Encode Test

    @Test
    fun testEncodeHtml5_1() {
        testEncodeHtml5("<p>asdf & ÿ ü '</p>", "&lt;p&gt;asdf &amp; &yuml; &uuml; &apos;&lt;&sol;p&gt;")
    }

    @Test
    fun testEncodeHtml5_2() {
        testEncodeHtml5("Hello <World>!", "Hello &lt;World&gt;&excl;")
    }

    @Test
    fun testEncodeHtml5_3() {
        testEncodeHtml5("The price is $10.", "The price is &dollar;10&period;")
    }

    @Test
    fun testEncodeHtml5_4() {
        testEncodeHtml5("<img src=\"image.jpg\">", "&lt;img src&equals;&quot;image&period;jpg&quot;&gt;")
    }

    @Test
    fun testEncodeHtml5_5() {
        testEncodeHtml5("   ", "&nbsp;&nbsp;&nbsp;")
    }

    @Test
    fun testEncodeHtml5_6() {
        testEncodeHtml5("Å", "&angst;")
    }

}