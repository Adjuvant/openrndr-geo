@file:JvmName("StyleDsl")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import geo.*
import geo.render.*

/**
 * ## 05 - Style DSL
 *
 * Demonstrates using the inline Style DSL for configuring render appearance.
 * Shows various styling options for points and polygons.
 *
 * ### Concepts
 * - Inline style DSL: drawer.geo(source) { stroke = BLUE; fill = RED }
 * - Point styling: fill, stroke, size, shape
 * - Line styling: stroke, strokeWeight, lineCap, lineJoin
 * - Polygon styling: fill with transparency
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.StyleDslKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load data using three-line workflow
        val data = loadGeo("examples/data/geo/sample.geojson")
        val projection = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw with inline style DSL - different styles for different renders
            
            // Style 1: Filled polygons with solid stroke
            drawer.geo(data, projection) {
                fill = ColorRGBa(0.8, 0.2, 0.2, 0.8)  // Red fill
                stroke = ColorRGBa(0.5, 0.1, 0.1)     // Dark red stroke
                strokeWeight = 2.0
            }
            
            // Draw styled points in corners to show different shapes
            val cornerPoints = listOf(
                Pair(50.0, 50.0),      // Top-left
                Pair(150.0, 50.0),      // Top-right
                Pair(50.0, 150.0)       // Bottom-left
            )
            
            // Square points
            cornerPoints.forEach { (x, y) ->
                drawer.fill = null
                drawer.stroke = ColorRGBa(0.2, 0.4, 0.8)
                drawer.strokeWeight = 1.5
                drawer.rectangle(x - 5, y - 5, 10.0, 10.0)
            }
            
            // Circle points (orange)
            drawer.fill = ColorRGBa(1.0, 0.6, 0.0)
            drawer.stroke = ColorRGBa(0.6, 0.3, 0.0)
            drawer.strokeWeight = 1.0
            cornerPoints.forEach { (x, y) ->
                drawer.circle(x + 100, y, 15.0)
            }
        }
    }
}
