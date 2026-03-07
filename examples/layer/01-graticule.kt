@file:JvmName("Graticule")
package examples.layer

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.*
import geo.layer.*
import geo.projection.*
import geo.render.*

/**
 * ## 01 - Graticule Layer
 *
 * Demonstrates creating and rendering a graticule - a grid of latitude/longitude lines
 * that provides geographic reference for map visualizations.
 *
 * ### Concepts
 * - Graticule generation using generateGraticuleSource()
 * - Three-line workflow with graticule layers
 * - Drawing geographic reference grids
 * - Configuring graticule spacing for different zoom levels
 * - Layer rendering with geographic data
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.layer.GraticuleKt
 * ```
 */
fun main() = application {
    configure {
        width = 1000
        height = 700
    }

    program {
        // Load coastline data using three-line workflow
        val coastline = try {
            loadGeo("examples/data/geo/coastline.geojson")
        } catch (e: Exception) {
            println("Could not load coastline: ${e.message}")
            null
        }

        // Define geographic bounds for the graticule
        val bounds = geo.Bounds(-180.0, -90.0, 180.0, 90.0)

        // Create projection
        val projection = ProjectionFactory.fitWorldMercator(
            width = width.toDouble(),
            height = height.toDouble()
        )

        // Generate graticule sources with different spacing
        val graticule10 = generateGraticuleSource(10.0, bounds)
        val graticule30 = generateGraticuleSource(30.0, bounds)

        println("Graticule (10deg): ${graticule10.countFeatures()} points")
        println("Graticule (30deg): ${graticule30.countFeatures()} points")

        extend {
            // Clear with ocean color
            drawer.clear(ColorRGBa(0.05, 0.1, 0.2))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Graticule Layer Example", 20.0, 30.0)
            drawer.text("Geographic reference grid (lat/lng lines)", 20.0, 50.0)

            // Draw coastline if available using inline style DSL
            coastline?.let { data ->
                drawer.geo(data, projection) {
                    fill = ColorRGBa.CORNFLOWER_BLUE.withAlpha(0.2)
                    stroke = ColorRGBa.CORNFLOWER_BLUE.withAlpha(0.5)
                    strokeWeight = 1.0
                }
            }

            // Draw 30-degree graticule (major lines)
            drawer.fill = null
            drawer.stroke = ColorRGBa.WHITE.withAlpha(0.4)
            drawer.strokeWeight = 1.5

            graticule30.features.forEach { feature ->
                if (feature.geometry is Point) {
                    val point = feature.geometry as Point
                    val isLatitude = point.x == 0.0 || point.x == 180.0 || point.x == -180.0
                    val isLongitude = point.y == 0.0 || point.y == 90.0 || point.y == -90.0

                    if (isLatitude || isLongitude) {
                        if (isLongitude) {
                            val top = toScreen(-90.0, point.x, projection)
                            val bottom = toScreen(90.0, point.x, projection)
                            drawer.lineSegment(top.x, top.y, bottom.x, bottom.y)
                        }
                        if (isLatitude) {
                            val left = toScreen(point.y, -180.0, projection)
                            val right = toScreen(point.y, 180.0, projection)
                            drawer.lineSegment(left.x, left.y, right.x, right.y)
                        }
                    }
                }
            }

            // Draw 10-degree graticule points (minor grid)
            drawer.fill = ColorRGBa.YELLOW.withAlpha(0.6)
            drawer.stroke = null

            graticule10.features.forEach { feature ->
                if (feature.geometry is Point) {
                    val point = feature.geometry as Point
                    val screen = point.toScreen(projection)
                    val isGridIntersection =
                        (point.x.toInt() % 10 == 0) && (point.y.toInt() % 10 == 0)

                    if (isGridIntersection && screen.x > 0 && screen.x < width &&
                        screen.y > 0 && screen.y < height) {
                        drawer.circle(screen.x, screen.y, 2.0)
                    }
                }
            }

            // Draw latitude/longitude labels
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.stroke = null

            listOf(-180, -120, -60, 0, 60, 120, 180).forEach { lng ->
                val screen = toScreen(0.0, lng.toDouble(), projection)
                if (screen.x > 50 && screen.x < width - 50) {
                    drawer.text("${lng}°", screen.x - 15, height - 20.0)
                }
            }

            listOf(-90, -60, -30, 0, 30, 60, 90).forEach { lat ->
                val screen = toScreen(lat.toDouble(), -175.0, projection)
                if (screen.y > 30 && screen.y < height - 50) {
                    drawer.text("${lat}°", 10.0, screen.y + 5)
                }
            }

            // Legend
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("White lines: 30-degree major grid", 700.0, 30.0)
            drawer.text("Yellow dots: 10-degree intersections", 700.0, 50.0)
        }
    }
}
