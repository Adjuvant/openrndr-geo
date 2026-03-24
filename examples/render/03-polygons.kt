@file:JvmName("Polygons")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.core.*
import geo.render.*

/**
 * ## 03 - Polygons
 *
 * Demonstrates rendering Polygon geometries using the streamlined API.
 * Shows both exterior rings and interior holes (rings).
 *
 * ### Concepts
 * - Loading data with loadGeo()
 * - Rendering polygons with inline style DSL
 * - Handling interior rings (holes) in polygons
 * - Three-line workflow
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.PolygonsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Three-line workflow, with some needless filter sugar.
        val data = loadGeo("examples/data/geo/polygonsWithHole.geojson")
            .filter { it.geometry is Polygon }
        val p = data.projectToFit(width, height)

        extend {
            // Clear with light gray background
            drawer.clear(ColorRGBa(0.95, 0.95, 0.95))

            // Draw polygons with inline style DSL
            // TODO fails to render holes in provided polygon features.
            drawer.geo(data) {
                projection = p
                fill = ColorRGBa(0.2, 0.6, 0.3, 0.6)  // Green with transparency
                stroke = ColorRGBa(0.1, 0.8, 0.1)     // Dark green stroke
                strokeWeight = 1.0
            }
        }
    }
}
