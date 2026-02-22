package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import geo.GeoPackage
import geo.Point
import geo.LineString
import geo.Polygon
import geo.MultiPoint
import geo.MultiLineString
import geo.MultiPolygon
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.drawMultiPoint
import geo.render.drawMultiLineString
import geo.render.drawMultiPolygon
import geo.render.StyleDefaults
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import org.openrndr.math.Vector2

/**
 * Live-coding rendering example using oliveProgram for hot reload.
 *
 * This example demonstrates how to use TemplateLiveProgram pattern with
 * oliveProgram {} block. All code inside oliveProgram can be modified
 * while the program is running - changes reflect immediately without restart.
 *
 * ## Usage
 * 1. Run this program: ./gradlew run --main=geo.examples.LiveRendering
 * 2. Edit the code inside oliveProgram {} while it's running
 * 3. Save the file - changes appear instantly in the running window
 *
 * ## Hot Reload Capabilities
 * - Change colors, sizes, stroke weights
 * - Modify projection parameters (center, scale)
 * - Add/remove styling logic
 * - Experiment with different visual approaches
 * - All without stopping and restarting the program
 *
 * Modify styling and data processing below - changes reflect in real-time!
 */
fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    oliveProgram {
        // Load larger test dataset (GeoPackage has more features)
        // GeoPackage provides rich vector data with Points, LineStrings, and Polygons
        val data = GeoPackage.load("data/geo/ness-vectors.gpkg")

        // Create a Mercator projection fitted to our viewport
        // This transforms lat/lng coordinates to screen pixel coordinates
        val projection = ProjectionFactory.mercator(width = width.toDouble(), height = height.toDouble())

        extend {
            // Clear background with light gray
            drawer.clear(ColorRGBa.GRAY.shade(0.9))

            // Render all features with default styling
            // MODIFY HERE: Change styles, add conditional logic, experiment!
            data.features.forEach { feature ->
                val geometry = feature.geometry

                // Project geometry to screen coordinates and render based on type
                when (geometry) {
                    is Point -> {
                        // Project point to screen space
                        val screenPoint = geometry.toScreen(projection)
                        // Render with default point style
                        // MODIFY: Try different shapes (Circle, Square, Triangle)
                        // MODIFY: Change colors, sizes, add animation
                        drawPoint(drawer, screenPoint.x, screenPoint.y, StyleDefaults.defaultPointStyle)
                    }

                    is LineString -> {
                        // Project all points to screen space
                        val screenPoints = geometry.points.map { point ->
                            point.toScreen(projection)
                        }
                        // Render with default line style
                        // MODIFY: Change stroke color, weight, line caps/joins
                        drawLineString(drawer, screenPoints, StyleDefaults.defaultLineStyle)
                    }

                    is Polygon -> {
                        // Project exterior ring to screen space
                        val screenPoints = geometry.exterior.map { point ->
                            point.toScreen(projection)
                        }
                        // Render with default polygon style (outline only)
                        // MODIFY: Add fill with opacity, change stroke, animate
                        drawPolygon(drawer, screenPoints, StyleDefaults.defaultPolygonStyle)
                    }

                    is MultiPoint -> {
                        // Render all points in the multi-point
                        // MODIFY: Style points based on properties, use different shapes
                        drawMultiPoint(drawer, geometry, StyleDefaults.defaultPointStyle)
                    }

                    is MultiLineString -> {
                        // Render all line strings
                        // MODIFY: Style lines differently, animate stroke dash patterns
                        drawMultiLineString(drawer, geometry, StyleDefaults.defaultLineStyle)
                    }

                    is MultiPolygon -> {
                        // Render all polygons (exterior rings only in v1)
                        // MODIFY: Add fill with color coding based on properties
                        drawMultiPolygon(drawer, geometry, StyleDefaults.defaultPolygonStyle)
                    }
                }
            }

            // TIPS FOR LIVE CODING:
            // - Try changing ColorRGBa.GRAY.shade(0.9) to other colors
            // - Modify Style { } blocks to customize appearance
            // - Add println() to debug feature properties
            // - Use feature.properties to drive visual encoding
            // - Animate values using seconds or frameCount
            // - Experiment with different projections from ProjectionFactory
        }
    }
}
