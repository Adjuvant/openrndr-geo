@file:JvmName("Multipolygons")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.MultiPolygon
import geo.render.Style
import geo.render.drawMultiPolygon
import geo.projection.ProjectionFactory
import org.openrndr.extra.color.presets.DEEP_SKY_BLUE

/**
 * ## 04 - MultiPolygons
 *
 * Demonstrates rendering MultiPolygon geometries using drawMultiPolygon().
 * MultiPolygons are used for collections of polygons, such as country boundaries
 * with separate land masses or islands.
 *
 * ### Concepts
 * - Loading GeoJSON with MultiPolygon geometries
 * - Rendering multiple polygons as a single feature
 * - Styling complex geometry collections
 * - Understanding when to use MultiPolygon vs Polygon
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.MultipolygonsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load MultiPolygon data from GeoJSON (e.g., world ocean)
        val data = GeoJSON.load("examples/data/geo/ocean.geojson")

        // Create a projection that fits the data to the window
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9
        )

        // Define polygon styling using the Style DSL
        val oceanStyle = Style {
            fill = ColorRGBa.DEEP_SKY_BLUE
            stroke = ColorRGBa(0.0, 0.3, 0.6)
            strokeWeight = 0.5
        }

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Iterate over features and render MultiPolygons
            data.features.forEach { feature ->
                if (feature.geometry is MultiPolygon) {
                    val multiPolygon = feature.geometry as MultiPolygon

                    // Render all polygons in the MultiPolygon - handles holes and clamping automatically
                    drawMultiPolygon(drawer, multiPolygon, projection, oceanStyle)
                }
            }
        }
    }
}
