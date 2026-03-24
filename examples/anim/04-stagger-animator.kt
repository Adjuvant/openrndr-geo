@file:JvmName("StaggerAnimator")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.core.*
import geo.animation.*
import geo.render.*

/**
 * ## 04 - Stagger Animator
 *
 * Demonstrates staggered animation using the FeatureAnimator and staggerByIndex
 * functions from the animation package with the streamlined API.
 *
 * ### Concepts
 * - FeatureAnimator for per-feature animations
 * - staggerByIndex for automatic delay calculation
 * - Three-line workflow for data loading
 * - Inline style DSL with animation
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.StaggerAnimatorKt
 * ```
 */
fun main() = application {
    configure {
        width = 700
        height = 700
    }

    program {
        // Three-line workflow
        val data = loadGeo("examples/data/geo/populated_places.geojson")
        val projection = data.projectToFit(width, height)

        // Create staggered animations using library FeatureAnimator
        val featureAnimators = data.features.take(150)
            .staggerByIndex(delayMs = 50) { GeoAnimator() }
            .map { fa ->
                fa.animator.apply {
                    size = 0.0
                    ::size.animate(5.0, 1000, Easing.CubicOut, fa.delay)
                }
                fa
            }
            .toList()

        extend {
            // Clear background
            drawer.clear(ColorRGBa(0.05, 0.1, 0.2))

            // Update all animators and draw circles with animated sizes
            featureAnimators.forEach { fa ->
                fa.animator.updateAnimation()
                val p = fa.feature.geometry as Point
                val ps = p.toScreen(projection)
                
                // Draw with inline style
                drawer.fill = ColorRGBa.CYAN
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 1.0
                drawer.circle(ps.x, ps.y, fa.animator.size)
            }
            
            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Staggered Animation", 20.0, 30.0)
            drawer.text("150 points with 50ms stagger delay", 20.0, 50.0)
        }
    }
}
