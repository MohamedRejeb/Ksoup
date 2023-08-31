package com.mohamedrejeb.ksoup

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import kotlin.test.Test
import kotlin.test.assertEquals

class KsoupEntitesTest {

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