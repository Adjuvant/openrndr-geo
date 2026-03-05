@file:JvmName("LoadGeojson")
package examples.core

import geo.GeoJSON
import geo.GeoSource

/**
 * ## 01 - Load GeoJSON
 *
 * Demonstrates loading geographic data from a GeoJSON file using the GeoJSON.load() function.
 *
 * ### Concepts
 * - Loading GeoJSON files with GeoJSON.load()
 * - Working with GeoSource to access features
 * - Iterating over features in a FeatureCollection
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.LoadGeojsonKt
 * ```
 */
fun main() {
    // Load GeoJSON from file using the GeoJSON loader
    val source: GeoSource = GeoJSON.load("examples/data/geo/sample.geojson")

    // Get the sequence of features
    val features = source.features

    // Count features (materialize to list to get count)
    val featureList = features.toList()
    val count = featureList.size

    // Print summary to console
    println("=== GeoJSON Loading Example ===")
    println("File: examples/data/geo/sample.geojson")
    println("Features loaded: $count")
    println("CRS: ${source.crs}")

    // Show geometry types of first few features
    println("\nFirst 5 features:")
    featureList.take(5).forEachIndexed { index, feature ->
        println("  ${index + 1}. ${feature.geometry::class.simpleName}")
    }
}
