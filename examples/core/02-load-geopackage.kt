@file:JvmName("LoadGeopackage")
package examples.core

import geo.core.*

/**
 * ## 02 - Load GeoPackage
 *
 * Demonstrates loading geographic data from a GeoPackage (.gpkg) file using the loadGeo() function.
 *
 * ### Concepts
 * - Loading GeoPackage files with loadGeo() or geoSource()
 * - Working with GeoSource for spatial queries
 * - Understanding GeoPackage as an alternative to GeoJSON
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.LoadGeopackageKt
 * ```
 */
fun main() {
    // Load GeoPackage using auto-magic loader
    val data = loadGeo("examples/data/geo/ness-vectors.gpkg")

    // Get the sequence of features
    val features = data.features

    // Count features
    val featureList = features.toList()
    val count = featureList.size

    // Print summary to console
    println("=== GeoPackage Loading Example ===")
    println("File: examples/data/geo/ness-vectors.gpkg")
    println("Features loaded: $count")
    println("CRS: ${data.crs}")

    // Show geometry types of first few features
    println("\nFirst 5 features:")
    featureList.take(5).forEachIndexed { index, feature ->
        println("  ${index + 1}. ${feature.geometry::class.simpleName}")
    }
    
    // Use printSummary for detailed info
    println("\n--- Source Summary ---")
    data.printSummary()
}
