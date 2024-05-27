import kotlinx.benchmark.gradle.JsBenchmarkTarget
import kotlinx.benchmark.gradle.JsBenchmarksExecutor
import kotlinx.benchmark.gradle.JvmBenchmarkTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlinx.benchmark)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm {
        val mainCompilation = compilations["main"]
        compilations.create("benchmark") { associateWith(mainCompilation) }
    }
    js(IR) {
        nodejs()
        val mainCompilation = compilations["main"]
        compilations.create("defaultExecutor") { associateWith(mainCompilation) }
        compilations.create("builtInExecutor") { associateWith(mainCompilation) }
    }
    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs().nodejs()

    // Native targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets.commonMain.dependencies {
        implementation(projects.ksoupEntities)
        implementation(projects.ksoupHtml)

        implementation(libs.kotlinx.benchmark.runtime)
    }

    sourceSets.jsMain {
        sourceSets["jsDefaultExecutor"].dependsOn(this)
        sourceSets["jsBuiltInExecutor"].dependsOn(this)
    }
}

// Configure benchmark
benchmark {
    configurations {
        named("main") { // --> jvmBenchmark, jsBenchmark, <native target>Benchmark, benchmark
            iterations = 5 // number of iterations
            iterationTime = 300
            iterationTimeUnit = "ms"
            advanced("jvmForks", 3)
            advanced("jsUseBridge", true)
        }

        register("params") {
            iterations = 5 // number of iterations
            iterationTime = 300
            iterationTimeUnit = "ms"
            include("ParamBenchmark")
            param("data", 5, 1, 8)
            param("unused", 6, 9)
        }

        register("fast") { // --> jvmFastBenchmark, jsFastBenchmark, <native target>FastBenchmark, fastBenchmark
            include("Common")
            exclude("long")
            iterations = 5
            iterationTime = 300 // time in ms per iteration
            iterationTimeUnit = "ms" // time in ms per iteration
            advanced("nativeGCAfterIteration", true)
        }

        register("csv") {
            include("Common")
            exclude("long")
            iterations = 1
            iterationTime = 300
            iterationTimeUnit = "ms"
            reportFormat = "csv" // csv report format
        }

        register("fork") {
            include("CommonBenchmark")
            iterations = 5
            iterationTime = 300
            iterationTimeUnit = "ms"
            advanced("jvmForks", "definedByJmh") // see README.md for possible "jvmForks" values
            advanced("nativeFork", "perIteration") // see README.md for possible "nativeFork" values
        }
    }

    // Setup configurations
    targets {
        // This one matches target name, e.g. 'jvm', 'js',
        // and registers its 'main' compilation, so 'jvm' registers 'jvmMain'
        register("jvm") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.21"
        }
        // This one matches source set name, e.g. 'jvmMain', 'jvmTest', etc
        // and register the corresponding compilation (here the 'benchmark' compilation declared in the 'jvm' target)
        register("jvmBenchmark") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.21"
        }
        register("jsDefaultExecutor")
        register("jsBuiltInExecutor") {
            this as JsBenchmarkTarget
            jsBenchmarksExecutor = JsBenchmarksExecutor.BuiltIn
        }
        register("wasmJs")

        // Native targets
        register("iosX64")
        register("iosArm64")
        register("iosSimulatorArm64")
        register("macosX64")
        register("macosArm64")
        register("linuxX64")
        register("linuxArm64")
        register("mingwX64")
    }
}

// Node.js with canary v8 that supports recent Wasm GC changes
rootProject.extensions.findByType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>()?.apply {
    version = "21.0.0-v8-canary202309167e82ab1fa2"
    downloadBaseUrl = "https://nodejs.org/download/v8-canary"
}

// Drop this when node js version become stable
configure(rootProject.tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask::class)) {
    args.add("--ignore-engines")
}
