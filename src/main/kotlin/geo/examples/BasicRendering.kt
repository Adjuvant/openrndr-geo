package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.Point
import geo.LineString
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.render.writeLineString
import geo.projection.ProjectionFactory
import org.openrndr.math.Vector2

/**
 * Basic Rendering Example
 *
 * This example demonstrates the core geo rendering API by:
 * 1. Loading geo data from a GeoJSON file
 * 2. Creating a map projection for screen coordinates
 * 3. Rendering Point and LineString geometries with styling
 *
 * To run this example:
 * ./gradlew run --main=geo.examples.BasicRendering
 *
 * The sample data (data/sample.geojson) contains:
 * - A Point feature representing a location
 * - A LineString feature representing a path
 *
 * You can modify the Style properties to experiment with:
 * - Colors (fill, stroke)
 * - Point shapes (Circle, Square, Triangle)
 * - Line weights and styles
 */
fun main() = application {
    // Configure the application window
    configure {
        width = 800
        height = 600
    }

    program {
        // Load geo data from the sample GeoJSON file
        // GeoJSON.load() returns a GeoJSONSource which contains a sequence of features
        val data = GeoJSON.load("data/sample.geojson")

        // Create a Mercator projection that fits the viewport
        // This converts geographic coordinates (latitude/longitude) to screen coordinates (pixels)
        val projection = ProjectionFactory.mercator(width = width.toDouble(), height = height.toDouble(),
            center = Vector2(4.7, 52.1),  // Center on Netherlands (lng, lat)
            scale = 25000.0                  // Zoom in
        )

        // The extend block is called every frame (60 times per second by default)
        extend {
            // Clear the screen with white background
            drawer.clear(ColorRGBa.WHITE)

            // Iterate through all features in the dataset
            // Each feature has a geometry (Point, LineString, Polygon, etc.) and properties
            data.features.forEach { feature ->
                val geometry = feature.geometry

                // Handle each geometry type appropriately
                when (geometry) {
                    is Point -> {
                        // Project the geographic point to screen coordinates
                        // toScreen() applies the Mercator projection transformation
                        val screen = geometry.toScreen(projection)

                        // Draw the point with a red circular marker
                        drawPoint(drawer, screen.x, screen.y, Style {
                            size = 12.0                    // Marker diameter in pixels
                            shape = Shape.Circle           // Can be Circle, Square, or Triangle
                            fill = ColorRGBa.RED           // Fill color
                            stroke = ColorRGBa.BLACK       // Outline color
                            strokeWeight = 2.0             // Outline thickness
                        })
                    }

                    is LineString -> {
                        // Project all points in the line string to screen coordinates
                        // LineString stores points as Vector2 in geographic coordinates
                        val screenPoints = geometry.points.map { pt ->
                            // Convert each Vector2 geographic point to screen space
                            Point(pt.x, pt.y).toScreen(projection)
                        }

                        // Draw the line string with blue styling
                        writeLineString(drawer, screenPoints, Style {
                            stroke = ColorRGBa.BLUE          // Line color
                            strokeWeight = 3.0             // Line thickness
                            // lineCap and lineJoin control how line ends and corners look
                            // BUTT, ROUND, SQUARE for caps; MITER, ROUND, BEVEL for joins
                        })
                    }

                    // Other geometry types can be added here:
                    // is Polygon -> { /* Draw polygon with writePolygon() */ }
                    // is MultiPoint -> { /* Draw multiple points */ }
                    // is MultiLineString -> { /* Draw multiple lines */ }
                    // is MultiPolygon -> { /* Draw multiple polygons */ }
                    else -> {
                        // Skip geometry types not handled in this example
                    }
                }
            }
        }
    }
}
