/**
 * Canonical Entry Point for openrndr-geo
 *
 * This is the primary entry point for the openrndr-geo library.
 * It demonstrates the essential three-line workflow:
 * 1. loadGeo() - auto-magic loading with caching
 * 2. projectToFit() - automatic projection fitting
 * 3. drawer.geo() - render with inline style DSL
 *
 * For a more comprehensive example with animations, multiple datasets,
 * and advanced features, see TemplateProgram.kt.
 */

import geo.*
import geo.render.*
import geo.projection.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        // Three-line workflow with auto-magic loading
        val data = loadGeo("data/sample.geojson")
        val topo = loadGeo("data/geo/catchment-topo.geojson")

        // Create projections that fit the data to the viewport
        val projection = data.projectToFit(width, height)
        val topoProj = topo.projectToFit(width, height)

        extend {
            // Clear background
            drawer.clear(ColorRGBa.BLACK)

            // Render topo with inline style DSL
            drawer.geo(topo, topoProj) {
                stroke = ColorRGBa.RED
                strokeWeight = 1.5
            }

            // Render data with inline style DSL
            drawer.geo(data, projection) {
                stroke = ColorRGBa.YELLOW
                fill = ColorRGBa.CYAN
                strokeWeight = 1.0
                size = 5.0
            }
        }
    }
}
