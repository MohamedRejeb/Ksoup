plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.kotlinx.kover).apply(false)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply(plugin = "org.jetbrains.kotlinx.kover")
}