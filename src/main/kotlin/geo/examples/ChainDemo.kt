package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.animatable.easing.Easing
import geo.animation.GeoAnimator
import geo.animation.composition.animate

/**
 * Chain Composition Demo
 *
 * Demonstrates sequential animation via ChainedAnimationBuilder:
 * 1. Move circle from left to right (x: 100 -> 500)
 * 2. Then move up (y: 200 -> 100)
 *
 * Each step runs after the previous completes.
 *
 * To run: ./gradlew -Popenrndr.application=geo.examples.ChainDemoKt run
 */
fun main() = application {
    configure {
        width = 600
        height = 400
    }

    program {
        val shape = GeoAnimator().apply {
            x = 100.0
            y = 200.0
        }

        val chain = animate(shape) {
            ::x.animate(500.0, 1000, Easing.CubicOut)
        }.then {
            ::y.animate(100.0, 1000, Easing.CubicOut)
        }

        extend {
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            chain.update()

            // Draw the animated shape
            drawer.fill = ColorRGBa.fromHex("#ff6b6b")
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.circle(Vector2(shape.x, shape.y), 15.0)

            // Debug info
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Chain: Right then Up", 20.0, 30.0)
            drawer.text("X: ${shape.x.toInt()} (target: 500)", 20.0, 55.0)
            drawer.text("Y: ${shape.y.toInt()} (target: 100)", 20.0, 80.0)

            // Show completion
            if (chain.isComplete()) {
                drawer.fill = ColorRGBa.GREEN
                drawer.text("DONE!", 250.0, 350.0)
            }
        }
    }
}
