@file:JvmName("PrintSummary")
package examples.core

import geo.GeoJSON
import geo.GeoPackage

/**
 * ## 03 - Print Summary
 *
 * Demonstrates using printSummary() to inspect geographic data at runtime.
 * printSummary() provides a comprehensive overview of a GeoSource including
 * feature count, bounding box, geometry types, and property schema.
 *
 * ### Concepts
 * - Using printSummary() for data inspection
 * - Understanding GeoSource metadata
 * - Console output for non-visual examples
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.PrintSummaryKt
 * ```
 */
fun main() {
    println("=== Data Inspection Example ===\n")

    // Load GeoJSON and print summary
    println("Loading GeoJSON...")
    val geojsonSource = GeoJSON.load("examples/data/geo/sample.geojson")
    geojsonSource.printSummary()

    println("\n")

    // Load GeoPackage and print summary
    println("Loading GeoPackage...")
    val gpkgSource = GeoPackage.load("examples/data/geo/ness-vectors.gpkg")
    gpkgSource.printSummary()
}
