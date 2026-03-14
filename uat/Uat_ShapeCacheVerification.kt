package uat

import geo.GeoJSON
import geo.geoSource
import geo.render.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * UAT: Shape Cache Verification (SIMPLIFIED)
 *
 * Visual sanity check for Phase 17 performance fixes.
 * 
 * This version uses only the standard path to verify styleByFeature works correctly.
 * The optimized path has known issues with geometry type handling.
 *
 * Expected behavior:
 * - Renders catchment-topo.geojson with elevation-based coloring
 * - Colors vary by property_value from blue (low) to red (high)
 *
 * Data: data/geo/catchment-topo.geojson (contour lines with elevation data)
 * Property: property_value (elevation in meters, range ~10-300m)
 *
 * Usage: Run as OPENRNDR application
 * ./gradlew run -Popenrndr.application=uat.Uat_ShapeCacheVerificationKt
 */
fun main() = application {
    configure {
        width = 1200
        height = 700
        title = "UAT - Shape Cache Verification (Phase 17)"
    }

    program {
        // Load font for labels
        val font = loadFont("data/fonts/default.otf", 16.0)

        // Load data
        val geoJsonPath = "data/geo/catchment-topo.geojson"
        val source = geoSource(geoJsonPath)

        extend {
            drawer.clear(ColorRGBa.WHITE)

            // Title
            drawer.fontMap = font
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Phase 17 UAT: Shape Cache Verification", 20.0, 30.0)
            drawer.text("Standard Path with styleByFeature", 20.0, 50.0)

            // Render with styleByFeature based on elevation
            drawer.geo(source) {
                // Render with elevation-based coloring
                styleByFeature = { feature ->
                    val elevation = feature.propertyAs<Double>("property_value") ?: 100.0
                    
                    // Debug: print first few elevations
                    if (frameCount == 1 && elevation > 200.0) {
                        println("High elevation: $elevation")
                    }
                    
                    // Color gradient: blue (low) -> cyan -> green -> yellow -> red (high)
                    val normalized = ((elevation - 10.0) / 290.0).coerceIn(0.0, 1.0)
                    val color = when {
                        normalized < 0.25 -> ColorRGBa.BLUE.mix(ColorRGBa.CYAN, normalized / 0.25)
                        normalized < 0.5 -> ColorRGBa.CYAN.mix(ColorRGBa.GREEN, (normalized - 0.25) / 0.25)
                        normalized < 0.75 -> ColorRGBa.GREEN.mix(ColorRGBa.YELLOW, (normalized - 0.5) / 0.25)
                        else -> ColorRGBa.YELLOW.mix(ColorRGBa.RED, (normalized - 0.75) / 0.25)
                    }
                    
                    // Create style with explicit stroke
                    Style { 
                        stroke = color
                        strokeWeight = 1.5
                    }
                }
            }

            // Legend
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Legend (Elevation):", 20.0, height - 120.0)
            drawer.stroke = ColorRGBa.BLUE
            drawer.lineSegment(20.0, height - 100.0, 50.0, height - 100.0)
            drawer.text("~10m (low)", 60.0, height - 95.0)
            
            drawer.stroke = ColorRGBa.GREEN
            drawer.lineSegment(20.0, height - 80.0, 50.0, height - 80.0)
            drawer.text("~150m (mid)", 60.0, height - 75.0)
            
            drawer.stroke = ColorRGBa.RED
            drawer.lineSegment(20.0, height - 60.0, 50.0, height - 60.0)
            drawer.text("~300m (high)", 60.0, height - 55.0)
            
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Note: Optimized path disabled pending geometry type fix", 20.0, height - 30.0)
        }
    }
}
