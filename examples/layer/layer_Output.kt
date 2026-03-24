package geo.examples

import geo.core.GeoPackage
import geo.core.GeoJSON
import geo.core.Point
import geo.core.LineString
import geo.core.Polygon
import geo.render.Style
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.withAlpha
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import geo.projection.toWGS84
import geo.projection.materialize
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.blend.Overlay
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Vector2

/**
 * Layer Output Example - Screenshot Capture
 *
 * Demonstrates screenshot capture workflow using OpenRNDR's Screenshots extension
 * with CRS-aware data loading.
 *
 * ## CRS Flow (Phase 04.1)
 * ```kotlin
 * val data = GeoPackage.load("data.gpkg")
 *     .toWGS84()
 *     .materialize()
 * ```
 *
 * ## Screenshot Workflow
 *
 * ### Key Press Capture (Spacebar)
 * Press SPACE to capture a screenshot at any time. Screenshots are automatically
 * saved to the `screenshots/` directory with timestamped filenames.
 *
 * ## File Naming Convention
 *
 * Screenshots are saved with descriptive names:
 * ```
 * layer-composition-00001.png
 * ```
 *
 * Format: `{name}-{frame}.png` where name is configured in the Screenshots extension.
 *
 * ## Screenshot Location
 *
 * Screenshots are saved to the `screenshots/` directory relative to the project root.
 * This directory is created automatically if it doesn't exist.
 *
 * ## Usage in Creative Coding
 *
 * The screenshot workflow follows the creative coding paradigm:
 * 1. Run the program
 * 2. Iterate and experiment visually
 * 3. Capture when you see something you like
 * 4. No separate batch infrastructure needed
 *
 * This preserves the fluid, exploratory nature of creative coding while
 * enabling output capture for documentation or further processing.
 */
fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    program {
        // Load geo data with CRS transformation
        val data = try {
            GeoPackage.load("data/geo/ness-vectors.gpkg")
                .toWGS84()
                .materialize()
        } catch (e: Exception) {
            println("Could not load ness-vectors.gpkg: ${e.message}")
            println("Falling back to sample.geojson")
            GeoJSON.load("data/sample.geojson")
                .materialize()
        }

        println("Data CRS: ${data.crs}")
        println("Features: ${data.listFeatures().size}")

        // Calculate bounds from transformed WGS84 data
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

        // Create projection
        val padding = 50.0
        val dataWidth = maxX - minX
        val dataHeight = maxY - minY
        val scaleX = (width - 2 * padding) / dataWidth
        val scaleY = (height - 2 * padding) / dataHeight
        val scale = kotlin.math.min(scaleX, scaleY)
        val center = Vector2((minX + maxX) / 2, (minY + maxY) / 2)
        
        // Convert scale to zoomLevel: zoom = log2(scale / 256)
        val zoomLevel = kotlin.math.log2(scale / 256.0)

        val projection: GeoProjection = ProjectionFactory.mercator(
            width = width.toDouble(),
            height = height.toDouble(),
            center = center,
            zoomLevel = zoomLevel
        )

        // Frame counter for animation
        var frameCount = 0

        // Create composite with layered data
        val composite = compose {
            // Background layer
            draw {
                drawer.clear(ColorRGBa.BLACK)
            }

            // Base data layer
            layer {
                draw {
                    data.features.take(400).forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                drawPoint(drawer, screen.x, screen.y, Style {
                                    fill = ColorRGBa.CYAN.withAlpha(0.6)
                                    stroke = ColorRGBa.WHITE
                                    strokeWeight = 1.0
                                    size = 5.0
                                })
                            }
                            is LineString -> {
                                val screenPoints = geometry.points.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawLineString(drawer, screenPoints, Style {
                                    stroke = ColorRGBa.WHITE.withAlpha(0.5)
                                    strokeWeight = 1.5
                                })
                            }
                            is Polygon -> {
                                val screenPoints = geometry.exterior.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawPolygon(drawer, screenPoints, Style {
                                    fill = ColorRGBa.BLUE.withAlpha(0.3)
                                    stroke = ColorRGBa.CYAN.withAlpha(0.4)
                                    strokeWeight = 1.0
                                })
                            }
                            else -> { }
                        }
                    }
                }
                blend(Multiply())
            }

            // Highlight layer (animated)
            layer {
                draw {
                    // Draw a pulsing highlight circle
                    val pulse = kotlin.math.sin(frameCount.toDouble() * 0.05) * 0.5 + 0.5
                    val radius = 100.0 + pulse * 50.0
                    drawer.fill = ColorRGBa.MAGENTA.withAlpha(0.2 * pulse)
                    drawer.stroke = ColorRGBa.MAGENTA.withAlpha(0.5 * pulse)
                    drawer.strokeWeight = 2.0
                    drawer.circle(Vector2(width / 2.0, height / 2.0), radius)
                }
                blend(Overlay())
            }

            // UI Layer (labels and status)
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE

                    // Title
                    drawer.text("Layer Output Example - Screenshot Demo", 20.0, 30.0)

                    // Instructions
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
                    drawer.text("Press SPACE to capture screenshot", 20.0, 55.0)
                    drawer.text("Screenshots saved to: screenshots/", 20.0, 80.0)
                }
            }
        }

        // Use native Screenshots extension for screenshot capture
        extend(Screenshots()) {
            // Space key triggers screenshot
            key = " "
            // Save to screenshots folder
            folder = "screenshots"
            // Custom filename with frame number
            name = "layer-composition-{frame}"
        }

        extend {
            composite.draw(drawer)
            frameCount++
        }
    }
}
