package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import geo.GeoPackage
import geo.Point
import geo.LineString
import geo.Polygon
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.Style
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import geo.projection.toWGS84
import geo.projection.materialize
import geo.projection.toWebMercator
import org.openrndr.math.Vector2

/**
 * Live-coding rendering example using oliveProgram for hot reload.
 *
 * This example demonstrates the new Phase 04.1 CRS-aware API:
 * - Load BNG data (EPSG:27700) and auto-transform to WGS84 (EPSG:4326)
 * - Use .materialize() to cache transformed features for render loop
 * - Simplified code: no manual CRS conversion needed
 *
 * ## New CRS Flow (Phase 04.1)
 * ```kotlin
 * // Old way: Manual conversion
 * val isBNG = data.crs == "EPSG:27700"
 * fun toLatLonIfBNG(x, y) = if (isBNG) ProjectionBNG.bngToLatLng(...) else Point(x, y)
 *
 * // New way: Fluent API
 * val data = GeoPackage.load("file.gpkg").toWGS84().materialize()
 * ```
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
 * - All without stopping and restarting the program
 */
fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    oliveProgram {
        // Load dataset with NEW CRS flow:
        // 1. Load BNG data from GeoPackage (EPSG:27700)
        // 2. Auto-transform to WGS84 (EPSG:4326) using .toWGS84()
        // 3. Materialize to cache features for render loop using .materialize()
        val data = GeoPackage.load("data/geo/ness-vectors.gpkg")
            .toWGS84()
            .materialize()

        println("Data CRS: ${data.crs}")
        println("Features loaded: ${data.listFeatures().size}")

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
            // Fallback to UK bounds
            minX = -8.0
            maxX = 2.0
            minY = 50.0
            maxY = 60.0
        }

        println("Bounds (WGS84): min($minX, $minY), max($maxX, $maxY) ($count features)")

        // Calculate center and scale for projection
        val centerLatLon = Vector2(
            (minX + maxX) / 2,  // center longitude
            (minY + maxY) / 2   // center latitude
        )
        val dataWidthLL = maxX - minX
        val dataHeightLL = maxY - minY

        // Calculate scale to fit data with padding
        val padding = 100.0
        val scaleX = (width - 2 * padding) / dataWidthLL
        val scaleY = (height - 2 * padding) / dataHeightLL
        val autoScale = kotlin.math.min(scaleX, scaleY)
        val finalScale = autoScale

        // Create projection with centered, zoomed view
        // Using zoomLevel: 0 = world, higher values = more zoomed
        // To convert from the old scale approach: zoomLevel = log2(scale / 256)
        val projection = ProjectionFactory.mercator(
            width = width.toDouble(),
            height = height.toDouble(),
            center = centerLatLon,
            zoomLevel = 2.0  // Starting zoom level for visualization
        )

        println("Center: lat=${centerLatLon.y}, lon=${centerLatLon.x}")
        println("Scale: $finalScale")

        // Count feature types
        val pointFeatures = data.features.count { it.geometry is Point }
        val lineFeatures = data.features.count { it.geometry is LineString }
        val polyFeatures = data.features.count { it.geometry is Polygon }
        println("Features: Points=$pointFeatures, Lines=$lineFeatures, Polygons=$polyFeatures")

        extend {
            // Clear background with white
            drawer.clear(ColorRGBa.WHITE)

            // Use darker colors for lines so they're visible
            val lineColor = ColorRGBa.BLACK
            val lineWidth = 3.0

            var renderedCount = 0
            var pointCount = 0
            var lineCount = 0
            var polyCount = 0

            // Render all features with default styling
            // NOTE: Data is already in WGS84, no manual conversion needed!
            data.features.forEach { feature ->
                val geometry = feature.geometry

                when (geometry) {
                    is Point -> {
                        // Data is WGS84, project directly to screen
                        val screenPoint = geometry.toScreen(projection)
                        drawPoint(drawer, screenPoint.x, screenPoint.y, Style {
                            fill = ColorRGBa.RED
                            stroke = ColorRGBa.BLACK
                            strokeWeight = 2.0
                            size = 8.0
                        })
                        pointCount++
                        renderedCount++
                    }

                    is LineString -> {
                        // Data is WGS84, project all points directly
                        val screenPoints = geometry.points.map { point ->
                            Point(point.x, point.y).toScreen(projection)
                        }

                        drawLineString(drawer, screenPoints, Style {
                            stroke = lineColor
                            strokeWeight = lineWidth
                        })
                        lineCount++
                        renderedCount++
                    }

                    is Polygon -> {
                        // Data is WGS84, project exterior ring directly
                        val screenPoints = geometry.exterior.map { point ->
                            Point(point.x, point.y).toScreen(projection)
                        }
                        drawPolygon(drawer, screenPoints, Style {
                            fill = ColorRGBa.BLUE
                            stroke = lineColor
                            strokeWeight = lineWidth
                        })
                        polyCount++
                        renderedCount++
                    }

                    else -> {
                        // Skip other geometry types
                    }
                }
            }

            // Draw center point marker at the center of the data bounds
            // centerLatLon is Vector2(lon, lat), project directly
            val centerScreen = projection.project(centerLatLon)
            drawer.fill = ColorRGBa.RED
            drawer.stroke = ColorRGBa.BLACK
            drawer.strokeWeight = 2.0
            drawer.circle(centerScreen, 8.0)

            // TIPS FOR LIVE CODING:
            // - Try changing ColorRGBa.RED to other colors
            // - Modify Style { } blocks to customize appearance
            // - Add println() to debug feature properties
            // - Use feature.properties to drive visual encoding
            // - Change the scale multiplier (currently 50) to zoom
        }
    }
}
