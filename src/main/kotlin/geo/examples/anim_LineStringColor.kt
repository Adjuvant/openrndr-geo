package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.GeoJSON
import geo.LineString
import geo.MultiLineString
import geo.projection.ProjectionFactory
import geo.render.Style
import geo.render.drawLineString
import geo.animation.animator

/**
 * LineString Color Animation Demo
 *
 * Intermediate example demonstrating:
 * 1. Loading GeoJSON with bounding box for auto-framing
 * 2. Animating LineString stroke colors via GeoAnimator
 * 3. Using animation progress to interpolate colors
 *
 * To run: ./gradlew run --main=geo.examples.anim_LineStringColor
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load GeoJSON
        val geojson = GeoJSON.load("data/geo/catchment-topo.geojson")
        
        // Get bounding box for auto-framing
        val bbox = geojson.totalBoundingBox()
        
        // For equirectangular, we need to set center and scale appropriately
        // The center is the geographic center, scale controls zoom level
        val center = Vector2(bbox.center.first, bbox.center.second)
        
        // Calculate a reasonable scale based on the data extent
        // At scale=1, the full world (-180 to 180 lng, -90 to 90 lat) fills the smallest dimension
        // We want our data extent to fill about 80% of the screen
        val dataWidthDegrees = bbox.width
        val dataHeightDegrees = bbox.height
        val worldWidthDegrees = 360.0
        val worldHeightDegrees = 180.0
        
        // Calculate scale to fit data with padding
        val scaleX = worldWidthDegrees / dataWidthDegrees
        val worldHeightScaled = worldHeightDegrees * (width.toDouble() / worldWidthDegrees)
        val scaleY = worldHeightDegrees / dataHeightDegrees
        
        // Use the smaller scale to fit both dimensions, then adjust for aspect ratio
        val scale = minOf(scaleX, scaleY) * 0.8  // 80% to add padding

        // Create projection centered on data
        val projection = ProjectionFactory.equirectangular(
            width = width.toDouble(),
            height = height.toDouble(),
            center = center,
            scale = scale
        )

        // Collect features for rendering
        val features = geojson.listFeatures()

        // Get animator for color animation
        val animator = animator()

        // Animate progress from 0 to 1 over 3 seconds
        animator.apply {
            ::progress.animate(1.0, 3000)
        }

        // Define colors to interpolate between
        val startColor = ColorRGBa(0.29, 0.56, 0.85)  // Blue #4a90d9
        val endColor = ColorRGBa(0.91, 0.30, 0.24)    // Red #e74c3c

        extend {
            // Update animation each frame
            animator.updateAnimation()

            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            // Interpolate color based on animation progress
            val currentColor = mixColors(startColor, endColor, animator.progress)

            // Draw all LineString features with animated color
            features.forEach { feature ->
                when (val geom = feature.geometry) {
                    is LineString -> {
                        val screenCoords = geom.points.map { pt: Vector2 -> projection.project(pt) }
                        drawLineString(drawer, screenCoords, Style(stroke = currentColor, strokeWeight = 1.5))
                    }
                    is MultiLineString -> {
                        geom.lineStrings.forEach { line ->
                            val screenCoords = line.points.map { pt: Vector2 -> projection.project(pt) }
                            drawLineString(drawer, screenCoords, Style(stroke = currentColor, strokeWeight = 1.5))
                        }
                    }
                    else -> { /* Skip non-line geometries */ }
                }
            }

            // Info overlay
            drawer.fill = ColorRGBa.WHITE
            drawer.text("LineString Color Animation", 20.0, 30.0)
            drawer.text("Progress: ${(animator.progress * 100).toInt()}%", 20.0, 55.0)
            drawer.text("Features: ${features.size}", 20.0, 80.0)
        }
    }
}

/**
 * Linear color interpolation between two colors
 */
private fun mixColors(c1: ColorRGBa, c2: ColorRGBa, t: Double): ColorRGBa {
    return ColorRGBa(
        r = c1.r + (c2.r - c1.r) * t,
        g = c1.g + (c2.g - c1.g) * t,
        b = c1.b + (c2.b - c1.b) * t,
        alpha = c1.a + (c2.a - c1.a) * t
    )
}
