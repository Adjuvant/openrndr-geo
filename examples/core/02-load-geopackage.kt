@file:JvmName("LoadGeopackage")
package examples.core

import geo.GeoPackage
import geo.GeoPackageSource

/**
 * ## 02 - Load GeoPackage
 *
 * Demonstrates loading geographic data from a GeoPackage (.gpkg) file using the GeoPackage.load() function.
 *
 * ### Concepts
 * - Loading GeoPackage files with GeoPackage.load()
 * - Working with GeoPackageSource for spatial queries
 * - Understanding GeoPackage as an alternative to GeoJSON
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.LoadGeopackageKt
 * ```
 */
fun main() {
    // Load GeoPackage from file using the GeoPackage loader
    val source: GeoPackageSource = GeoPackage.load("examples/data/geo/ness-vectors.gpkg")

    // Get the sequence of features
    val features = source.features

    // Count features
    val featureList = features.toList()
    val count = featureList.size

    // Print summary to console
    println("=== GeoPackage Loading Example ===")
    println("File: examples/data/geo/ness-vectors.gpkg")
    println("Features loaded: $count")
    println("CRS: ${source.crs}")

    // Show geometry types of first few features
    println("\nFirst 5 features:")
    featureList.take(5).forEachIndexed { index, feature ->
        println("  ${index + 1}. ${feature.geometry::class.simpleName}")
    }
}
