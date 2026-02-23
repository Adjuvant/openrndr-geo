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
import geo.projection.ProjectionType

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val geojson = GeoJSON.load("data/geo/catchment-topo.geojson")
        val projection = ProjectionFactory.fitBounds(geojson.boundingBox(),
            width.toDouble(), height.toDouble(), padding = 1.0,
            projection = ProjectionType.MERCATOR)

        // Collect features for rendering
        val features = geojson.listFeatures()

        // Extract min/max of property_value for linear mapping
        val propertyValues = features.mapNotNull { it.doubleProperty("property_value") }
        val minValue = propertyValues.minOrNull() ?: 0.0
        val maxValue = propertyValues.maxOrNull() ?: 1.0
        val valueRange = maxValue - minValue
        println("Value range: $minValue to $maxValue")
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
                // Grab the height from line and use for stroke later
                val propertyValue = feature.doubleProperty("property_value") ?: minValue
                val t = if (valueRange > 0.0) (propertyValue - minValue) / valueRange else 0.0
                var weight = 0.05 + t * (.5 - 0.05) // linear map to 0.05–0.5

                when (val geom = feature.geometry) {
                    is LineString -> {
                        val screenCoords = geom.points.map { pt: Vector2 -> projection.project(pt) }
                        drawLineString(drawer, screenCoords, Style(stroke = currentColor, strokeWeight = weight))
                    }
                    is MultiLineString -> {
                        geom.lineStrings.forEach { line ->
                            val screenCoords = line.points.map { pt: Vector2 -> projection.project(pt) }
                            drawLineString(drawer, screenCoords, Style(stroke = currentColor, strokeWeight = weight))
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
