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