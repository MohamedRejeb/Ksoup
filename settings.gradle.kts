rootProject.name = "Ksoup"

pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":ksoup-html",
    ":ksoup-entities",
)