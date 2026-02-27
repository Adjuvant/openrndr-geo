@file:JvmName("Polygons")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.Polygon
import geo.render.Style
import geo.render.drawPolygon
import geo.projection.ProjectionFactory

/**
 * ## 03 - Polygons
 *
 * Demonstrates rendering Polygon geometries using drawPolygon().
 * Shows both exterior rings and interior holes (rings).
 *
 * ### Concepts
 * - Loading GeoJSON polygon data
 * - Rendering polygons with fill and stroke
 * - Handling interior rings (holes) in polygons
 * - Projecting polygon coordinates to screen space
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.PolygonsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load polygon data from GeoJSON
        val data = GeoJSON.load("examples/data/geo/sample.geojson")

        // Create a projection that fits the data to the window
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9
        )

        // Define polygon styling using the Style DSL
        val polygonStyle = Style {
            fill = ColorRGBa(0.2, 0.6, 0.3, 0.7)  // Green with transparency
            stroke = ColorRGBa(0.1, 0.3, 0.1)     // Dark green stroke
            strokeWeight = 1.0
        }

        extend {
            // Clear with light gray background
            drawer.clear(ColorRGBa(0.95, 0.95, 0.95))

            // Iterate over features and render Polygons
            data.features.forEach { feature ->
                if (feature.geometry is Polygon) {
                    val polygon = feature.geometry as Polygon

                    // Project exterior ring coordinates to screen space
                    val screenExterior = polygon.exterior.map { projection.toScreen(it.x, it.y) }

                    // Project interior rings (holes) to screen space
                    val screenInteriors = polygon.interiors.map { ring ->
                        ring.map { projection.toScreen(it.x, it.y) }
                    }

                    // Render the polygon with exterior and any interior holes
                    drawPolygon(drawer, screenExterior, screenInteriors, polygonStyle)
                }
            }
        }
    }
}
