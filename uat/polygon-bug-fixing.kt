@file:JvmName("PolygonBugFixing")
package uat

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.core.*
import geo.render.*

/**
 * UAT Test: Antimeridian Crossing Fixes
 * 
 * Purpose: Verify polygons crossing the antimeridian render correctly without:
 * - Infinite loops
 * - Incorrect splitting
 * - Interior rings outside exterior bounds
 * 
 * Data: data/geo/test-antimeridian-crossing.geojson
 * - wrapping-polygon: simple polygon crossing antimeridian
 * - pre-split-polygon: already at ±180 boundary
 * - wrapping-polygon-with-hole: polygon with hole near antimeridian
 * - multi-polygon-one-crossing: MultiPolygon with one crossing member
 * - control-no-crossing: control polygon (no crossing)
 * - fiji-style-wrap: Fiji-style short crossing
 */
fun main() = application {
    configure {
        width = 1200
        height = 800
        title = "UAT: Antimeridian Crossing (Phase 17)"
    }

    program {
        val antimeridianData = loadGeo("data/geo/test-antimeridian-crossing.geojson")
        val antimeridianProjection = antimeridianData.projectToFit(width, height)

        extend {
            drawer.clear(ColorRGBa(0.1, 0.1, 0.15))

            // Title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Antimeridian Crossing Test", 20.0, 30.0)
            drawer.text("Yellow polygons should render without errors or infinite loop", 20.0, 50.0)

            // Render test data
            drawer.geo(antimeridianData) {
                projection = antimeridianProjection
                fill = ColorRGBa.YELLOW
                stroke = ColorRGBa.BLUE
                strokeWeight = 1.0
            }

            // Legend
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Legend:", 20.0, height - 80.0)
            drawer.fill = ColorRGBa.YELLOW
            drawer.text("Yellow = Test polygons", 20.0, height - 60.0)
            drawer.stroke = ColorRGBa.BLUE
            drawer.strokeWeight = 1.0
            drawer.lineSegment(20.0, height - 45.0, 70.0, height - 45.0)
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Blue = Polygon outlines", 80.0, height - 50.0)
        }
    }
}
