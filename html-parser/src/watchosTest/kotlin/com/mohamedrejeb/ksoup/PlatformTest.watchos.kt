package com.mohamedrejeb.ksoup

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun testExample() {
        assertTrue(Greeting().greeting().contains("watchos"), "Check watchos is mentioned")
    }
}