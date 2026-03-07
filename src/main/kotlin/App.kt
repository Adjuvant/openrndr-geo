/**
 * Canonical Entry Point for openrndr-geo
 * 
 * This is the primary entry point for the openrndr-geo library.
 * It demonstrates the essential workflow: load data, create a projection,
 * and render with basic styling.
 * 
 * For a more comprehensive example with animations, multiple datasets,
 * and advanced features, see TemplateProgram.kt.
 */

import geo.geoSource
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.Style
import geo.render.geo
import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        // Load a single GeoJSON dataset
        val data = geoSource("data/sample.geojson")
        
        // Create a projection that fits the data to the viewport
        val projection = ProjectionFactory.fitBounds(
            data.totalBoundingBox(),
            width.toDouble(), 
            height.toDouble(),
            padding = 20.0,
            projection = ProjectionType.MERCATOR
        )
        
        extend {
            // Clear background
            drawer.clear(ColorRGBa.BLACK)
            
            // Render with simple styling
            drawer.geo(data) {
                this.projection = projection
                styleByType = mapOf(
                    "Point" to Style(
                        fill = ColorRGBa.CYAN
                    )
                )
            }
        }
    }
}
