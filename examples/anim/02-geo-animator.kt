@file:JvmName("GeoAnimator")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.GeoJSON
import geo.Point
import geo.animation.animator
import geo.projection.ProjectionFactory
import geo.projection.toScreen
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import org.openrndr.extra.color.presets.CYAN

/**
 * ## 02 - Geo Animator
 *
 * Demonstrates animating geo-specific properties like coordinates and style attributes.
 * Shows how to animate geographic data visualization properties.
 *
 * ### Concepts
 * - Animating point position on map
 * - Animating style properties (size, color components)
 * - Coordinating animation with geographic data
 * - Projection-aware animation
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.anim.GeoAnimatorKt
 * ```
 */
fun main() = application {
    configure {
        width = 900
        height = 700
    }

    program {
        // Load geographic data
        val data = GeoJSON.load("examples/data/geo/populated_places.geojson")
        val features = data.features.toList().take(50)

        // Create projection that fits data to viewport
        val projection = ProjectionFactory.fitBounds(
            data.boundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 50.0
        )

        // Get the first point as animation target
        val targetFeature = features.firstOrNull()
        val targetPoint = if (targetFeature?.geometry is Point) {
            targetFeature.geometry as Point
        } else {
            Point(0.0, 0.0)
        }

        val animator = animator()

        // Animate from center of viewport to target location
        val targetScreen = projection.toScreen(targetPoint.x, targetPoint.y)
        val startX = width / 2.0
        val startY = height / 2.0

        // Animate position
        animator.apply {
            ::x.animate(targetScreen.x, 3000, Easing.CubicInOut)
            ::y.animate(targetScreen.y, 3000, Easing.CubicInOut)
            // Animate size: grow from small to normal
            ::pointSize.animate(12.0, 2500, Easing.CubicOut)
            // Animate opacity
            ::opacity.animate(1.0, 3000, Easing.CubicInOut)
            // Progress tracker
            ::progress.animate(1.0, 3000)
        }

        // Define initial and target colors
        val startColor = ColorRGBa.CYAN
        val targetColor = ColorRGBa.fromHex("#ff6b6b")

        extend {
            // Update animations
            animator.updateAnimation()

            // Calculate animated color (interpolate RGB components)
            val animatedColor = ColorRGBa(
                r = startColor.r + (targetColor.r - startColor.r) * animator.opacity,
                g = startColor.g + (targetColor.g - startColor.g) * animator.opacity,
                b = startColor.b + (targetColor.b - startColor.b) * animator.opacity,
                a = animator.opacity
            )

            // Clear background
            drawer.clear(ColorRGBa(0.05, 0.1, 0.2))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Geo Property Animation", 20.0, 30.0)
            drawer.text("Animating point from center to target location", 20.0, 50.0)
            drawer.text("Position: (${animator.x.toInt()}, ${animator.y.toInt()})", 20.0, 80.0)
            drawer.text("Size: ${animator.pointSize.toInt()}, Opacity: ${(animator.opacity * 100).toInt()}%", 20.0, 100.0)

            // Draw all other points (static)
            val staticStyle = Style {
                fill = ColorRGBa.WHITE.withAlpha(0.3)
                stroke = null
                size = 4.0
                shape = Shape.Circle
            }

            features.drop(1).forEach { feature ->
                if (feature.geometry is Point) {
                    val point = feature.geometry as Point
                    val screenPt = projection.toScreen(point.x, point.y)
                    drawPoint(drawer, screenPt, staticStyle)
                }
            }

            // Draw animated point with animated properties
            val animatedStyle = Style {
                fill = animatedColor
                stroke = ColorRGBa.WHITE
                strokeWeight = 2.0
                size = animator.pointSize
                shape = Shape.Circle
            }

            drawPoint(drawer, org.openrndr.math.Vector2(animator.x, animator.y), animatedStyle)

            // Draw connection line from start to current position
            drawer.stroke = ColorRGBa.WHITE.withAlpha(0.3)
            drawer.strokeWeight = 1.0
            drawer.lineSegment(startX, startY, animator.x, animator.y)

            // Show target marker
            drawer.fill = ColorRGBa.GREEN.withAlpha(0.5)
            drawer.stroke = ColorRGBa.GREEN
            drawer.strokeWeight = 1.0
            drawer.circle(targetScreen.x, targetScreen.y, 15.0)

            // Draw legend
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("White dots: Static locations", 600.0, 30.0)
            drawer.text("Cyan -> Red: Animated point", 600.0, 50.0)
            drawer.text("Green circle: Target location", 600.0, 70.0)

            if (animator.progress >= 1.0) {
                drawer.fill = ColorRGBa.GREEN
                drawer.text("Animation complete!", 350.0, 650.0)
            }
        }
    }
}
