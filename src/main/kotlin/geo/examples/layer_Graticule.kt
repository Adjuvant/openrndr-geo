package geo.examples

import geo.GeoPackage
import geo.GeoJSON
import geo.Point
import geo.LineString
import geo.Polygon
import geo.render.Style
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.withAlpha
import geo.layer.generateGraticuleSource
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.fx.blend.Overlay
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.math.Vector2

/**
 * Layer Graticule Example
 *
 * Demonstrates graticule (latitude/longitude grid) generation and usage.
 * Shows different spacing values and their visual effect on reference grids.
 *
 * ## Graticule Spacing Values
 *
 * The spacing parameter controls the density of the grid lines:
 *
 * - **1.0 degree**: Detailed view showing individual degree lines.
 *   Best for zoomed-in regional views where precise location matters.
 *
 * - **5.0 degrees**: Regional view providing good balance between detail
 *   and clarity. Shows major latitude/longitude lines without clutter.
 *
 * - **10.0 degrees**: Continental view with clean reference lines.
 *   Ideal for global or large-scale visualizations.
 *
 * ## Visual Effect
 *
 * Graticule points are drawn as small markers at grid intersections.
 * Different spacing values create different visual densities:
 * - 1° spacing: Dense, precise reference
 * - 5° spacing: Balanced, readable reference
 * - 10° spacing: Sparse, clean reference
 *
 * ## Usage Pattern
 *
 * ```kotlin
 * // Generate graticule for data bounds with 5-degree spacing
 * val graticule = generateGraticuleSource(5.0, dataBounds)
 *
 * // Use in a layer
 * layer {
 *     draw {
 *         graticule.features.forEach { feature ->
 *             if (feature.geometry is Point) {
 *                 val screen = feature.geometry.toScreen(projection)
 *                 drawer.circle(Vector2(screen.x, screen.y), 2.0)
 *             }
 *         }
 *     }
 *     blend(Overlay())
 * }
 * ```
 *
 * Graticule provides essential lat/lng reference for orientation,
 * helping users understand the geographic context of the data.
 */
