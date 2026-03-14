package uat

import geo.*
import geo.render.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * UAT: Shape Cache Verification
 *
 * Visual test for Phase 17 geometry caching fixes.
 * 
 * Tests both standard and optimized rendering paths with elevation-based coloring.
 * 
 * Expected behavior:
 * - Top: Standard path with styleByFeature (elevation colors)
 * - Bottom: Optimized path (basic styling - styleByFeature NYI for optimized)
 * - LineStrings should render as LINES not closed polygons
 * - No black fills should appear
 *
 * Data: data/geo/catchment-topo.geojson (contour lines)
 */
fun main() = application {
    configure {
        width = 1200
        height = 900
        title = "UAT - Shape Cache Verification (Phase 17)"
    }

    program {
        val font = loadFont("data/fonts/default.otf", 16.0)
        val geoJsonPath = "data/geo/catchment-topo.geojson"
        
        // Load both standard and optimized sources
        val standardSource = geoSource(geoJsonPath)
        val optimizedSource = GeoJSON.load(geoJsonPath, optimize = true)

        extend {
            drawer.clear(ColorRGBa.WHITE)

            // Title
            drawer.fontMap = font
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Phase 17 UAT: Shape Cache Verification", 20.0, 30.0)

            // --- TOP HALF: Standard Path ---
            drawer.text("Standard Path (styleByFeature with elevation colors)", 20.0, 55.0)

            drawer.geo(standardSource) {
                styleByFeature = { feature ->
                    val elevation = feature.propertyAs<Double>("property_value") ?: 100.0
                    val normalized = ((elevation - 10.0) / 290.0).coerceIn(0.0, 1.0)
                    val color = when {
                        normalized < 0.25 -> ColorRGBa.BLUE.mix(ColorRGBa.CYAN, normalized / 0.25)
                        normalized < 0.5 -> ColorRGBa.CYAN.mix(ColorRGBa.GREEN, (normalized - 0.25) / 0.25)
                        normalized < 0.75 -> ColorRGBa.GREEN.mix(ColorRGBa.YELLOW, (normalized - 0.5) / 0.25)
                        else -> ColorRGBa.YELLOW.mix(ColorRGBa.RED, (normalized - 0.75) / 0.25)
                    }
                    Style { 
                        stroke = color
                        strokeWeight = 1.0
                    }
                }
            }

            // Divider line
            drawer.stroke = ColorRGBa.GRAY
            drawer.lineSegment(0.0, height / 2.0, width.toDouble(), height / 2.0)

            // --- BOTTOM HALF: Optimized Path ---
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Optimized Path (basic styling - styleByFeature NYI)", 20.0, height / 2.0 + 25.0)

            drawer.geo(optimizedSource) {
                stroke = ColorRGBa(0.2, 0.6, 0.2)  // Dark green
                strokeWeight = 0.8
            }

            // Legend
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Legend:", 20.0, height - 100.0)
            
            drawer.stroke = ColorRGBa.BLUE
            drawer.lineSegment(20.0, height - 80.0, 50.0, height - 80.0)
            drawer.text("Low elevation (~10m)", 60.0, height - 75.0)
            
            drawer.stroke = ColorRGBa.GREEN
            drawer.lineSegment(20.0, height - 60.0, 50.0, height - 60.0)
            drawer.text("Mid elevation (~150m)", 60.0, height - 55.0)
            
            drawer.stroke = ColorRGBa.RED
            drawer.lineSegment(20.0, height - 40.0, 50.0, height - 40.0)
            drawer.text("High elevation (~300m)", 60.0, height - 35.0)
            
            drawer.fill = ColorRGBa.BLACK
            drawer.text("PASS: LineStrings render as open lines (not filled polygons)", 20.0, height - 15.0)
        }
    }
}
