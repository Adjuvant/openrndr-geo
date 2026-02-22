package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.geojson.loadGeoJSON
import geo.projection.ProjectionFactory
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
        // Load GeoJSON and get bounding box for framing
        val geojson = loadGeoJSON("data/geo/catchment-topo.geojson")
        val bbox = geojson.bbox!!

        // Create projection to fit data to screen with padding
        val projection = ProjectionFactory.equirectangular(bbox, width, height, 0.1)

        // Get animator for color animation
        val animator = animator()

        // Animate progress from 0 to 1 over 3 seconds
        animator.apply {
            ::progress.animate(1.0, 3000)
        }

        // Define colors to interpolate between
        val startColor = ColorRGBa.fromHex("#4a90d9")  // Blue
        val endColor = ColorRGBa.fromHex("#e74c3c")    // Red

        extend {
            // Update animation each frame
            animator.updateAnimation()

            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            // Interpolate color based on animation progress
            val currentColor = startColor.mix(endColor, animator.progress)

            // Draw all LineString features with animated color
            geojson.features.forEach { feature ->
                when (val geom = feature.geometry) {
                    is geo.LineString -> {
                        val screenCoords = geom.coordinates.map { projection(it) }
                        drawer.stroke = currentColor
                        drawer.strokeWeight = 1.5
                        drawer.fill = null
                        drawer.lineSegments(screenCoords.map { listOf(it) })
                    }
                    is geo.MultiLineString -> {
                        geom.coordinates.forEach { line ->
                            val screenCoords = line.map { projection(it) }
                            drawer.stroke = currentColor
                            drawer.strokeWeight = 1.5
                            drawer.fill = null
                            drawer.lineSegments(screenCoords.map { listOf(it) })
                        }
                    }
                    else -> { /* Skip non-line geometries */ }
                }
            }

            // Info overlay
            drawer.fill = ColorRGBa.WHITE
            drawer.text("LineString Color Animation", 20.0, 30.0)
            drawer.text("Progress: ${(animator.progress * 100).toInt()}%", 20.0, 55.0)
            drawer.text("Features: ${geojson.features.count()}", 20.0, 80.0)
        }
    }
}

/**
 * Linear color interpolation between two colors
 */
private fun ColorRGBa.mix(other: ColorRGBa, t: Double): ColorRGBa {
    return ColorRGBa(
        r = r + (other.r - r) * t,
        g = g + (other.g - g) * t,
        b = b + (other.b - b) * t,
        a = a + (other.a - a) * t
    )
}
