@file:JvmName("Multipolygons")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.*
import geo.render.*

/**
 * ## 04 - MultiPolygons
 *
 * Demonstrates rendering MultiPolygon geometries using the streamlined API.
 * MultiPolygons are used for collections of polygons, such as country boundaries
 * with separate land masses or islands.
 *
 * ### Concepts
 * - Loading data with loadGeo()
 * - Rendering MultiPolygons with inline style DSL
 * - Handling complex geometry collections
 * - Three-line workflow
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.MultipolygonsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Three-line workflow
        val data = loadGeo("examples/data/geo/ocean.geojson")
        val projection = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw multipolygons with inline style DSL
            drawer.geo(data, projection) {
                fill = ColorRGBa.DEEP_SKY_BLUE
                stroke = ColorRGBa(0.0, 0.3, 0.6)
                strokeWeight = 0.5
            }
        }
    }
}
