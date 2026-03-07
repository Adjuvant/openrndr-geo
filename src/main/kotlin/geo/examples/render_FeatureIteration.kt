package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.Point
import geo.LineString
import geo.MultiLineString
import geo.MultiPoint
import geo.MultiPolygon
import geo.Polygon
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.projection.ProjectionFactory
import geo.render.drawLineString
import geo.render.drawMultiLineString
import geo.render.drawMultiPoint
import geo.render.drawMultiPolygon
import geo.render.drawPolygon
import org.openrndr.extra.color.presets.ORANGE

/**
 * Feature Iteration Example
 *
 * This intermediate example demonstrates feature iteration and rendering by:
 * 1. Loading geo data from a GeoJSON file
 * 2. Creating a map projection for screen coordinates
 * 3. Iterating through features and handling multiple geometry types
 * 4. Rendering each geometry type with appropriate styling
 *
 * To run this example:
 * ./gradlew run --main=geo.examples.FeatureIteration
 *
 * The sample data (data/geo/coastline.geojson) contains:
 * - Point, LineString, and Polygon features
 *
 * You can modify the Style properties to experiment with:
 * - Colors (fill, stroke)
 * - Point shapes (Circle, Square, Triangle)
 * - Line weights and styles
 */
fun main() = application {
    // Configure the application window
    configure {
        width = 600
        height = 600
    }

    program {
        // Load geo data from the sample GeoJSON file
        // GeoJSON.load() returns a GeoJSONSource which contains a sequence of features
        val data = GeoJSON.load("data/geo/coastline.geojson")

        // Use fitWorldMercator for a projection that shows the whole world at zoom=0
        // zoom=0 means world fits in viewport, zoom=1 means 2x zoomed in, etc.
        val projection = ProjectionFactory.fitWorldMercator(width.toDouble(), height.toDouble())

        val pointStyle = Style {
            fill = ColorRGBa.RED
            stroke = ColorRGBa.BLACK
            size = 5.0
            shape = Shape.Circle
        }

        val lineStyle = Style {
            stroke = ColorRGBa.BLUE          // Line color
            strokeWeight = 1.0             // Line thickness
            // lineCap and lineJoin control how line ends and corners look
            // BUTT, ROUND, SQUARE for caps; MITER, ROUND, BEVEL for joins
        }

        val polygonStyle = Style {
            fill = ColorRGBa.GREEN
            stroke = ColorRGBa.ORANGE
            strokeWeight = 1.0
        }
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
                        // Draw the point with predefined style
                        drawPoint(drawer, geometry.toScreen(projection), pointStyle)
                    }

                    is LineString -> {
                        // Project all points in the line string to screen coordinates
                        // LineString stores points as Vector2 in geographic coordinates
                        // Draw the line string with blue styling
                        drawLineString(drawer, geometry.toScreen(projection), lineStyle)
                    }

                    is Polygon -> {
                        // Data is WGS84, project exterior ring directly
                        drawPolygon(drawer, geometry.exteriorToScreen(projection), polygonStyle)
                    }

                    is MultiPoint -> { /* Draw multiple points */
                        drawMultiPoint(drawer, geometry, projection, pointStyle)
                    }

                    is MultiLineString -> { /* Draw multiple lines */
                        drawMultiLineString(drawer, geometry, projection, lineStyle)
                    }
                    is MultiPolygon -> { /* Draw multiple polygons */
                        drawMultiPolygon(drawer, geometry, projection, polygonStyle)
                    }
                    // Other geometry types can be added here:
                    else -> {
                        // Skip geometry types not handled in this example
                    }
                }
            }
        }
    }
}
