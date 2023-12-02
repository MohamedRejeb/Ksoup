plugins {
    kotlin("multiplatform")
}

kotlin {
    applyDefaultHierarchyTemplate()
    explicitApi()
    
    jvm {
        jvmToolchain(11)
    }
    js(IR) {
        browser()
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        /* Main source sets */
        val commonMain by getting {
            dependencies {
                // The library is lightweight, we don't use any other dependencies :D
            }
        }

        /* Test source sets */
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
