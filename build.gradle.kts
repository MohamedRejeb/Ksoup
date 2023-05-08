buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.bundles.plugins)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    group = "com.mohamedrejeb.ksoup"
    version = "0.1.0"

//    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

//    extensions.configure<PublishingExtension> {
//        repositories {
//            maven {
//                val isSnapshot = version.toString().endsWith("SNAPSHOT")
//                url = uri(
//                    if (!isSnapshot) "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
//                    else "https://s01.oss.sonatype.org/content/repositories/snapshots"
//                )
//
//                credentials {
//                    username = System.getenv("OssrhUsername")
//                    password = System.getenv("OssrhPassword")
//                }
//            }
//        }
//
//        val javadocJar = tasks.register<Jar>("javadocJar") {
////            dependsOn(tasks.dokkaHtml)
//            archiveClassifier.set("javadoc")
////            from("$buildDir/dokka")
//        }
//
//        publications {
//            withType<MavenPublication> {
//                artifact(javadocJar)
//
//                pom {
//                    name.set("Compose Rich Editor")
//                    description.set("A Compose multiplatform library that provides a rich text editor.")
//                    licenses {
//                        license {
//                            name.set("Apache-2.0")
//                            url.set("https://opensource.org/licenses/Apache-2.0")
//                        }
//                    }
//                    url.set("https://github.com/MohamedRejeb/Compose-Rich-Editor")
//                    issueManagement {
//                        system.set("Github")
//                        url.set("https://github.com/MohamedRejeb/Compose-Rich-Editor/issues")
//                    }
//                    scm {
//                        connection.set("https://github.com/MohamedRejeb/Compose-Rich-Editor.git")
//                        url.set("https://github.com/MohamedRejeb/Compose-Rich-Editor")
//                    }
//                    developers {
//                        developer {
//                            name.set("Mohamed Rejeb")
//                            email.set("mohamedrejeb445@gmail.com")
//                        }
//                    }
//                }
//            }
//        }
//    }

//    val publishing = extensions.getByType<PublishingExtension>()
//    extensions.configure<SigningExtension> {
//        useInMemoryPgpKeys(
//            System.getenv("SigningKeyId"),
//            System.getenv("SigningKey"),
//            System.getenv("SigningPassword"),
//        )
//
//        sign(publishing.publications)
//    }

}