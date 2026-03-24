@file:JvmName("GeoStack")
package examples.core

import geo.core.*

/**
 * ## 04 - GeoStack
 *
 * Demonstrates combining multiple geographic datasets into a unified stack using GeoStack.
 * GeoStack automatically handles CRS unification across all sources.
 *
 * ### Concepts
 * - `geoStack()` for combining multiple GeoSources
 * - Automatic CRS unification across sources
 * - Querying stack metadata (source count, total features, bounding box)
 * - Multi-dataset composition for overlay rendering
 * - Works with both loadGeo() and geoSource()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.GeoStackKt
 * ```
 */
fun main() {
    println("=== GeoStack Multi-Dataset Composition Example ===\n")

    // Load multiple datasets using new API
    println("Loading datasets...")
    val sampleData = loadGeo("examples/data/geo/sample.geojson")
    val cities = loadGeo("examples/data/geo/populated_places.geojson")
    val riversLakes = loadGeo("examples/data/geo/rivers_lakes.geojson")

    // Create a GeoStack combining all sources
    println("Creating GeoStack...")
    val stack = geoStack(sampleData, cities, riversLakes)

    println()

    // Display GeoStack capabilities
    println("┌─────────────────────────────────────────────────────┐")
    println("│                  GeoStack Summary                   │")
    println("├─────────────────────────────────────────────────────┤")
    println("│ Sources:          ${stack.sourceCount().toString().padEnd(36)}│")
    println("│ Unified CRS:      ${stack.crs.padEnd(36)}│")
    println("│ Total Features:   ${stack.features.toList().size.toString().padEnd(36)}│")

    val bounds = stack.totalBoundingBox()
    println("│ Bounding Box:                                             │")
    println("│   minX: ${bounds.minX.toString().take(10).padEnd(10)}  minY: ${bounds.minY.toString().take(10).padEnd(10)}         │")
    println("│   maxX: ${bounds.maxX.toString().take(10).padEnd(10)}  maxY: ${bounds.maxY.toString().take(10).padEnd(10)}         │")
    println("└─────────────────────────────────────────────────────┘")

    println()
    println("GeoStack ready for overlay rendering with automatic CRS handling!")
}
