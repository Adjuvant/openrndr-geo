@file:JvmName("ViewportCacheHoles")
package uat

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.*
import geo.render.*

/**
 * UAT Test: Viewport Cache Hole Rendering
 * 
 * Purpose: Verify that viewport caching preserves interior ring (hole) coordinates
 * and renders them correctly using compound { difference {} } boolean operations.
 * 
 * What this tests:
 * 1. projectGeometryToArray() includes interior coordinates for Polygon
 * 2. renderProjectedCoordinates() reconstructs ring boundaries from cached data
 * 3. writePolygonWithHoles() is called for cached rendering
 * 4. Holes render as transparent cutouts, not filled regions
 * 
 * Expected result:
 * - Polygons render with visible holes (transparent cutouts)
 * - Holes should be clearly visible as empty space within polygon fill
 * - No filled regions where holes should be
 * 
 * Test data: data/geo/polygonsWithHole.geojson
 * - Contains 6 polygons with various hole configurations
 * - Holes overlap between features to test compound difference
 * 
 * To run:
 * ./gradlew run -Popenrndr.application=uat.ViewportCacheHolesKt
 */
fun main() = application {
    configure {
        width = 800
        height = 600
        title = "UAT: Viewport Cache Hole Rendering"
    }

    program {
        // Load test data with holes
        val data = loadGeo("data/geo/polygonsWithHole.geojson")
        
        // Use Drawer.geo() which uses viewport caching internally
        // This exercises the projectGeometryToArray() and renderProjectedCoordinates() code paths
        val projection = data.projectToFit(width, height)

        extend {
            drawer.clear(ColorRGBa(0.1, 0.1, 0.15))

            // Render with viewport caching enabled (default behavior)
            // The cache should preserve interior ring coordinates
            drawer.geo(data) {
                this.projection = projection
                fill = ColorRGBa(0.3, 0.7, 0.9, 0.7)  // Cyan with transparency
                stroke = ColorRGBa(1.0, 1.0, 1.0)
                strokeWeight = 2.0
            }

            // Debug info
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Viewport Cache Hole Test", 20.0, 30.0)
            drawer.text("Polygons should have visible holes (transparent cutouts)", 20.0, 50.0)
            drawer.text("If holes are filled, viewport cache is losing interior coordinates", 20.0, 70.0)
        }
    }
}
