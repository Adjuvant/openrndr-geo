@file:JvmName("StyleDsl")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import geo.GeoJSON
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.render.drawPolygon
import geo.projection.ProjectionFactory

/**
 * ## 05 - Style DSL
 *
 * Demonstrates using the Style { } DSL for configuring render appearance.
 * Shows various styling options for points and polygons.
 *
 * ### Concepts
 * - Style DSL syntax and configuration
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
        // Load polygon data for demonstration
        val data = GeoJSON.load("examples/data/geo/sample.geojson")

        // Create a projection that fits the data to the window
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9
        )

        // Style 1: Filled polygon with solid stroke
        val filledStyle = Style {
            fill = ColorRGBa(0.8, 0.2, 0.2, 0.8)  // Red fill
            stroke = ColorRGBa(0.5, 0.1, 0.1)     // Dark red stroke
            strokeWeight = 2.0
        }

        // Style 2: Points only (no fill, colored stroke)
        val pointStyle = Style {
            fill = null  // No fill - outline only
            stroke = ColorRGBa(0.2, 0.4, 0.8)     // Blue stroke
            strokeWeight = 1.5
            size = 10.0
            shape = Shape.Square
        }

        // Style 3: Filled points with round caps for lines
        val roundStyle = Style {
            fill = ColorRGBa(0.2, 0.7, 0.3)       // Green fill
            stroke = ColorRGBa(0.1, 0.4, 0.1)     // Dark green stroke
            strokeWeight = 2.0
            size = 12.0
            shape = Shape.Circle
            lineCap = LineCap.ROUND
            lineJoin = LineJoin.ROUND
        }

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw polygons with filled style
            data.features.forEach { feature ->
                val geometry = feature.geometry
                when {
                    geometry is geo.Polygon -> {
                        val screenExterior = geometry.exterior.map { projection.toScreen(it.x, it.y) }
                        val screenInteriors = geometry.interiors.map { ring ->
                            ring.map { projection.toScreen(it.x, it.y) }
                        }
                        drawPolygon(drawer, screenExterior, screenInteriors, filledStyle)
                    }
                    geometry is geo.Point -> {
                        val screenPoint = projection.toScreen(geometry.x, geometry.y)
                        drawPoint(drawer, screenPoint, pointStyle)
                    }
                }
            }

            // Draw some styled points in corners to show different shapes
            val cornerPoints = listOf(
                Pair(50.0, 50.0),      // Top-left
                Pair(150.0, 50.0),      // Top-right
                Pair(50.0, 150.0)       // Bottom-left
            )

            // Draw with square points
            cornerPoints.forEach { (x, y) ->
                drawPoint(drawer, org.openrndr.math.Vector2(x, y), pointStyle)
            }

            // Draw with circle points
            val circleStyle = Style {
                fill = ColorRGBa(1.0, 0.6, 0.0)   // Orange
                stroke = ColorRGBa(0.6, 0.3, 0.0)
                strokeWeight = 1.0
                size = 15.0
                shape = Shape.Circle
            }
            cornerPoints.forEach { (x, y) ->
                drawPoint(drawer, org.openrndr.math.Vector2(x + 100, y), circleStyle)
            }
        }
    }
}
