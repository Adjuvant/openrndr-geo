package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.animation.GeoAnimator
import geo.animation.composition.GeoTimeline

/**
 * Simple Timeline Test
 *
 * Minimal example demonstrating GeoTimeline composition:
 * 1. Create timeline with 2 animations at different offsets
 * 2. Update timeline each frame
 * 3. Observe animations start at their offsets
 *
 * To run: ./gradlew run --main=geo.examples.TimelineSimple
 */
fun main() = application {
    configure {
        width = 600
        height = 400
    }

    program {
        val startTime = System.currentTimeMillis()

        // Simple timeline: circle at 0ms, square at 2500ms
        val timeline = GeoTimeline {
            val anim1 = GeoAnimator()
            add(anim1, offset = 0)

            val anim2 = GeoAnimator()
            add(anim2, offset = 2500)
        }

        extend {
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            timeline.update()

            val elapsed = (System.currentTimeMillis() - startTime)

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            // If offset 0 reached, circle shows
            if (elapsed >= 0) {
                drawer.fill = ColorRGBa.RED
                drawer.circle(200.0, 200.0, 50.0)
                drawer.text("Circle (offset=0)", 160.0, 280.0)
            }

            // If offset 2500ms reached, square shows
            if (elapsed >= 2500) {
                drawer.fill = ColorRGBa.BLUE
                drawer.rectangle(350.0, 150.0, 100.0, 100.0)
                drawer.text("Square (offset=2500)", 350.0, 280.0)
            }

            // Debug info
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Timeline: Circle at 0ms, Square at 2500ms", 20.0, 30.0)
            drawer.text("Elapsed: ${elapsed}ms", 20.0, 60.0)
        }
    }
}