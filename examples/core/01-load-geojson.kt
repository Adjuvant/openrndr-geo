@file:JvmName("LoadGeojson")
package examples.core

import geo.core.*

/**
 * ## 01 - Load GeoJSON
 *
 * Demonstrates loading geographic data from a GeoJSON file using the loadGeo() function.
 *
 * ### Concepts
 * - Loading GeoJSON files with loadGeo() (auto-magic) or geoSource() (explicit)
 * - Working with GeoSource to access features
 * - Iterating over features in a FeatureCollection
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.LoadGeojsonKt
 * ```
 */
fun main() {
    // Load GeoJSON using auto-magic loader (auto-CRS, auto-caching)
    val data = loadGeo("examples/data/geo/sample.geojson")

    // Get the sequence of features
    val features = data.features

    // Count features (materialize to list to get count)
    val featureList = features.toList()
    val count = featureList.size

    // Print summary to console
    println("=== GeoJSON Loading Example ===")
    println("File: examples/data/geo/sample.geojson")
    println("Features loaded: $count")
    println("CRS: ${data.crs}")

    // Show geometry types of first few features
    println("\nFirst 5 features:")
    featureList.take(5).forEachIndexed { index, feature ->
        println("  ${index + 1}. ${feature.geometry::class.simpleName}")
    }
    
    // Alternative: Use geoSource() for explicit control
    println("\n--- Using geoSource() for explicit control ---")
    val explicitSource = geoSource("examples/data/geo/sample.geojson")
    println("Explicit load - Features: ${explicitSource.countFeatures()}")
}
