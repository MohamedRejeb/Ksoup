plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.kotlinx.kover).apply(false)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    group = "com.mohamedrejeb.ksoup"
    version = "0.1.2"

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                val isSnapshot = version.toString().endsWith("SNAPSHOT")
                url = uri(
                    if (!isSnapshot) "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
                    else "https://s01.oss.sonatype.org/content/repositories/snapshots"
                )

                credentials {
                    username = System.getenv("OssrhUsername")
                    password = System.getenv("OssrhPassword")
                }
            }
        }

        val javadocJar = tasks.register<Jar>("javadocJar") {
            archiveClassifier.set("javadoc")
        }

        publications {
            withType<MavenPublication> {
                artifact(javadocJar)

                pom {
                    name.set("Ksoup")
                    description.set("A lightweight Kotlin Multiplatform library to parse HTML/XML data.")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                        }
                    }
                    url.set("https://github.com/MohamedRejeb/Ksoup")
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/MohamedRejeb/Ksoup/issues")
                    }
                    scm {
                        connection.set("https://github.com/MohamedRejeb/Ksoup.git")
                        url.set("https://github.com/MohamedRejeb/Ksoup")
                    }
                    developers {
                        developer {
                            name.set("Mohamed Rejeb")
                            email.set("mohamedrejeb445@gmail.com")
                        }
                    }
                }
            }
        }
    }

    val publishing = extensions.getByType<PublishingExtension>()
    extensions.configure<SigningExtension> {
        useInMemoryPgpKeys(
            System.getenv("SigningKeyId"),
            System.getenv("SigningKey"),
            System.getenv("SigningPassword"),
        )

        sign(publishing.publications)
    }

    // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(project.tasks.withType(Sign::class.java))
    }

}