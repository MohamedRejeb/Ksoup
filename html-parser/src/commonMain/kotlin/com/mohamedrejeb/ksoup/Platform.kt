package com.mohamedrejeb.ksoup

expect val platform: String

class Greeting {
    fun greeting() = "Hello, $platform!"
}