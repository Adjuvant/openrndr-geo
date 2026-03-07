@file:JvmName("LineStringColorAnim")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.loadFont
import geo.*
import geo.animation.*
import geo.render.*
import geo.projection.*

/**
 * ## 06 - LineString Color Animation
 *
 * Demonstrates animating LineString colors based on feature properties
 * using the streamlined API.
 *
 * ### Concepts
 * - Property-based styling with animation
 * - Three-line workflow
 * - GeoRenderConfig for advanced styling
 * - Color interpolation with mix()
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.LineStringColorAnimKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val font = loadFont("data/fonts/default.otf", 22.0)
        
        // Three-line workflow
        val data = loadGeo("examples/data/geo/catchment-topo.geojson")
        val projection = data.projectToFit(width, height)

        // Property range for stroke weight
        val values = data.features.mapNotNull { it.doubleProperty("property_value") }
        val minValue = values.minOrNull() ?: 0.0
        val maxValue = values.maxOrNull() ?: 1.0
        val range = maxValue - minValue

        // Animate color progress
        val animator = animator()
        animator.apply {
            ::progress.animate(1.0, 3000)
        }

        val startColor = ColorRGBa(0.29, 0.56, 0.85)
        val endColor = ColorRGBa(0.91, 0.30, 0.24)

        extend {
            animator.updateAnimation()
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            val currentColor = mix(startColor, endColor, animator.progress)

            // Draw using explicit GeoRenderConfig block to avoid overload ambiguity
            drawer.geo(data, block = fun GeoRenderConfig.() {
                this.projection = projection
                styleByFeature = { feature: Feature ->
                    val v = feature.doubleProperty("property_value") ?: minValue
                    val t = if (range > 0.0) (v - minValue) / range else 0.0
                    Style(stroke = currentColor, strokeWeight = 0.05 + t * 0.45)
                }
            })

            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text("LineString Color Animation", 20.0, 30.0)
            drawer.text("Progress: ${(animator.progress * 100).toInt()}%", 20.0, 55.0)
        }
    }
}
