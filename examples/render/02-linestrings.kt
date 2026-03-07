@file:JvmName("Linestrings")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.*
import geo.render.*

/**
 * ## 02 - LineStrings
 *
 * Demonstrates rendering LineString geometries using the streamlined API.
 *
 * ### Concepts
 * - Loading data with loadGeo()
 * - Creating a projection with projectToFit()
 * - Rendering lines with inline style DSL
 * - Three-line workflow: loadGeo() → projectToFit() → drawer.geo()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.LinestringsKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Three-line workflow
        val data = loadGeo("examples/data/geo/rivers_lakes.geojson")
        val projection = data.projectToFit(width, height)

        extend {
            // Clear with white background
            drawer.clear(ColorRGBa.WHITE)

            // Draw linestrings with inline style DSL
            drawer.geo(data, projection) {
                stroke = ColorRGBa(0.0, 0.4, 0.8)  // Blue color
                strokeWeight = 1.5
                fill = null  // Lines don't need fill
            }
        }
    }
}
