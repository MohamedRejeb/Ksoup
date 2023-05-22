plugins {
    kotlin("multiplatform")
}

kotlin {
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
//    tvosSimulatorArm64()
    watchosX64()
    watchosArm64()
//    watchosSimulatorArm64()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        /* Main source sets */
        val commonMain by getting {
            dependencies {
                implementation(project(":ksoup-entites"))
                // The library is lightweight, we don't use any other dependencies :D
            }
        }
        val nativeMain by creating
        val jvmMain by getting
        val jsMain by getting
        val iosMain by creating
        val tvosMain by creating
        val watchosMain by creating
        val linuxMain by creating
        val macosMain by creating
        val windowsMain by creating
        val iosX64Main by getting 
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val tvosX64Main by getting 
        val tvosArm64Main by getting 
//        val tvosSimulatorArm64Main by getting
        val watchosX64Main by getting 
        val watchosArm64Main by getting
//        val watchosSimulatorArm64Main by getting
        val linuxX64Main by getting
        val macosX64Main by getting 
        val macosArm64Main by getting
        val mingwX64Main by getting

        /* Main hierarchy */
        nativeMain.dependsOn(commonMain)
        jvmMain.dependsOn(commonMain)
        jsMain.dependsOn(commonMain)
        iosMain.dependsOn(nativeMain)
        iosX64Main.dependsOn(iosMain)
        iosArm64Main.dependsOn(iosMain)
        iosSimulatorArm64Main.dependsOn(iosMain)
        tvosMain.dependsOn(nativeMain)
        tvosX64Main.dependsOn(tvosMain)
        tvosArm64Main.dependsOn(tvosMain)
//        tvosSimulatorArm64Main.dependsOn(tvosMain)
        watchosMain.dependsOn(nativeMain)
        watchosX64Main.dependsOn(watchosMain)
        watchosArm64Main.dependsOn(watchosMain)
//        watchosSimulatorArm64Main.dependsOn(watchosMain)
        linuxMain.dependsOn(nativeMain)
        linuxX64Main.dependsOn(linuxMain)
        macosMain.dependsOn(nativeMain)
        macosX64Main.dependsOn(macosMain)
        macosArm64Main.dependsOn(macosMain)
        windowsMain.dependsOn(nativeMain)
        mingwX64Main.dependsOn(windowsMain)

        /* Test source sets */
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
