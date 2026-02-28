@file:JvmName("StaggerAnimator")
package examples.anim

import geo.Point
import geo.animation.GeoAnimator
import geo.animation.staggerByIndex
import geo.geoSource
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import org.openrndr.animatable.easing.Easing
import org.openrndr.application

/**
 * Data class pairing a feature with its dedicated animator.
 *
 * Each feature gets its own GeoAnimator instance to enable independent
 * staggered animations. This avoids the singleton pattern limitation and
 * allows each feature to have its own animation state and timing.
 */
data class FeatureAnimator(val feature: geo.Feature, val animator: GeoAnimator)

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

        // Create staggered animations - each feature gets its own animator
        // The predelayInMs parameter handles the stagger timing (no Thread.sleep needed)
        val featureAnimators = data.features.take(50).staggerByIndex(50).map { wrapper ->
            val animator = GeoAnimator() // Create instance per feature (NOT singleton)
            animator.apply {
                size = 0.0
                // Use predelayInMs (4th parameter) for non-blocking stagger delay
                ::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)
            }
            FeatureAnimator(wrapper.feature, animator)
        }.toList()

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