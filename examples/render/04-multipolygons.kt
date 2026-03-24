@file:JvmName("Multipolygons")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.core.*
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
 * ./gradlew run -Popenrndr.application=examples.render.Multipolygons
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Three-line workflow
        // TODO be useful to apply contraints on load, or in projeection step. I.e. before rendering.
        val data = loadGeo("examples/data/geo/polygonsWithHole.geojson")
        val p = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            //TODO Ocean will not render as when projection applied the latitudes in ocean data cause it to explode.
            // Draw multipolygons with inline style DSL
            drawer.geo(data) {
                projection = p
                fill = ColorRGBa.DEEP_SKY_BLUE
                stroke = ColorRGBa(0.0, 0.6, 0.6)
                strokeWeight = 0.5
            }
        }
    }
}
