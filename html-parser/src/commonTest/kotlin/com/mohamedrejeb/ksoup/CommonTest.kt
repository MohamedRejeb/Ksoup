package com.mohamedrejeb.ksoup

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class CommonTest {

    @Test
    fun testExample() {
        assertTrue(Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }

    @Test
    fun testExample2() {
        val name: String = "html"

//        println("""
//            mapOf(
//                ${map.map { (key, value) -> "\"$key\" to \"$value\"" }.joinToString(", ")}
//            )
//        """.trimIndent())

        assertTrue(true)
    }
}