@file:JvmName("FeatureIteration")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.*
import geo.render.*
import geo.projection.*

/**
 * ## 08 - Feature Iteration
 *
 * Demonstrates feature iteration and rendering multiple geometry types
 * using the streamlined API with three-line workflow.
 *
 * ### Concepts
 * - Loading data with loadGeo()
 * - Iterating through features and handling multiple geometry types
 * - Using projectToFit() for automatic projection
 * - Rendering with inline style DSL
 * - Three-line workflow with manual iteration
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.FeatureIterationKt
 * ```
 */
fun main() = application {
    configure {
        width = 600
        height = 600
    }

    program {
        // Three-line workflow
        val data = loadGeo("examples/data/geo/coastline.geojson")
        val p = data.projectToFit(width, height)

        // Define styles using inline DSL pattern
        val pointStyle = Style {
            fill = ColorRGBa.RED
            stroke = ColorRGBa.BLACK
            size = 5.0
            shape = Shape.Circle
        }

        val lineStyle = Style {
            stroke = ColorRGBa.BLUE
            strokeWeight = 1.0
        }

        val polygonStyle = Style {
            fill = ColorRGBa.GREEN
            stroke = ColorRGBa.ORANGE
            strokeWeight = 1.0
        }

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Option 1: Use drawer.geo() for simple rendering
            drawer.geo(data) {
                projection = p
                stroke = ColorRGBa.CORNFLOWER_BLUE
                strokeWeight = 1.0
                fill = ColorRGBa.CORNFLOWER_BLUE.withAlpha(0.2)
            }

            // Option 2: Manual iteration for custom per-geometry handling
            // This shows how to access individual geometries when needed
            data.features.take(10).forEach { feature ->
                val geometry = feature.geometry
                when (geometry) {
                    is Point -> drawPoint(drawer, geometry.toScreen(p), pointStyle)
                    is LineString -> drawLineString(drawer, geometry.toScreen(p), lineStyle)
                    is Polygon -> drawPolygon(drawer, geometry.exteriorToScreen(p), polygonStyle)
                    is MultiPoint -> drawMultiPoint(drawer, geometry, p, pointStyle)
                    is MultiLineString -> drawMultiLineString(drawer, geometry, p, lineStyle)
                    is MultiPolygon -> drawMultiPolygon(drawer, geometry, p, polygonStyle)
                }
            }
        }
    }
}
