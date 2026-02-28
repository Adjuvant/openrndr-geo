@file:JvmName("StaggerAnimator")
package examples.anim

import geo.Point
import geo.animation.FeatureAnimator
import geo.animation.GeoAnimator
import geo.animation.staggerByIndex
import geo.geoSource
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import org.openrndr.animatable.easing.Easing
import org.openrndr.application

fun main() = application {
    configure {
        width = 700
        height = 700
    }

    program {
        // Load geographic data
        val data = geoSource("examples/data/geo/populated_places.geojson")
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 20.0,
            projection = ProjectionType.MERCATOR
        )

        // Create staggered animations using library FeatureAnimator
        // Each feature gets its own animator with staggered delay computed automatically
        val featureAnimators = data.features.take(150)
            .staggerByIndex(delayMs = 50) { GeoAnimator() }
            .map { fa ->
                fa.animator.apply {
                    size = 0.0
                    // Use fa.delay for non-blocking stagger delay
                    ::size.animate(5.0, 1000, Easing.CubicOut, fa.delay)
                }
                fa
            }
            .toList()

        extend {
            // Update all animators and draw circles with animated sizes
            featureAnimators.forEach { fa ->
                fa.animator.updateAnimation()
                val p = fa.feature.geometry as Point
                val ps = p.toScreen(projection)
                drawer.circle(ps.x, ps.y, fa.animator.size)
            }
        }
    }
}