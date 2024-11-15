import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("module.publication")
}

kotlin {
    applyDefaultHierarchyTemplate()
    explicitApi()
    jvmToolchain(8)

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    jvm()
    js(IR).nodejs()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs().nodejs()
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi().nodejs()

    /* Main source sets */
    sourceSets.commonMain.dependencies {
        // The library is lightweight, we don't use any other dependencies :D
    }

    /* Test source sets */
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}
