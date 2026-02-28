@file:JvmName("GeoAnimator")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.easing.Easing
import geo.GeoJSON
import geo.Point
import geo.animation.animator
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.Style
import geo.render.Shape
import geo.render.drawPoint
import geo.render.withAlpha

// CYAN is available via ColorRGBa directly

/**
 * ## 02 - Geo Animator
 *
 * Demonstrates animating geo-specific properties like coordinates.
 * Shows how to animate geographic data visualization on a map.
 *
 * ### Concepts
 * - Animating point position on map
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
            padding = 20.0,
            projection = ProjectionType.MERCATOR
        )

        // Get the first point as animation target
        val targetFeature = features.firstOrNull()
        val targetPoint = if (targetFeature?.geometry is Point) {
            targetFeature.geometry as Point
        } else {
            Point(0.0, 0.0)
        }

        val animator = animator()

        // Calculate target screen position
        val targetScreen = targetPoint.toScreen(projection)
        val originX = width / 2.0
        val originY = height / 2.0

        // Animate position from center to target
        animator.apply {
            ::x.animate(targetScreen.x, 3000, Easing.CubicInOut)
            ::y.animate(targetScreen.y, 3000, Easing.CubicInOut)
        }

        extend {
            // Update animations
            animator.updateAnimation()

            // Clear background
            drawer.clear(ColorRGBa(0.05, 0.1, 0.2))

            // Draw title
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Geo Property Animation", 20.0, 30.0)
            drawer.text("Animating point from center to target location", 20.0, 50.0)
            drawer.text("Position: (${animator.x.toInt()}, ${animator.y.toInt()})", 20.0, 80.0)

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
                    val screenPt = point.toScreen(projection)
                    drawPoint(drawer, screenPt, staticStyle)
                }
            }

            // Draw animated point
            val animatedStyle = Style {
                fill = ColorRGBa(0.0, 1.0, 1.0)  // Cyan color
                stroke = ColorRGBa.WHITE
                strokeWeight = 2.0
                size = 12.0
                shape = Shape.Circle
            }

            drawPoint(drawer, org.openrndr.math.Vector2(animator.x, animator.y), animatedStyle)

            // Draw the connection line from the start to the current position
            drawer.stroke = ColorRGBa.WHITE.withAlpha(0.3)
            drawer.strokeWeight = 1.0
            drawer.lineSegment(originX, originY, animator.x, animator.y)

            // Show target marker
            drawer.fill = ColorRGBa.GREEN.withAlpha(0.5)
            drawer.stroke = ColorRGBa.GREEN
            drawer.strokeWeight = 1.0
            drawer.circle(targetScreen.x, targetScreen.y, 15.0)

            // Draw legend
            drawer.fill = ColorRGBa.WHITE.withAlpha(0.7)
            drawer.text("White dots: Static locations", 600.0, 30.0)
            drawer.text("Cyan: Animated point", 600.0, 50.0)
            drawer.text("Green circle: Target location", 600.0, 70.0)

            // Show completion status
            if (animator.x >= targetScreen.x - 1 && animator.y >= targetScreen.y - 1) {
                drawer.fill = ColorRGBa.GREEN
                drawer.text("Animation complete!", 350.0, 650.0)
            }
        }
    }
}
