@file:JvmName("PrintSummary")
package examples.core

import geo.core.*

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
 * - Both loadGeo() and geoSource() work with printSummary()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.PrintSummaryKt
 * ```
 */
fun main() {
    println("=== Data Inspection Example ===\n")

    // Load GeoJSON using auto-magic loader and print summary
    println("Loading GeoJSON with loadGeo()...")
    val geojsonData = loadGeo("examples/data/geo/sample.geojson")
    geojsonData.printSummary()

    println("\n")

    // Load GeoPackage and print summary
    println("Loading GeoPackage with loadGeo()...")
    val gpkgData = loadGeo("examples/data/geo/ness-vectors.gpkg")
    gpkgData.printSummary()
    
    println("\n--- Using geoSource() for explicit control ---")
    val explicitData = geoSource("examples/data/geo/sample.geojson")
    explicitData.printSummary()
}
