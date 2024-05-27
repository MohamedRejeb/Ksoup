plugins {
    id("root.publication")
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.allopen).apply(false)
    alias(libs.plugins.kotlinx.kover).apply(false)
    alias(libs.plugins.kotlinx.benchmark).apply(false)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlinx.kover")
}