package geo.examples

import geo.GeoPackage
import geo.GeoJSON
import geo.GeoSource
import geo.Point
import geo.LineString
import geo.Polygon
import geo.render.Style
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.withAlpha
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.projection.toScreen
import geo.projection.toWGS84
import geo.projection.materialize
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.loadFont
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.blend.Overlay
import org.openrndr.extra.fx.blend.Screen
import org.openrndr.math.Vector2

/**
 * Layer Blend Modes Example
 *
 * Demonstrates all four recommended blend modes with visual comparison
 * and CRS-aware data loading.
 *
 * ## CRS Flow (Phase 04.1)
 * ```kotlin
 * val data = GeoPackage.load("data.gpkg")
 *     .toWGS84()       // Transform BNG to WGS84
 *     .materialize()   // Cache for rendering
 * ```
 *
 * ## Blend Modes for Geo Visualization
 *
 * ### Multiply (Best for: Dark backgrounds)
 * Darkens the composite result. Excellent for overlaying geo data on
 * dark basemaps or when you want features to darken the background.
 * Formula: Result = Source × Destination
 *
 * ### Overlay (Best for: Detail preservation)
 * Increases contrast while preserving details. Best blend mode for
 * layering geo features when you want both layers to remain visible.
 * Formula: Combines Multiply and Screen based on destination brightness
 *
 * ### Screen (Best for: Light backgrounds, highlights)
 * Lightens the composite result. Good for creating highlights or
 * lightening dark areas. Use sparingly with geo data.
 * Formula: Result = 1 - (1-Source) × (1-Destination)
 *
 * ### Add (Best for: Glow effects, artistic renders)
 * Additive blend that creates brightening effects. Can produce
 * glowing/halos effects when used with semi-transparent layers.
 * Formula: Result = Source + Destination
 *
 * ## Recommendations
 *
 * **For most geo work:** Start with Overlay - it preserves detail
 * from both layers while creating a cohesive composite.
 *
 * **For dark themes:** Multiply creates a natural "map at night" look.
 *
 * **For highlighting:** Use Add with low-opacity layers to create
 * subtle emphasis without overwhelming the base layer.
 *
 * ## Platform Compatibility
 *
 * **macOS Note:** This example demonstrates all four blend modes using orx-fx. The Metal
 * backend on macOS may have shader compilation issues with these blend modes. This is
 * a known orx-fx library issue, not specific to openrndr-geo.
 *
 * - **Affected:** macOS with Metal backend
 * - **Not affected:** Windows, Linux, macOS with OpenGL (if available)
 *
 * **Workaround:** Test blend mode examples on non-macOS platforms for full compatibility.
 */

