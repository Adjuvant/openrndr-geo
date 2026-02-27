@file:JvmName("Timeline")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import org.openrndr.math.Vector2
import geo.animation.animator

/**
 * ## 03 - Timeline Animation
 *
 * Demonstrates timeline-based animation sequencing using multiple animated
 * properties with different delays to create coordinated sequences.
 *
 * ### Concepts
 * - Sequencing animations with different start times
 * - Keyframe-like animation using property chaining
 * - Timeline coordination for complex animations
 * - Staggered animations for visual effect
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
        val animator = animator()

        // Create a timeline with staggered animations
        // Each animation starts at a different offset to create a sequence

        // Shape 1: Circle - starts at 0ms
        animator.apply {
            ::circleX.animate(150.0, 1500, Easing.CubicInOut)
            ::circleY.animate(350.0, 1500, Easing.CubicInOut)
            ::circleSize.animate(60.0, 1500, Easing.CubicOut)
        }

        // Shape 2: Square - starts at 500ms (delayed)
        animator.apply {
            ::squareX.animate(450.0, 1500, Easing.CubicInOut)
            ::squareY.animate(350.0, 1500, Easing.CubicInOut)
            ::squareSize.animate(60.0, 1500, Easing.CubicOut)
        }

        // Shape 3: Triangle - starts at 1000ms (more delayed)
        animator.apply {
            ::triangleX.animate(750.0, 1500, Easing.CubicInOut)
            ::triangleY.animate(350.0, 1500, Easing.CubicInOut)
            ::triangleSize.animate(60.0, 1500, Easing.CubicOut)
        }

        // Master timeline progress
        animator.apply {
            ::progress.animate(1.0, 4000)
        }

        extend {
            // CRITICAL: Update animations each frame
            animator.updateAnimation()

            // Clear background
            drawer.clear(ColorRGBa(0.08, 0.08, 0.12))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Timeline Animation Sequencing", 20.0, 30.0)
            drawer.text("Each shape starts at a different offset: 0ms, 500ms, 1000ms", 20.0, 50.0)
            drawer.text("Total timeline: 4000ms", 20.0, 70.0)

            // Calculate visual timeline positions
            val timelineY = 600.0
            val timelineWidth = 700.0
            val startX = 100.0

            // Draw timeline bar background
            drawer.fill = ColorRGBa(0.2, 0.2, 0.25)
            drawer.stroke = ColorRGBa.WHITE.withAlpha(0.3)
            drawer.rectangle(startX, timelineY, timelineWidth, 20.0)

            // Draw progress
            val progressWidth = timelineWidth * (animator.progress.coerceAtMost(1.0))
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.3)
            drawer.stroke = null
            drawer.rectangle(startX, timelineY, progressWidth, 20.0)

            // Draw offset markers
            val markerTimes = listOf(0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000)
            drawer.stroke = ColorRGBa.WHITE.withAlpha(0.5)
            drawer.strokeWeight = 1.0
            markerTimes.forEach { time ->
                val x = startX + (time.toDouble() / 4000.0) * timelineWidth
                drawer.lineSegment(x, timelineY - 5, x, timelineY + 25)
            }

            // Draw shapes at animated positions
            val elapsed = (animator.progress * 4000).toLong()

            // Circle (offset 0)
            val circleOpacity = if (elapsed >= 0) {
                if (elapsed < 1500) elapsed.toDouble() / 1500.0 else 1.0
            } else 0.0

            if (circleOpacity > 0) {
                drawer.fill = ColorRGBa.RED.withAlpha(circleOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                drawer.circle(animator.circleX, animator.circleY, animator.circleSize)
                drawer.fill = ColorRGBa.WHITE.withAlpha(circleOpacity)
                drawer.text("0ms", animator.circleX - 15, animator.circleY + animator.circleSize + 20)
            }

            // Square (offset 500ms)
            val squareOpacity = if (elapsed >= 500) {
                if (elapsed < 2000) (elapsed - 500).toDouble() / 1500.0 else 1.0
            } else 0.0

            if (squareOpacity > 0) {
                drawer.fill = ColorRGBa.BLUE.withAlpha(squareOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                val half = animator.squareSize / 2
                drawer.rectangle(animator.squareX - half, animator.squareY - half, animator.squareSize, animator.squareSize)
                drawer.fill = ColorRGBa.WHITE.withAlpha(squareOpacity)
                drawer.text("500ms", animator.squareX - 20, animator.squareY + half + 20)
            }

            // Triangle (offset 1000ms)
            val triangleOpacity = if (elapsed >= 1000) {
                if (elapsed < 2500) (elapsed - 1000).toDouble() / 1500.0 else 1.0
            } else 0.0

            if (triangleOpacity > 0) {
                drawer.fill = ColorRGBa.GREEN.withAlpha(triangleOpacity)
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 2.0
                val size = animator.triangleSize
                val halfSize = size / 2
                val top = Vector2(animator.triangleX, animator.triangleY - halfSize)
                val bottomLeft = Vector2(animator.triangleX - halfSize * 0.866, animator.triangleY + halfSize)
                val bottomRight = Vector2(animator.triangleX + halfSize * 0.866, animator.triangleY + halfSize)
                drawer.lineStrip(listOf(top, bottomLeft, bottomRight, top))
                drawer.fill = ColorRGBa.WHITE.withAlpha(triangleOpacity)
                drawer.text("1000ms", animator.triangleX - 25, animator.triangleY + halfSize + 20)
            }

            // Draw legend
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("Red Circle: starts at 0ms", 50.0, 150.0)
            drawer.text("Blue Square: starts at 500ms", 300.0, 150.0)
            drawer.text("Green Triangle: starts at 1000ms", 550.0, 150.0)

            // Show completion
            if (animator.progress >= 1.0) {
                drawer.fill = ColorRGBa.YELLOW
                drawer.text("Timeline complete!", 350.0, 550.0)
            }
        }
    }
}
