package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import geo.GeoPackage
import geo.Point
import geo.LineString
import geo.Polygon
import geo.Bounds
import geo.render.drawPoint
import geo.render.drawLineString
import geo.render.drawPolygon
import geo.render.StyleDefaults
import geo.render.Style
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import geo.projection.ProjectionBNG
import org.openrndr.math.Vector2

/**
 * Live-coding rendering example using oliveProgram for hot reload.
 *
 * This example demonstrates how to use the TemplateLiveProgram pattern with
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
 * **Important**: All GeoProjection implementations expect lat/lng (WGS84) coordinates.
 * If data uses BNG (EPSG:27700), convert easting/northing to lat/lng first using ProjectionBNG.bngToLatLng()
 */
fun main() = application {
    configure {
        width = 1024
        height = 768
    }

    oliveProgram {
        // Load larger test dataset (GeoPackage has more features)
        val data = GeoPackage.load("data/geo/ness-vectors.gpkg")

        // Detect CRS and setup conversion
        val isBNG = data.crs == "EPSG:27700"  // British National Grid

// Calculate bounds from data to set proper zoom
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

        println("Native bounds: min($minX, $minY), max($maxX, $maxY) ($count features)")

        // If BNG, convert bounds to lat/lon for proper scaling
        val (minLL, maxLL) = if (isBNG) {
            val minLL = ProjectionBNG.bngToLatLng(Vector2(minX, minY))
            val maxLL = ProjectionBNG.bngToLatLng(Vector2(maxX, maxY))
            Pair(minLL, maxLL)
        } else {
            Pair(Vector2(minX, minY), Vector2(maxX, maxY))
        }

        // Now we have bounds in lat/lon degrees - calculate center and scale
        // minLL.x = minLongitude, minLL.y = minLatitude
        // maxLL.x = maxLongitude, maxLL.y = maxLatitude
        val centerLatLon = Vector2(
            (minLL.x + maxLL.x) / 2,  // center longitude
            (minLL.y + maxLL.y) / 2   // center latitude
        )
        val dataWidthLL = maxLL.x - minLL.x
        val dataHeightLL = maxLL.y - minLL.y

        // Calculate scale to fit data with padding
        val padding = 100.0  // Restore padding
        val scaleX = (width - 2 * padding) / dataWidthLL
        val scaleY = (height - 2 * padding) / dataHeightLL
        val autoScale = kotlin.math.min(scaleX, scaleY)

        // Use calculated scale directly (don't multiply)
        val finalScale = autoScale

        // Create projection with centered, zoomed view
        val projection = ProjectionFactory.mercator(
            width = width.toDouble(),
            height = height.toDouble(),
            center = centerLatLon,
            scale = finalScale*50 // Magic number!
        )

        println("Lat/lon center: lat=${centerLatLon.y}, lon=${centerLatLon.x}")
        println("Data extent: ${dataWidthLL}° x ${dataHeightLL}°")
        println("Auto scale: $autoScale, Final scale: $finalScale")

        // Count feature types
        val pointFeatures = data.features.count { it.geometry is Point }
        val lineFeatures = data.features.count { it.geometry is LineString }
        val polyFeatures = data.features.count { it.geometry is Polygon }
        println("Feature types: Points=$pointFeatures, Lines=$lineFeatures, Polygons=$polyFeatures")

        println("GeoPackage CRS: ${data.crs}")
        println("Is BNG data: $isBNG")
        println("Using projection: ${projection.javaClass.simpleName}")

// Helper: convert BNG coordinate to lat/lng if needed
        fun toLatLonIfBNG(easting: Double, northing: Double): Point {
            return if (isBNG) {
                val latLng = ProjectionBNG.bngToLatLng(Vector2(easting, northing))
                // latLng.x = longitude, latLng.y = latitude (from bngToLatLng return value)
                // Point expects x=longitude, y=latitude
                Point(latLng.x, latLng.y)
            } else {
                Point(easting, northing)
            }
        }

        extend {
            // Clear background with white background
            drawer.clear(ColorRGBa.WHITE)

            // Use darker colors for lines so they're visible
            val lineColor = ColorRGBa.BLACK
            val lineWidth = 3.0  // Thicker lines for visibility

            var renderedCount = 0
            var pointCount = 0
            var lineCount = 0
            var polyCount = 0

// Render all features with default styling
            data.features.forEach { feature ->
                val geometry = feature.geometry

                when (geometry) {
                    is Point -> {
                        // Convert BNG to lat/lng if needed, then project to screen
                        val latLonPoint = toLatLonIfBNG(geometry.x, geometry.y)
                        val screenPoint = latLonPoint.toScreen(projection)
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
                        // Convert BNG to lat/lng for all points, then project to screen
                        val screenPoints = geometry.points.map { point ->
                            toLatLonIfBNG(point.x, point.y).toScreen(projection)
                        }

                        // Debug: print first line's screen coordinates
//                        if (lineCount == 0) {
//                            println("=== FIRST LINE DEBUG ===")
//                            println("First line has ${screenPoints.size} points")
//                            screenPoints.take(3).forEach { pt ->
//                                println("  Screen: (${pt.x.toInt()}, ${pt.y.toInt()})")
//                            }
//                            println("First line center: (${(screenPoints.sumOf { it.x } / screenPoints.size).toInt()}, ${(screenPoints.sumOf { it.y } / screenPoints.size).toInt()})")
//                            println("Viewport: ${width}x${height}")
//                            println("Lines should be visible!")
//                            println("=== END DEBUG ===")
//                        }

                        drawLineString(drawer, screenPoints, Style {
                            stroke = lineColor
                            strokeWeight = lineWidth
                        })
                        lineCount++
                        renderedCount++
                    }

                    is Polygon -> {
                        // Convert BNG to lat/lng for exterior ring, then project to screen
                        val screenPoints = geometry.exterior.map { point ->
                            toLatLonIfBNG(point.x, point.y).toScreen(projection)
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

            // Debug: print render stats once per second (every 60 frames)
//            if (frameCount % 60 == 0) {
//                println("Frame $frameCount: rendered $renderedCount features (points=$pointCount, lines=$lineCount, polys=$polyCount)")
//            }

            // TEST: Draw the center point as a red marker to verify projection works
            val centerScreen = Point(centerLatLon.y, centerLatLon.x).toScreen(projection)
            if (frameCount == 0) {
                println("== DEBUG ==")
                println("Center on screen: (${centerScreen.x.toInt()}, ${centerScreen.y.toInt()})")
                println("Viewport: ${width}x${height}")
                println("Is center on screen? ${centerScreen.x >= 0 && centerScreen.x <= width && centerScreen.y >= 0 && centerScreen.y <= height}")
                println("Red circle at: (${centerScreen.x.toInt()}, ${centerScreen.y.toInt()})")
                println("== END DEBUG ==")
            }
            drawer.stroke = ColorRGBa.RED
            drawer.strokeWeight = 3.0
            drawer.circle(Vector2(centerScreen.x, centerScreen.y), 30.0)

            // TIPS FOR LIVE CODING:
            // - Try changing ColorRGBa.GRAY.shade(0.9) to other colors
            // - Modify Style { } blocks to customize appearance
            // - Add println() to debug feature properties
            // - Use feature.properties to drive visual encoding
        }
    }
}