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

    // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
//        dependsOn(project.tasks.withType(Sign::class.java))
//    }

}