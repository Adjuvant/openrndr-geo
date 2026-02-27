@file:JvmName("Points")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.Point
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.projection.ProjectionFactory
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.DODGER_BLUE

/**
 * ## 01 - Points
 *
 * Demonstrates rendering Point geometries using drawPoint().
 *
 * ### Concepts
 * - Loading GeoJSON data
 * - Creating a projection with ProjectionFactory.fitBounds()
 * - Rendering points with drawPoint()
 * - Basic point styling (color, size, shape)
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.PointsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load point data from GeoJSON
        val data = GeoJSON.load("examples/data/geo/populated_places.geojson")

        // Create a projection that fits the data to the window
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9
        )

        // Define point styling using the Style DSL
        val pointStyle = Style {
            fill = ColorRGBa.ORANGE
            stroke = ColorRGBa.DODGER_BLUE
            strokeWeight = 1.0
            size = 8.0
            shape = Shape.Circle
        }

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Iterate over features and render points
            data.features.forEach { feature ->
                if (feature.geometry is Point) {
                    val point = feature.geometry as Point
                    // Project geographic coordinates to screen coordinates
                    val screenPoint = point.toScreen(projection)
                    // Render the point
                    drawPoint(drawer, screenPoint, pointStyle)
                }
            }
        }
    }
}