fun main() = application {
    configure {
        width = 1200
        height = 900
    }

    program {
        // Load geo data
        val data = try {
            GeoPackage.load("data/geo/ness-vectors.gpkg")
        } catch (e: Exception) {
            println("Could not load ness-vectors.gpkg: ${e.message}")
            println("Falling back to sample.geojson")
            GeoJSON.load("data/sample.geojson")
        }

        // Calculate bounds from data
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var count = 0

        data.features.take(1000).forEach { feature ->
            val bbox = feature.geometry.boundingBox
            if (!bbox.isEmpty()) {
                minX = kotlin.math.min(minX, bbox.minX)
                minY = kotlin.math.min(minY, bbox.minY)
                maxX = kotlin.math.max(maxX, bbox.maxX)
                maxY = kotlin.math.max(maxY, bbox.maxY)
                count++
            }
        }

        if (count == 0) {
            minX = -8.0; maxX = 2.0; minY = 50.0; maxY = 60.0
        }

        val dataBounds = geo.Bounds(minX, minY, maxX, maxY)
        println("Data bounds: $dataBounds")

        // Create projection
        val padding = 50.0
        val dataWidth = maxX - minX
        val dataHeight = maxY - minY
        val scaleX = (width - 2 * padding) / dataWidth
        val scaleY = (height - 2 * padding) / dataHeight
        val scale = kotlin.math.min(scaleX, scaleY)
        val center = Vector2((minX + maxX) / 2, (minY + maxY) / 2)

        val projection: GeoProjection = ProjectionFactory.mercator(
            width = width.toDouble(),
            height = height.toDouble(),
            center = center,
            scale = scale
        )

        // Create graticule sources with different spacing
        val graticule1deg = generateGraticuleSource(1.0, dataBounds)
        val graticule5deg = generateGraticuleSource(5.0, dataBounds)
        val graticule10deg = generateGraticuleSource(10.0, dataBounds)

        println("Graticule points: 1°=${graticule1deg.countFeatures()}, " +
                "5°=${graticule5deg.countFeatures()}, " +
                "10°=${graticule10deg.countFeatures()}")

        // Create composite showing different graticule densities side by side
        val composite = compose {
            // Clear background
            draw {
                drawer.clear(ColorRGBa(0.05, 0.05, 0.1, 1.0))  // Very dark blue
            }

            // Title
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Graticule Spacing Comparison", 20.0, 30.0)
                }
            }

            // Left third: 1-degree spacing (detailed)
            layer {
                draw {
                    // Label
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
                    drawer.text("1° spacing (detailed)", 20.0, 70.0)
                    
                    // Draw graticule points in left third
                    drawer.fill = ColorRGBa.YELLOW.withAlpha(0.6)
                    drawer.stroke = null
                    graticule1deg.features.forEach { feature ->
                        if (feature.geometry is Point) {
                            val screen = feature.geometry.toScreen(projection)
                            // Only draw in left third
                            if (screen.x < width / 3.0) {
                                drawer.circle(Vector2(screen.x, screen.y), 1.5)
                            }
                        }
                    }
                    
                    // Draw some data points
                    data.features.take(100).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                if (screen.x < width / 3.0) {
                                    drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
                                    drawer.circle(Vector2(screen.x, screen.y), 4.0)
                                }
                            }
                            else -> {}
                        }
                    }
                }
                blend(Overlay())
            }

            // Middle third: 5-degree spacing (balanced)
            layer {
                draw {
                    // Label
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
                    drawer.text("5° spacing (balanced)", width / 3.0 + 20.0, 70.0)
                    
                    // Draw graticule points in middle third
                    drawer.fill = ColorRGBa.GREEN.withAlpha(0.6)
                    drawer.stroke = null
                    graticule5deg.features.forEach { feature ->
                        if (feature.geometry is Point) {
                            val screen = feature.geometry.toScreen(projection)
                            // Only draw in middle third
                            if (screen.x >= width / 3.0 && screen.x < 2 * width / 3.0) {
                                drawer.circle(Vector2(screen.x, screen.y), 2.0)
                            }
                        }
                    }
                    
                    // Draw some data points
                    data.features.take(100).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                if (screen.x >= width / 3.0 && screen.x < 2 * width / 3.0) {
                                    drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
                                    drawer.circle(Vector2(screen.x, screen.y), 4.0)
                                }
                            }
                            else -> {}
                        }
                    }
                }
                blend(Overlay())
            }

            // Right third: 10-degree spacing (sparse)
            layer {
                draw {
                    // Label
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
                    drawer.text("10° spacing (sparse)", 2 * width / 3.0 + 20.0, 70.0)
                    
                    // Draw graticule points in right third
                    drawer.fill = ColorRGBa.MAGENTA.withAlpha(0.6)
                    drawer.stroke = null
                    graticule10deg.features.forEach { feature ->
                        if (feature.geometry is Point) {
                            val screen = feature.geometry.toScreen(projection)
                            // Only draw in right third
                            if (screen.x >= 2 * width / 3.0) {
                                drawer.circle(Vector2(screen.x, screen.y), 3.0)
                            }
                        }
                    }
                    
                    // Draw some data points
                    data.features.take(100).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                if (screen.x >= 2 * width / 3.0) {
                                    drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
                                    drawer.circle(Vector2(screen.x, screen.y), 4.0)
                                }
                            }
                            else -> {}
                        }
                    }
                }
                blend(Overlay())
            }

            // Draw all data as base layer
            layer {
                draw {
                    data.features.take(300).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is LineString -> {
                                val screenPoints = geometry.points.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawLineString(drawer, screenPoints, Style {
                                    stroke = ColorRGBa.WHITE.withAlpha(0.3)
                                    strokeWeight = 1.0
                                })
                            }
                            is Polygon -> {
                                val screenPoints = geometry.exterior.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawPolygon(drawer, screenPoints, Style {
                                    fill = ColorRGBa.BLUE.withAlpha(0.2)
                                    stroke = ColorRGBa.WHITE.withAlpha(0.2)
                                    strokeWeight = 0.5
                                })
                            }
                            else -> {}
                        }
                    }
                }
                blend(Multiply())
            }
        }

        extend {
            composite.draw(drawer)
        }
    }
}
