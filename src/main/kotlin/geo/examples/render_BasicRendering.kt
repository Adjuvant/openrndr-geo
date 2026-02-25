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
import geo.projection.ProjectionConfig
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
        height = 400
    }

    program {
        // Load geo data from the sample GeoJSON file
        // GeoJSON.load() returns a GeoJSONSource which contains a sequence of features
        val data = GeoJSON.load("data/geo/coastline.geojson")
        // TODO Nice to have: join multiple imports into a single data monolith, then use features crawl as it.
        // Create a Mercator projection that fits the viewport
        // This converts geographic coordinates (latitude/longitude) to screen coordinates (pixels)
        val config = ProjectionConfig(
            width = width.toDouble(),
            height = height.toDouble(),
            center = null,
            scale = 10.0, // TODO Seems to act like 100% rather than intended 0-1 scale
            bounds = data.boundingBox()
        )
        // TODO scale fucked.
        // val projection = ProjectionFactory.mercator(config)
        // TODO Broken
//        val projection = ProjectionFactory.fitWorldMercator(width.toDouble(), height.toDouble())
        // TODO 50% broken, stuff in right scale but lines fuck up at poles
        val projection =
            ProjectionFactory.fitBounds(data.totalBoundingBox(), width.toDouble(), height.toDouble(), padding = 1.0)

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
        // TODO shouldn't we move everything to screen space by this stage? Or would that cause issues (data triangulation)?
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
                    // TODO fails on ocean as it covers beyond min max lat longs
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
