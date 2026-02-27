@file:JvmName("Linestrings")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.LineString
import geo.render.Style
import geo.render.drawLineString
import geo.projection.ProjectionFactory

/**
 * ## 02 - LineStrings
 *
 * Demonstrates rendering LineString geometries using drawLineString().
 *
 * ### Concepts
 * - Loading GeoJSON line data
 * - Projecting LineString coordinates to screen space
 * - Styling lines with stroke color and weight
 * - Understanding coordinate transformations
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.LinestringsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load line data from GeoJSON
        val data = GeoJSON.load("examples/data/geo/rivers_lakes.geojson")

        // Create a projection that fits the data to the window
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9
        )

        // Define line styling using the Style DSL
        val lineStyle = Style {
            stroke = ColorRGBa(0.0, 0.4, 0.8)  // Blue color
            strokeWeight = 1.5
        }

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Iterate over features and render LineStrings
            data.features.forEach { feature ->
                if (feature.geometry is LineString) {
                    val line = feature.geometry as LineString
                    // Project all coordinates in the LineString to screen space
                    val screenLine = line.toScreen(projection)
                    // Render the LineString
                    drawLineString(drawer, screenLine, lineStyle)
                }
            }
        }
    }
}
