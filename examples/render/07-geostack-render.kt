@file:JvmName("GeoStackRender")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.geoStack

/**
 * ## 07 - GeoStack Rendering
 *
 * Demonstrates rendering multiple geographic datasets through a unified GeoStack
 * with automatic CRS unification and auto-fitting projection.
 *
 * ### Concepts
 * - `geoStack()` for multi-dataset composition
 * - Automatic CRS unification across sources
 * - Multi-dataset overlay rendering
 * - Auto-fit projection with stack.render()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.GeoStackRenderKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load multiple GeoJSON datasets with different geometry types
        println("Loading datasets...")
        val sampleData = GeoJSON.load("examples/data/geo/sample.geojson")
        val cities = GeoJSON.load("examples/data/geo/populated_places.geojson")
        val riversLakes = GeoJSON.load("examples/data/geo/rivers_lakes.geojson")

        // Create a GeoStack combining all sources
        // Automatically unifies CRS across all datasets
        println("Creating GeoStack with ${listOf(sampleData, cities, riversLakes).size} sources...")
        val stack = geoStack(sampleData, cities, riversLakes)

        println("GeoStack ready: ${stack.sourceCount()} sources, unified CRS: ${stack.crs}")

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Render all features in the stack with auto-fit projection
            // Projection is automatically calculated to fit all data to the window
            stack.render(drawer)
        }
    }
}