// TODO promote helper function to API feature, quite reusable.
// Helper function to draw data in a specific quadrant
private fun drawDataQuadrant(
    drawer: Drawer,
    data: GeoSource,
    projection: GeoProjection,
    offsetX: Double,
    offsetY: Double,
    quadWidth: Double,
    quadHeight: Double
) {
    data.features.take(200).forEach { feature ->
        when (val geometry = feature.geometry) {
            is Point -> {
                val screen = geometry.toScreen(projection)
                // Apply quadrant offset
                val finalX = screen.x + offsetX
                val finalY = screen.y + offsetY
                // Only draw if in quadrant bounds
                if (finalX >= offsetX && finalX <= offsetX + quadWidth &&
                    finalY >= offsetY && finalY <= offsetY + quadHeight) {
                    drawPoint(drawer, finalX, finalY, Style {
                        fill = ColorRGBa(1.0, 0.5, 0.0).withAlpha(0.7)  // Orange
                        stroke = ColorRGBa.YELLOW
                        strokeWeight = 1.5
                        size = 6.0
                    })
                }
            }
            is LineString -> {
                val screenPoints = geometry.points.mapNotNull { pt ->
                    val screen = Point(pt.x, pt.y).toScreen(projection)
                    val finalX = screen.x + offsetX
                    val finalY = screen.y + offsetY
                    if (finalX >= offsetX && finalX <= offsetX + quadWidth &&
                        finalY >= offsetY && finalY <= offsetY + quadHeight) {
                        Vector2(finalX, finalY)
                    } else null
                }
                if (screenPoints.isNotEmpty()) {
                    drawLineString(drawer, screenPoints, Style {
                        stroke = ColorRGBa(1.0, 0.5, 0.0).withAlpha(0.8)
                        strokeWeight = 2.5
                    })
                }
            }
            is Polygon -> {
                val screenPoints = geometry.exterior.mapNotNull { pt ->
                    val screen = Point(pt.x, pt.y).toScreen(projection)
                    val finalX = screen.x + offsetX
                    val finalY = screen.y + offsetY
                    if (finalX >= offsetX && finalX <= offsetX + quadWidth &&
                        finalY >= offsetY && finalY <= offsetY + quadHeight) {
                        Vector2(finalX, finalY)
                    } else null
                }
                if (screenPoints.size >= 3) {
                    drawPolygon(drawer, screenPoints, Style {
                        fill = ColorRGBa(1.0, 0.5, 0.0).withAlpha(0.4)
                        stroke = ColorRGBa.YELLOW.withAlpha(0.6)
                        strokeWeight = 1.5
                    })
                }
            }
            else -> { }
        }
    }
}

fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    program {
        val font = loadFont("data/fonts/default.otf", 64.0)
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

        data.features.take(500).forEach { feature ->
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
        val projection = ProjectionFactory.fitBounds(data.totalBoundingBox(),
            width.toDouble(), height.toDouble(), padding = .80,
            projection = ProjectionType.MERCATOR)

        // Create 4-quadrant blend mode comparison
        val composite = compose {
            // Clear background
            draw {
                drawer.clear(ColorRGBa(0.1, 0.1, 0.2, 1.0))  // Dark blue-gray
            }

            // Label: Multiply (top-left)
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Multiply", 30.0, 40.0)
                }
            }
            layer {
                draw {
                    val sineShift = kotlin.math.sin(seconds / 180.0 * kotlin.math.PI) * (width / 2.0) * 0.5
                    drawDataQuadrant(drawer, data, projection, 0.0 + sineShift, 0.0,
                        width / 2.0, height / 2.0)
                }
                blend(Multiply())
            }

            // Label: Overlay (top-right)
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Overlay", width / 2.0 + 30.0, 40.0)
                }
            }
            layer {
                draw {
                    val sineShift = kotlin.math.sin(seconds / 180.0 * kotlin.math.PI) * (width / 2.0) * 0.5
                    drawDataQuadrant(drawer, data, projection, width / 2.0 - sineShift, 0.0,
                        width / 2.0, height / 2.0)
                }
                blend(Overlay())
            }

            // Label: Screen (bottom-left)
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Screen", 30.0, height / 2.0 + 40.0)
                }
            }
            layer {
                draw {
                    val sineShift = kotlin.math.sin(seconds / 180.0 * kotlin.math.PI) * (width / 2.0) * 0.5
                    drawDataQuadrant(drawer, data, projection, 0.0 + sineShift, height / 2.0,
                        width / 2.0, height / 2.0)
                }
                blend(Screen())
            }

            // Label: Add (bottom-right)
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Add", width / 2.0 + 30.0, height / 2.0 + 40.0)
                }
            }
            layer {
                draw {
                    val sineShift = kotlin.math.sin(seconds / 180.0 * kotlin.math.PI) * (width / 2.0) * 0.5
                    drawDataQuadrant(drawer, data, projection, width / 2.0 - sineShift, height / 2.0,
                        width / 2.0, height / 2.0)
                }
                blend(Add())
            }
        }

        extend {
            drawer.fontMap = font
            composite.draw(drawer)
        }
    }
}
