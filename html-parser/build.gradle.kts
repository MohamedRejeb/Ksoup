plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
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
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.touchlab.kermit)
                implementation(libs.kotlinx.serialization.json)
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
        val tvosSimulatorArm64Main by getting
        val watchosX64Main by getting 
        val watchosArm64Main by getting  
        val watchosSimulatorArm64Main by getting
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
        tvosSimulatorArm64Main.dependsOn(tvosMain)
        watchosMain.dependsOn(nativeMain)
        watchosX64Main.dependsOn(watchosMain)
        watchosArm64Main.dependsOn(watchosMain)
        watchosSimulatorArm64Main.dependsOn(watchosMain)
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
        val nativeTest by creating
        val jvmTest by getting
        val jsTest by getting
        val iosTest by creating
        val tvosTest by creating
        val watchosTest by creating
        val linuxTest by creating
        val macosTest by creating
        val windowsTest by creating
        val iosX64Test by getting 
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val tvosX64Test by getting 
        val tvosArm64Test by getting 
        val tvosSimulatorArm64Test by getting
        val watchosX64Test by getting 
        val watchosArm64Test by getting  
        val watchosSimulatorArm64Test by getting
        val linuxX64Test by getting
        val macosX64Test by getting 
        val macosArm64Test by getting
        val mingwX64Test by getting

        /* Test hierarchy */
        nativeTest.dependsOn(commonTest)
        jvmTest.dependsOn(commonTest)
        jsTest.dependsOn(commonTest)
        iosTest.dependsOn(nativeTest)
        iosX64Test.dependsOn(iosTest)
        iosArm64Test.dependsOn(iosTest)
        iosSimulatorArm64Test.dependsOn(iosTest)
        tvosTest.dependsOn(nativeTest)
        tvosX64Test.dependsOn(tvosTest)
        tvosArm64Test.dependsOn(tvosTest)
        tvosSimulatorArm64Test.dependsOn(tvosTest)
        watchosTest.dependsOn(nativeTest)
        watchosX64Test.dependsOn(watchosTest)
        watchosArm64Test.dependsOn(watchosTest)
        watchosSimulatorArm64Test.dependsOn(watchosTest)
        linuxTest.dependsOn(nativeTest)
        linuxX64Test.dependsOn(linuxTest)
        macosTest.dependsOn(nativeTest)
        macosX64Test.dependsOn(macosTest)
        macosArm64Test.dependsOn(macosTest)
        windowsTest.dependsOn(nativeTest)
        mingwX64Test.dependsOn(windowsTest)
    }
}
