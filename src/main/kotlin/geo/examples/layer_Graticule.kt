package geo.examples

import geo.GeoPackage
import geo.GeoJSON
import geo.Point
import geo.LineString
import geo.Polygon
import geo.animation.animator
import geo.render.Style
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.withAlpha
import geo.layer.generateGraticuleSource
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.projection.toScreen
import geo.projection.toWGS84
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blend.Overlay
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.blur.ApproximateGaussianBlur
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.sin

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
        var sigma_a = 5.0
        val animator = animator()

        // Animate over 30 seconds (30000ms) with ping-pong
        // Sigma ranges from 5.0 (blurry) to 0.1 (clear)
        animator.apply {
            ::progress.animate(1.0, 30000)
        }

        val font = loadFont("data/fonts/default.otf", 12.0)
        // Load geo data
        val data = try {
            // Need in
            GeoPackage.load("data/geo/ness-vectors.gpkg").toWGS84().materialize()
        } catch (e: Exception) {
            println("Could not load ness-vectors.gpkg: ${e.message}")
            println("Falling back to sample.geojson")
            GeoJSON.load("data/sample.geojson")
        }

        // Check bounds from data
        println("Data bounds: ${data.totalBoundingBox()} (from data)")

        // Create projection
        val projection = ProjectionFactory.fitBounds(data.totalBoundingBox(),
            width.toDouble(), height.toDouble(), padding = 50.0,
            projection = ProjectionType.MERCATOR)

        // Create graticule sources with different spacing
        val graticule1deg = generateGraticuleSource(1.0, data.totalBoundingBox())
        val graticule5deg = generateGraticuleSource(5.0, data.totalBoundingBox())
        val graticule10deg = generateGraticuleSource(10.0, data.totalBoundingBox())

        println("Graticule points: 1°=${graticule1deg.countFeatures()}, " +
                "5°=${graticule5deg.countFeatures()}, " +
                "10°=${graticule10deg.countFeatures()}")

        graticule1deg.features.forEach { feature -> println(feature) }
        // Create composite showing different graticule densities side by side
        val composite = compose {
            // Clear background
            draw {
                drawer.clear(ColorRGBa(0.05, 0.05, 0.1, 1.0))  // Very dark blue
            }

            // Title - BOTTOM
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Graticule Spacing Comparison", 20.0, 30.0)
                }
            }

            // Draw all data as base layer
            layer {
                blend(Add())
                draw {
                    data.features.take(300).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is LineString -> {
                                val screenPoints = geometry.points.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawLineString(drawer, screenPoints, Style {
                                    stroke = ColorRGBa.WHITE.withAlpha(0.9)
                                    strokeWeight = 1.0
                                })
                            }
                            is Polygon -> {
                                val screenPoints = geometry.exterior.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawPolygon(drawer, screenPoints, Style {
                                    fill = ColorRGBa.BLUE.withAlpha(0.9)
                                    stroke = ColorRGBa.WHITE.withAlpha(0.9)
                                    strokeWeight = 0.5
                                })
                            }
                            else -> {}
                        }
                    }
                }

                post(ApproximateGaussianBlur()){
                    window = 25
                    sigma = sigma_a
                }
            }

            // Left third: 1-degree spacing (detailed)
            layer {
                // blend(Overlay())
                draw {
                    val offsetX = 0.0
                    val offsetY = 80.0  // Leave room for title

                    // Label
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
                    drawer.text("1° spacing (detailed)", 20.0 + offsetX, 70.0 + offsetY)

                    // Draw graticule points in left third
                    drawer.fill = ColorRGBa.YELLOW.withAlpha(0.6)
                    drawer.stroke = null
                    graticule1deg.features.forEach { feature ->
                        if (feature.geometry is Point) {
                            val screen = feature.geometry.toScreen(projection)
                            // Apply offset to position in left third
                            val x = screen.x + offsetX
                            val y = screen.y + offsetY
                            drawer.circle(Vector2(x, y), 1.5)
                        }
                    }

                    // Draw some data points
                    data.features.take(100).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                val x = screen.x + offsetX
                                val y = screen.y + offsetY
                                drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
                                drawer.circle(Vector2(x, y), 4.0)
                            }
                            else -> {}
                        }
                    }
                }
            }
//
//            // Middle third: 5-degree spacing (balanced)
//            layer {
//                draw {
//                    // Label
//                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
//                    drawer.text("5° spacing (balanced)", width / 3.0 + 20.0, 70.0)
//
//                    // Draw graticule points in middle third
//                    drawer.fill = ColorRGBa.GREEN.withAlpha(0.6)
//                    drawer.stroke = null
//                    graticule5deg.features.forEach { feature ->
//                        if (feature.geometry is Point) {
//                            val screen = feature.geometry.toScreen(projection)
//                            // Only draw in middle third
//                            if (screen.x >= width / 3.0 && screen.x < 2 * width / 3.0) {
//                                drawer.circle(Vector2(screen.x, screen.y), 2.0)
//                            }
//                        }
//                    }
//
//                    // Draw some data points
//                    data.features.take(100).forEach { feature ->
//                        when (val geometry = feature.geometry) {
//                            is Point -> {
//                                val screen = geometry.toScreen(projection)
//                                if (screen.x >= width / 3.0 && screen.x < 2 * width / 3.0) {
//                                    drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
//                                    drawer.circle(Vector2(screen.x, screen.y), 4.0)
//                                }
//                            }
//                            else -> {}
//                        }
//                    }
//                }
//                blend(Overlay())
//            }
//
//            // Right third: 10-degree spacing (sparse)
//            layer {
//                draw {
//                    // Label
//                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
//                    drawer.text("10° spacing (sparse)", 2 * width / 3.0 + 20.0, 70.0)
//
//                    // Draw graticule points in right third
//                    drawer.fill = ColorRGBa.MAGENTA.withAlpha(0.6)
//                    drawer.stroke = null
//                    graticule10deg.features.forEach { feature ->
//                        if (feature.geometry is Point) {
//                            val screen = feature.geometry.toScreen(projection)
//                            // Only draw in right third
//                            if (screen.x >= 2 * width / 3.0) {
//                                drawer.circle(Vector2(screen.x, screen.y), 3.0)
//                            }
//                        }
//                    }
//
//                    // Draw some data points
//                    data.features.take(100).forEach { feature ->
//                        when (val geometry = feature.geometry) {
//                            is Point -> {
//                                val screen = geometry.toScreen(projection)
//                                if (screen.x >= 2 * width / 3.0) {
//                                    drawer.fill = ColorRGBa.CYAN.withAlpha(0.8)
//                                    drawer.circle(Vector2(screen.x, screen.y), 4.0)
//                                }
//                            }
//                            else -> {}
//                        }
//                    }
//                }
//                blend(Overlay())
//            }
        }

        extend {
            animator.updateAnimation()
            // Sine wave for ping-pong: goes 0→1→0 over the cycle
            val sineValue = (sin(animator.progress * PI - PI / 2) + 1) / 2

            // Map to sigma: 5.0 at start, 0.1 at midpoint, back to 5.0
            // Inverse: small sigma = clear, large sigma = blurry
            sigma_a = 5.0 - (sineValue * 4.9)  // 5.0 down to 0.1
            drawer.fontMap = font
            composite.draw(drawer)
        }
    }
}
