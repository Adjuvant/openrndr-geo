@file:JvmName("Timeline")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import org.openrndr.math.Vector2
import geo.animation.animator
import geo.render.withAlpha

/**
 * ## 03 - Timeline Animation
 *
 * Demonstrates timeline-based animation with staggered sequences.
 * Multiple shapes animate in sequence with delays.
 *
 * ### Concepts
 * - Sequencing animations with different start times
 * - Staggered animations for visual effect
 * - Easing functions for smooth motion
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.TimelineKt
 * ```
 */
fun main() = application {
    configure {
        width = 900
        height = 700
    }

    program {
        // Create separate animators for each shape to control timing independently
        val circleAnim = animator()
        val squareAnim = animator()
        val triangleAnim = animator()

        // Circle animation - starts at 0ms
        circleAnim.apply {
            ::x.animate(150.0, 1500, Easing.CubicInOut)
            ::y.animate(350.0, 1500, Easing.CubicInOut)
        }

        // Square animation - starts at 500ms (delayed)
        squareAnim.apply {
            ::x.animate(450.0, 1500, Easing.CubicInOut)
            ::y.animate(350.0, 1500, Easing.CubicInOut)
        }

        // Triangle animation - starts at 1000ms (more delayed)
        triangleAnim.apply {
            ::x.animate(750.0, 1500, Easing.CubicInOut)
            ::y.animate(350.0, 1500, Easing.CubicInOut)
        }

        // Master timeline progress tracker
        val timelineAnim = animator()
        timelineAnim.apply {
            ::progress.animate(1.0, 4000)
        }

        extend {
            // CRITICAL: Update animations each frame
            circleAnim.updateAnimation()
            squareAnim.updateAnimation()
            triangleAnim.updateAnimation()
            timelineAnim.updateAnimation()

            // Clear background
            drawer.clear(ColorRGBa(0.08, 0.08, 0.12))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Timeline Animation Sequencing", 20.0, 30.0)
            drawer.text("Each shape starts at a different offset: 0ms, 500ms, 1000ms", 20.0, 50.0)
            drawer.text("Total timeline: 4000ms", 20.0, 70.0)

            // Calculate elapsed time
            val elapsed = (timelineAnim.progress * 4000).toLong()

            // Circle (offset 0) - animate size based on elapsed time
            val circleSize = if (elapsed < 1500) (elapsed.toDouble() / 1500.0) * 60.0 else 60.0
            val circleOpacity = if (elapsed < 1500) elapsed.toDouble() / 1500.0 else 1.0
            if (circleOpacity > 0) {
                drawer.fill = ColorRGBa.RED.withAlpha(circleOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                drawer.circle(circleAnim.x, circleAnim.y, circleSize)
            }

            // Square (offset 500ms)
            val squareOpacity = if (elapsed >= 500) {
                if (elapsed < 2000) (elapsed - 500).toDouble() / 1500.0 else 1.0
            } else 0.0
            if (squareOpacity > 0) {
                drawer.fill = ColorRGBa.BLUE.withAlpha(squareOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                val half = 30.0
                drawer.rectangle(squareAnim.x - half, squareAnim.y - half, half * 2, half * 2)
            }

            // Triangle (offset 1000ms)
            val triangleOpacity = if (elapsed >= 1000) {
                if (elapsed < 2500) (elapsed - 1000).toDouble() / 1500.0 else 1.0
            } else 0.0
            if (triangleOpacity > 0) {
                drawer.fill = ColorRGBa.GREEN.withAlpha(triangleOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                val size = 60.0
                val halfSize = size / 2
                val top = Vector2(triangleAnim.x, triangleAnim.y - halfSize)
                val bottomLeft = Vector2(triangleAnim.x - halfSize * 0.866, triangleAnim.y + halfSize)
                val bottomRight = Vector2(triangleAnim.x + halfSize * 0.866, triangleAnim.y + halfSize)
                drawer.lineStrip(listOf(top, bottomLeft, bottomRight, top))
            }

            // Draw legend
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("Red Circle: starts at 0ms", 50.0, 150.0)
            drawer.text("Blue Square: starts at 500ms", 300.0, 150.0)
            drawer.text("Green Triangle: starts at 1000ms", 550.0, 150.0)

            // Show completion
            if (timelineAnim.progress >= 1.0) {
                drawer.fill = ColorRGBa.YELLOW
                drawer.text("Timeline complete!", 350.0, 550.0)
            }
        }
    }
}
