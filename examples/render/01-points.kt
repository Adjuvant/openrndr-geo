@file:JvmName("Points")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.*
import geo.render.*

/**
 * ## 01 - Points
 *
 * Demonstrates rendering Point geometries using the streamlined API.
 *
 * ### Concepts
 * - Loading data with loadGeo()
 * - Creating a projection with projectToFit()
 * - Rendering points with inline style DSL
 * - Three-line workflow: loadGeo() → projectToFit() → drawer.geo()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.PointsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Three-line workflow
        val data = loadGeo("examples/data/geo/populated_places.geojson")
        val projection = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw points with inline style DSL
            drawer.geo(data, projection) {
                fill = ColorRGBa.ORANGE
                stroke = ColorRGBa.DODGER_BLUE
                strokeWeight = 1.0
                size = 8.0
                shape = Shape.Circle
            }
        }
    }
}
