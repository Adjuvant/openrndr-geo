@file:JvmName("BasicAnimation")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.animation.animator

/**
 * ## 01 - Basic Animation
 *
 * Demonstrates basic property animation using OPENRNDR's Animatable with
 * the ::property.animate() syntax for smooth tweening.
 *
 * ### Concepts
 * - OPENRNDR Animatable for property-based animation
 * - ::property.animate() syntax for tweening
 * - Easing functions for smooth motion curves
 * - updateAnimation() per-frame callback
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.BasicAnimationKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val animator = animator()

        // Configure animations: animate x, y, and size properties
        // Using different easing functions for different effects
        animator.apply {
            // Animate position with CubicInOut for smooth start and stop
            ::x.animate(600.0, 3000, Easing.CubicInOut)
            ::y.animate(450.0, 3000, Easing.CubicInOut)
            // Animate size with CubicOut for natural "settling" effect
            ::size.animate(80.0, 2000, Easing.CubicOut)
            // Animation progress tracker
            ::progress.animate(1.0, 3000)
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
            drawer.text("size: ${animator.size.toInt()}", 20.0, 100.0)
            drawer.text("progress: ${(animator.progress * 100).toInt()}%", 20.0, 120.0)

            // Draw animated circle
            drawer.fill = ColorRGBa.fromHex("#ff6b6b")
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.circle(animator.x, animator.y, animator.size)

            // Draw trail effect
            drawer.fill = ColorRGBa.fromHex("#ff6b6b").withAlpha(0.3)
            drawer.stroke = null
            drawer.circle(animator.x - 20, animator.y - 20, animator.size * 0.7)
            drawer.circle(animator.x - 40, animator.y - 40, animator.size * 0.4)

            // Show easing info
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("x/y: CubicInOut (smooth start/stop)", 500.0, 30.0)
            drawer.text("size: CubicOut (natural settle)", 500.0, 50.0)

            // Show completion status
            if (animator.progress >= 1.0) {
                drawer.fill = ColorRGBa.GREEN
                drawer.text("Animation complete!", 300.0, 550.0)
            }
        }
    }
}
