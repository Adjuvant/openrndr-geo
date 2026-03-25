import java.time.Duration

group = property("project.group") ?: error("project.group not set")
version = property("project.version") ?: error("project.version not set")

plugins {
    alias(libs.plugins.kotlin.serialization)
    id("conventions.kotlin-jvm")
    id("conventions.openrndr-tasks")
    id("conventions.distribute-application")
}

dependencies {
    implementation(openrndr.application.glfw)
    implementation(openrndr.draw)
    implementation(openrndr.openal)
    runtimeOnly(openrndr.gl3)

    implementation(openrndr.dialogs)
    implementation(openrndr.orextensions)

    implementation(openrndr.ffmpeg)
    implementation(orx.bundles.basic)
    implementation(orx.olive)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    runtimeOnly(libs.bundles.logging.simple)

    testImplementation(libs.junit)

    // Non-openrndr
    /* GeoPackage support for reading spatial data files */
    implementation("mil.nga.geopackage:geopackage:6.6.5")
    /* Coordinate reference system transformations */
    implementation("org.locationtech.proj4j:proj4j:1.4.1")
    implementation("org.locationtech.proj4j:proj4j-epsg:1.4.1")

}

// Add examples directory to Kotlin source sets
kotlin.sourceSets.getByName("main") {
    kotlin.srcDir("examples")
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir("uat")
}

// Default test task — exclude slow benchmarks
tasks.test {
    filter {
        excludeTestsMatching("geo.performance.*")
    }
}

// Run benchmarks explicitly
tasks.register<Test>("benchmark") {
    description = "Runs performance benchmarks"
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    filter {
        includeTestsMatching("geo.performance.*")
    }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }

    timeout = Duration.ofMinutes(15)
    jvmArgs = listOf("-Xmx2g")
}
// ============================================================================
// Regression Testing
// ============================================================================
// Dedicated test task for running regression tests on all examples
// Usage:
//   ./gradlew regressionTest           - Run only regression tests
//   ./gradlew test                     - Run all tests including regression
//   ./gradlew check                    - Run all verification tasks
//
// The regressionTest task validates that all v1.2.0 examples continue to work
// with the Phase 11-12 optimizations (batch projection and viewport caching).
// ============================================================================
tasks.register<Test>("regressionTest") {
    description = "Runs regression tests for all examples to verify backward compatibility"
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    filter {
        includeTestsMatching("geo.regression.*")
    }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }

    // Longer timeout for running actual examples
    timeout = Duration.ofMinutes(10)

    // Ensure examples are compiled before running regression tests
    dependsOn("compileKotlin")

    // JVM arguments for test execution
    jvmArgs = listOf("-Xmx2g")
}

// Add regression tests to the check lifecycle
tasks.named("check") {
    dependsOn("regressionTest")
}

tasks.register("fileIndex") {
    description = "Generates .planning/FILE_INDEX.md with class-to-file mapping"
    group = "documentation"

    // Depend on compilation so the class metadata is available
    dependsOn("compileKotlin")

    doLast {
        val outputFile = file(".planning/FILE_INDEX.md")
        outputFile.parentFile.mkdirs()

        val sourceRoots = listOf("src/main/kotlin", "src/test/kotlin", "examples", "uat")
        val entries = mutableListOf<String>()

        sourceRoots.forEach { root ->
            val rootDir = file(root)
            if (!rootDir.exists()) return@forEach

            rootDir.walkTopDown()
                .filter { it.extension == "kt" }
                .sortedBy { it.relativeTo(projectDir).path }
                .forEach { ktFile ->
                    val relativePath = ktFile.relativeTo(projectDir).path
                    val content = ktFile.readText()

                    // Extract @file:JvmName
                    val jvmName = Regex("""@file:JvmName\("([^"]+)"\)""")
                        .find(content)?.groupValues?.get(1)

                    // Extract class/object/interface declarations
                    val declarations = Regex("""^(?:enum class|data class|sealed class|sealed interface|abstract class|open class|class|object|interface)\s+([A-Z][A-Za-z0-9_]+)""", RegexOption.MULTILINE)
                        .findAll(content)
                        .map { it.groupValues[1] }
                        .distinct()
                        .sorted()
                        .toList()

                    // Extract top-level fun declarations (not inside a class)
                    val topFuns = Regex("""^fun\s+(?:<[^>]+>\s+)?([a-zA-Z][A-Za-z0-9_]+)\s*\(""", RegexOption.MULTILINE)
                        .findAll(content)
                        .map { it.groupValues[1] }
                        .distinct()
                        .sorted()
                        .toList()

                    val parts = mutableListOf<String>()

                    if (jvmName != null) {
                        parts.add("@JvmName($jvmName)")
                    }

                    if (declarations.isNotEmpty()) {
                        parts.add(declarations.joinToString(", "))
                    } else if (jvmName == null) {
                        // No classes, no JvmName — infer effective class name
                        val baseName = ktFile.nameWithoutExtension
                            .replaceFirstChar { it.uppercaseChar() }
                        parts.add("${baseName}Kt (top-level)")
                    }

                    if (topFuns.isNotEmpty() && declarations.isEmpty()) {
                        parts.add("fns: ${topFuns.joinToString(", ")}")
                    }

                    val annotation = parts.joinToString("  ")
                    entries.add("%-75s → %s".format(relativePath, annotation))
                }
        }

        val output = buildString {
            appendLine("# FILE_INDEX")
            appendLine()
            appendLine("Auto-generated class-to-file lookup for openrndr-geo.")
            appendLine("Run `./gradlew fileIndex` to regenerate.")
            appendLine()
            appendLine("```")
            entries.forEach { appendLine(it) }
            appendLine("```")
        }

        outputFile.writeText(output)
        println("Written ${entries.size} entries to ${outputFile.relativeTo(projectDir).path}")
    }
}

// ============================================================================
// UAT Verification Scripts
// ============================================================================
// Run UAT scripts from the uat/ directory
// Usage:
//   ./gradlew runUAT --uats=uAtClassName    - Run specific UAT (e.g., Uat_GraticuleLayerFeatures)
//
// The UAT scripts provide visual verification of features that can't be unit tested.
// They open a window showing the feature in action so you can verify it manually.
// ============================================================================
tasks.register<JavaExec>("runUAT") {
    description = "Run a UAT visual verification script from uat/"
    group = "verification"

    // The UAT class to run - pass via -Puats=ClassName
    val uatClass = project.findProperty("uats") as String? ?: "Uat_GraticuleLayerFeatures"
    val fullClassName = "uat.$uatClass"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(fullClassName)

    doFirst {
        println("Running UAT: $fullClassName")
        println("To run a different UAT: ./gradlew runUAT -Puats=UatClassName")
    }
}