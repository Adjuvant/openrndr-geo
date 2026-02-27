@file:JvmName("Mercator")
package examples.proj

import geo.GeoJSON
import geo.LineString
import geo.Polygon
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.render.Style
import geo.render.drawLineString
import geo.render.drawPolygon
import org.openrndr.extra.color.presets.CORNFLOWER_BLUE
import org.openrndr.extra.color.presets.STEEL_BLUE

/**
 * ## 01 - Mercator Projection
 *
 * Demonstrates using ProjectionFactory.fitWorldMercator() for world-scale rendering
 * with the Web Mercator coordinate reference system.
 *
 * ### Concepts
 * - Web Mercator projection (EPSG:3857) for world-scale maps
 * - ProjectionFactory.fitWorldMercator() for fitting the world to viewport
 * - How Mercator projection distorts area at high latitudes
 * - Zoom level effects on visible area
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.proj.MercatorKt
 * ```
 */
fun main() = application {
    configure {
        width = 1000
        height = 600
    }

    program {
        // Create a world Mercator projection that fits the entire world
        val projection = ProjectionFactory.fitWorldMercator(
            width = width.toDouble(),
            height = height.toDouble()
        )

        // Load coastline data for visual reference
        val coastline = try {
            GeoJSON.load("examples/data/geo/coastline.geojson")
        } catch (e: Exception) {
            println("Could not load coastline: ${e.message}")
            null
        }

        extend {
            // Clear with dark ocean color
            drawer.clear(ColorRGBa(0.05, 0.1, 0.2))

            // Draw coastline if available
            coastline?.features?.forEach { feature ->
                when (val geometry = feature.geometry) {
                    is LineString -> {
                        val screenPoints = geometry.points.map { pt ->
                            projection.toScreen(pt.x, pt.y)
                        }
                        drawLineString(drawer, screenPoints, Style {
                            fill = null
                            stroke = ColorRGBa.CORNFLOWER_BLUE
                            strokeWeight = 1.0
                        })
                    }
                    is Polygon -> {
                        val screenPoints = geometry.exterior.map { pt ->
                            projection.toScreen(pt.x, pt.y)
                        }
                        drawPolygon(drawer, screenPoints, Style {
                            fill = ColorRGBa.STEEL_BLUE.withAlpha(0.3)
                            stroke = ColorRGBa.CORNFLOWER_BLUE
                            strokeWeight = 1.0
                        })
                    }
                }
            }

            // Draw equator line for reference
            drawer.stroke = ColorRGBa.RED.withAlpha(0.5)
            drawer.strokeWeight = 1.0
            drawer.lineSegment(0.0, height / 2.0, width.toDouble(), height / 2.0)

            // Draw prime meridian for reference
            drawer.lineSegment(width / 2.0, 0.0, width / 2.0, height.toDouble())

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Mercator Projection (Web Mercator EPSG:3857)", 20.0, 30.0)
            drawer.text("World fitted to viewport", 20.0, 50.0)
            drawer.text("Note: Distortion increases toward poles", 20.0, height - 30.0)
        }
    }
}
