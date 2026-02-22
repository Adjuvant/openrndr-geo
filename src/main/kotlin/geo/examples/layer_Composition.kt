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
import org.openrndr.math.Vector2

/**
 * Layer Composition Example
 *
 * Demonstrates basic layer stacking using orx-compositor with CRS-aware data loading.
 * Shows how to:
 * - Load data and auto-transform to WGS84 using .toWGS84()
 * - Materialize for render optimization using .materialize()
 * - Create layers from different data sources
 * - Use graticule as a reference layer
 * - Apply blend modes to combine layers
 * - Project and render geo data
 *
 * ## CRS Flow (Phase 04.1)
 * ```kotlin
 * // Load and transform in one chain
 * val data = GeoPackage.load("data.gpkg")
 *     .toWGS84()       // Auto-transform to WGS84
 *     .materialize()   // Cache for render loop
 * ```
 *
 * ## Key Concepts
 *
 * **Layer Order Matters**: In orx-compositor, layers defined later in the `compose { }`
 * block are drawn on top of earlier layers. The last layer appears as the topmost element.
 *
 * **Blend Modes**: Use orx-fx blend modes (Multiply, Overlay, Screen, Add) to control
 * how layers combine. Each blend mode produces different visual effects:
 * - Multiply: Darkens, good for dark backgrounds
 * - Overlay: Increases contrast, preserves detail
 * - Screen: Lightens, good for highlights
 * - Add: Additive blend, creates glow effects
 *
 * **Graticule Layer**: A lat/lng grid provides visual reference for orientation.
 * Use [generateGraticuleSource] to create a GeoSource from grid points.
 *
 * ## Platform Compatibility
 *
 * **macOS Note:** This example uses orx-fx blend modes (Multiply, Overlay) which may
 * encounter shader compilation issues on macOS with the Metal backend. If you experience
 * freezing or shader warnings (textureSize0/textureSize1 uniforms), this is a known
 * orx-fx library issue, not a problem with this library.
 *
 * **Workaround:** On macOS, consider using simple alpha blending instead of orx-fx blend
 * modes, or test on Windows/Linux platforms where the Metal backend is not used.
 *
 * **Tracking:** See OpenRNDR/orx issues for Metal backend compatibility updates.
 */
fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    program {
        // Load geo data with CRS transformation:
        // GeoPackage data is BNG (EPSG:27700), transform to WGS84 (EPSG:4326)
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
            // Fallback bounds
            minX = -8.0; maxX = 2.0; minY = 50.0; maxY = 60.0
        }

        val dataBounds = geo.Bounds(minX, minY, maxX, maxY)
        println("Bounds (WGS84): $dataBounds")
        println("Features counted: $count")

        // Create projection (Mercator with padding)
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

        // Create graticule source (5-degree spacing)
        val graticuleSource = generateGraticuleSource(5.0, dataBounds)

        // Create compositor with layers
        // Layer order: background -> graticule -> data (top)
        val composite = compose {
            // Background layer
            draw {
                drawer.clear(ColorRGBa.BLACK)
            }

            // Graticule layer (middle) - just draw points as grid intersections
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE.withAlpha(0.3)
                    drawer.stroke = null
                    graticuleSource.features.forEach { feature ->
                        if (feature.geometry is Point) {
                            val screen = feature.geometry.toScreen(projection)
                            drawer.circle(Vector2(screen.x, screen.y), 2.0)
                        }
                    }
                }
                blend(Overlay())
            }

            // Data layer (top)
            layer {
                draw {
                    data.features.forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = geometry.toScreen(projection)
                                drawPoint(drawer, screen.x, screen.y, Style {
                                    fill = ColorRGBa.BLUE.withAlpha(0.5)
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
                                    stroke = ColorRGBa.BLUE.withAlpha(0.7)
                                    strokeWeight = 2.0
                                })
                            }
                            is Polygon -> {
                                val screenPoints = geometry.exterior.map { pt ->
                                    Point(pt.x, pt.y).toScreen(projection)
                                }
                                drawPolygon(drawer, screenPoints, Style {
                                    fill = ColorRGBa.BLUE.withAlpha(0.3)
                                    stroke = ColorRGBa.WHITE.withAlpha(0.5)
                                    strokeWeight = 1.0
                                })
                            }
                            else -> { }
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
