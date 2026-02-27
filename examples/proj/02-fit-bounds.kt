@file:JvmName("FitBounds")
package examples.proj

import geo.GeoJSON
import geo.Point
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.projection.toScreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.render.Style
import geo.render.drawPoint
import geo.render.Shape
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.CYAN

/**
 * ## 02 - Fit Bounds Projection
 *
 * Demonstrates using ProjectionFactory.fitBounds() to automatically fit
 * a projection to the geographic extent of your data.
 *
 * ### Concepts
 * - Automatic projection fitting to data bounds
 * - Padding parameter for margin around the viewport
 * - Equirectangular vs Mercator projection types
 * - How fitBounds calculates zoom level from data extent
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.proj.FitBoundsKt
 * ```
 */
fun main() = application {
    configure {
        width = 900
        height = 700
    }

    program {
        // Load populated places data
        val data = GeoJSON.load("examples/data/geo/populated_places.geojson")

        // Get the bounding box of the data
        val dataBounds = data.boundingBox()

        println("Data bounds: $dataBounds")

        // Create a projection that fits the data to the viewport
        // Using padding to leave some margin around the edges
        val projection = ProjectionFactory.fitBounds(
            bounds = dataBounds,
            width = width.toDouble(),
            height = height.toDouble(),
            padding = 40.0,
            projection = ProjectionType.EQUIRECTANGULAR
        )

        // Also create a Mercator version for comparison
        val mercatorProjection = ProjectionFactory.fitBounds(
            bounds = dataBounds,
            width = width.toDouble(),
            height = height.toDouble(),
            padding = 40.0,
            projection = ProjectionType.MERCATOR
        )

        // Style for points
        val pointStyle = Style {
            fill = ColorRGBa.ORANGE
            stroke = ColorRGBa.CYAN
            strokeWeight = 1.0
            size = 6.0
            shape = Shape.Circle
        }

        var showMercator = false

        keyboard.keyUp.listen {
            if (it.key.name == "Space") {
                showMercator = !showMercator
            }
        }

        extend {
            val currentProjection = if (showMercator) mercatorProjection else projection

            // Clear background
            drawer.clear(ColorRGBa(0.1, 0.1, 0.15))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text(
                if (showMercator) "Mercator Projection" else "Equirectangular Projection",
                20.0, 30.0
            )
            drawer.text("Data bounds fitted to viewport (padding=40px)", 20.0, 50.0)
            drawer.text("Press SPACE to toggle projection type", 20.0, 70.0)

            // Render points
            data.features.take(500).forEach { feature ->
                if (feature.geometry is Point) {
                    val point = feature.geometry as Point
                    val screenPoint = currentProjection.toScreen(point.x, point.y)
                    drawPoint(drawer, screenPoint, pointStyle)
                }
            }
        }
    }
}
