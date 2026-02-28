@file:JvmName("LineStringColorAnim")
package examples.anim

import geo.geoSource
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.Style
import geo.animation.animator
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.loadFont

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val font = loadFont("data/fonts/default.otf", 22.0)
        val data = geoSource("examples/data/geo/catchment-topo.geojson")

        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 50.0,
            projection = ProjectionType.MERCATOR
        )

        // Property range for stroke weight
        val values = data.features.mapNotNull { it.doubleProperty("property_value") }
        val minValue = values.minOrNull() ?: 0.0
        val maxValue = values.maxOrNull() ?: 1.0
        val range = maxValue - minValue

        // Animate color progress
        val animator = animator()
        animator::progress.animate(1.0, 3000)

        val startColor = ColorRGBa(0.29, 0.56, 0.85)
        val endColor = ColorRGBa(0.91, 0.30, 0.24)

        extend {
            animator.updateAnimation()
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            val currentColor = mix(startColor, endColor, animator.progress)

            drawer.geo(data) {
                toScreen(projection)
                styleByFeature { feature ->
                    val v = feature.doubleProperty("property_value") ?: minValue
                    val t = if (range > 0.0) (v - minValue) / range else 0.0
                    Style(stroke = currentColor, strokeWeight = 0.05 + t * 0.45)
                }
            }

            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text("LineString Color Animation", 20.0, 30.0)
            drawer.text("Progress: ${(animator.progress * 100).toInt()}%", 20.0, 55.0)
        }
    }
}
