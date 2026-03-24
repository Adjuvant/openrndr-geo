@file:JvmName("StyleDsl")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import geo.core.*
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
        val p = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw with inline style DSL - different styles for different renders
            
            // Style 1: Filled polygons with solid stroke, style passed in DSL
            drawer.geo(data) {
                projection = p
                fill = ColorRGBa.PINK.opacify(.8)  // Red fill
                stroke = ColorRGBa.RED     // Dark red stroke
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
                drawer.stroke = ColorRGBa.BLUE
                drawer.strokeWeight = 1.5
                drawer.rectangle(x - 5, y - 5, 10.0, 10.0)
            }
            
            // Circle points (orange)
            drawer.stroke = ColorRGBa.BLACK
            drawer.strokeWeight = 5.0
            drawer.circles{
                cornerPoints.forEach { (x, y) ->
                    drawer.fill = ColorRGBa(1.0, 0.6, 0.0)
                    circle(x + 100, y*2, 15.0)
                }
            }

        }
    }
}
