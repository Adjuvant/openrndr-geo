package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.animation.animator

/**
 * Basic Animation Demo
 *
 * Minimal example demonstrating GeoAnimator and property tweening:
 * 1. Get animator via animator() extension
 * 2. Configure animations with ::property.animate()
 * 3. Call updateAnimation() each frame
 * 4. Read animated properties for rendering
 *
 * To run: ./gradlew run --main=geo.examples.anim_BasicAnimation
 */
fun main() = application {
    configure {
        width = 600
        height = 400
    }

    program {
        val animator = animator()

        // Configure animations: animate x and y properties
        animator.apply {
            ::x.animate(400.0, 2000, Easing.CubicInOut)
            ::y.animate(300.0, 2000, Easing.CubicOut)
        }

        extend {
            // CRITICAL: Update animations each frame
            animator.updateAnimation()

            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            // Draw animated circle using tweened properties
            drawer.fill = ColorRGBa.fromHex("#ff6b6b")
            drawer.stroke = ColorRGBa.WHITE
            drawer.circle(animator.x, animator.y, 50.0)

            // Show progress
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Basic Tweening", 20.0, 30.0)
            drawer.text("x: ${animator.x.toInt()}, y: ${animator.y.toInt()}", 20.0, 50.0)
            drawer.text("Use ::property.animate(target, duration, easing)", 20.0, 80.0)
        }
    }
}
