package uat

import geo.GeoJSON
import geo.geoSource
import geo.render.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * UAT: Shape Cache Verification
 *
 * Visual sanity check for Phase 17 performance fixes:
 * - ViewportCache integration with Drawer.geo()
 * - styleByFeature support for property-based coloring
 * - Both standard and optimized rendering paths
 *
 * Expected behavior:
 * - Top half: Standard GeoSource rendering with elevation-based coloring
 * - Bottom half: OptimizedGeoSource rendering with same coloring
 * - Colors should vary by property_value (elevation) from blue (low) to red (high)
 * - Both views should render the same geographic data
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
        height = 900
        title = "UAT - Shape Cache Verification (Phase 17)"
    }

    program {
        // Load font for labels
        val font = loadFont("data/fonts/default.otf", 16.0)

        // Load data
        val geoJsonPath = "data/geo/catchment-topo.geojson"

        // Standard source (no optimization)
        val standardSource = geoSource(geoJsonPath)

        // Optimized source (batch projection enabled)
        val optimizedSource = GeoJSON.load(geoJsonPath, optimize = true)

        extend {
            drawer.clear(ColorRGBa.WHITE)

            // Title
            drawer.fontMap = font
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Phase 17 UAT: Shape Cache Verification", 20.0, 30.0)
            drawer.text("Top: Standard Path | Bottom: Optimized Path", 20.0, 50.0)

            // --- TOP HALF: Standard Path ---
            drawer.text("Standard Path (ViewportCache with Geometry keys)", 20.0, 70.0)

            // Render with styleByFeature based on elevation
            drawer.geo(standardSource) {
                // Style configuration using the actual API
                stroke = ColorRGBa.BLUE
                strokeWeight = 0.5

                // Note: styleByFeature is available via GeoRenderConfig
                // This drives per-feature styling based on properties
                styleByFeature = { feature ->
                    val elevation = feature.propertyAs<Double>("property_value") ?: 100.0
                    // Color gradient: blue (low) -> green -> yellow -> red (high)
                    val normalized = ((elevation - 10.0) / 290.0).coerceIn(0.0, 1.0)
                    val color = when {
                        normalized < 0.33 -> ColorRGBa.BLUE.mix(ColorRGBa.GREEN, normalized / 0.33)
                        normalized < 0.66 -> ColorRGBa.GREEN.mix(ColorRGBa.YELLOW, (normalized - 0.33) / 0.33)
                        else -> ColorRGBa.YELLOW.mix(ColorRGBa.RED, (normalized - 0.66) / 0.34)
                    }
                    Style(stroke = color, strokeWeight = 1.0)
                }
            }

            // --- BOTTOM HALF: Optimized Path ---
            // Label for bottom half
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Optimized Path (ViewportCache with OptimizedFeature keys)",
                20.0, (height / 2).toDouble() + 10.0)

            // Render optimized source
            // Note: OptimizedGeoSource currently doesn't support styleByFeature
            // due to type mismatch between Feature and OptimizedFeature
            // See plan 17-02 for gap closure details
            drawer.geo(optimizedSource) {
                stroke = ColorRGBa.GREEN
                strokeWeight = 0.5
            }

            // Legend
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Legend:", 20.0, height - 100.0)
            drawer.text("Blue = Low elevation (~10m)", 20.0, height - 80.0)
            drawer.text("Green = Mid-low elevation (~100m)", 20.0, height - 60.0)
            drawer.text("Yellow = Mid-high elevation (~200m)", 20.0, height - 40.0)
            drawer.text("Red = High elevation (~300m)", width / 2.0, height - 40.0)
            drawer.text("Note: Optimized path uses default styling (styleByFeature NYI)",
                20.0, height - 20.0)
        }
    }
}
