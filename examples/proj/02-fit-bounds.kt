@file:JvmName("FitBounds")
package examples.proj

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import geo.core.*
import geo.render.*

/**
 * ## 02 - Fit Bounds
 *
 * Demonstrates using projectToFit() to automatically fit geographic data
 * to the available screen space with the three-line workflow.
 *
 * ### Concepts
 * - Three-line workflow: loadGeo() → projectToFit() → drawer.geo()
 * - Automatic fitting without manual ProjectionFactory configuration
 * - Tight fit (100% of viewport)
 * - Equirectangular projection option
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.proj.FitBounds
 * ```
 */
fun main() = application {
    configure {
        width = 900
        height = 700
    }

    program {
        // Load data
        val data = loadGeo("examples/data/geo/populated_places.geojson")

        // Get bounds info for display
        val dataBounds = data.boundingBox()
        println("Data bounds: min(${dataBounds.minX}, ${dataBounds.minY}) max(${dataBounds.maxX}, ${dataBounds.maxY})")

        // Create projection using projectToFit
        val p = data.projectToFit(width, height)

        extend {
            // Clear background
            drawer.clear(ColorRGBa(0.1, 0.1, 0.15))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Fit Bounds with Three-line Workflow", 20.0, 30.0)
            drawer.text("Tight fit (100% of viewport w 20 pixel padding)", 20.0, 50.0)
            drawer.text("Points: ${data.countFeatures()}", 20.0, 70.0)

            // Render points with inline style DSL
            drawer.geo(data) {
                projection = p
                fill = ColorRGBa.ORANGE
                stroke = ColorRGBa(0.0, 1.0, 1.0)  // Cyan
                strokeWeight = 1.0
                size = 6.0
                shape = Shape.Circle
            }
        }
    }
}
