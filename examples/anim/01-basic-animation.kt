@file:JvmName("BasicAnimation")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.animation.animator
import geo.render.withAlpha

/**
 * ## 01 - Basic Animation
 *
 * Demonstrates basic property animation using OPENRNDR's Animatable with
 * the ::property.animate() syntax for smooth tweening. Geo Animator is basically a
 * wrapper around OPENRNDR's Animatable that provides a more convenient API for
 * animating geometric properties.
 *
 * ### Concepts
 * - OPENRNDR Animatable for property-based animation
 * - ::property.animate() syntax for tweening
 * - Easing functions for smooth motion curves
 * - updateAnimation() per-frame callback
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.BasicAnimation
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val animator = animator()

        // Configure animations: animate x and y properties
        // Using different easing functions for different effects
        animator.apply {
            // Animate position with CubicInOut for smooth start and stop
            ::x.animate(600.0, 3000, Easing.CubicInOut)
            ::y.animate(450.0, 3000, Easing.CubicInOut)
        }

        extend {
            // CRITICAL: Update animations each frame
            animator.updateAnimation()

            // Clear with dark background
            drawer.clear(ColorRGBa(0.1, 0.1, 0.15))

            // Draw title and info
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Basic Property Animation", 20.0, 30.0)
            drawer.text("::property.animate(target, duration, easing)", 20.0, 50.0)
            drawer.text("x: ${animator.x.toInt()}, y: ${animator.y.toInt()}", 20.0, 80.0)

            // Draw animated circle
            drawer.fill = ColorRGBa.fromHex("#ff6b6b")
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.circle(animator.x, animator.y, 50.0)

            // Show easing info
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("x/y: CubicInOut (smooth start/stop)", 500.0, 30.0)

            // Show completion status when animation is done
            if (animator.x >= 599.0 && animator.y >= 449.0) {
                drawer.fill = ColorRGBa.GREEN
                drawer.text("Animation complete!", 300.0, 550.0)
            }
        }
    }
}
