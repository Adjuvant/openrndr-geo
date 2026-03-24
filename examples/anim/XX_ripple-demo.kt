@file:JvmName("RippleDemo")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.core.Point
import geo.core.Feature
import org.openrndr.draw.Drawer

/**
 * Ripple Effect Visualizer (Spatial Stagger)
 *
 * This example demonstrates distance-based stagger animation:
 * 1. Creates random features distributed across coordinate space
 * 2. Shows ripple effect radiating from origin point
 * 3. Features farther from origin start later (proportional to distance)
 *
 * To run this example:
 * ./gradlew run --main=geo.examples.RippleDemo
 *
 * Debug output shows:
 * - Origin point coordinates
 * - Distance calculations for sample features
 * - Current ripple state (wave radius, progress)
 */
fun main() = application {
    configure {
        width = 1000
        height = 800
    }

    program {
        // Create random features distributed across the space
        val randomFeatures = createRandomFeatures(count = 100)

        // Define ripple origin (center of screen)
        val origin = Vector2(500.0, 400.0)

        // Apply spatial stagger: features farther from origin animate later
        val rippleFeatures = randomFeatures.asSequence().map { feature ->
            val (centerX, centerY) = feature.geometry.boundingBox.center
            val featurePoint = Vector2(centerX, centerY)
            val distance = featurePoint.distanceTo(origin)
            val delayMs = (distance * .10).toLong()  // 2ms per pixel distance

            Triple(feature, distance, delayMs)
        }.toList()

        val startTime = System.currentTimeMillis()

        extend {
            drawer.clear(ColorRGBa.fromHex("#0a0a1a"))

            val currentTime = System.currentTimeMillis()
            val elapsedTime = (currentTime - startTime).toLong()

            val debugPanelX = 20.0
            var debugPanelY = 20.0
            val lineHeight = 24.0

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            drawer.text("RIPPLE EFFECT VISUALIZER (Spatial Stagger)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            drawer.text("Elapsed Time: ${elapsedTime}ms", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Origin: (${origin.x.toInt()}, ${origin.y.toInt()})", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Total Features: ${rippleFeatures.size}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            // Calculate current wave radius
            val waveRadiusMs = elapsedTime
            val waveRadius = waveRadiusMs / 2.0

            drawer.text("Animation Progress:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Wave Radius: ${waveRadius.toInt()} px", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Scale: 2.0ms per pixel distance", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            drawer.text("Visualization:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Red circle: Origin point", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Gray circles: Features not affected yet", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Green circles: Features inside active wave", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Cyan circles: Features completed animation", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            // Draw ripple wave
            drawer.stroke = ColorRGBa.fromHex("#ff6b6b").opacify(0.3)
            drawer.strokeWeight = 2.0
            drawer.circle(origin.x, origin.y, waveRadius)

            // Draw origin point
            drawer.fill = ColorRGBa.RED
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.circle(origin.x, origin.y, 8.0)

            drawer.fill = ColorRGBa.fromHex("#ff6b6b")
            drawer.stroke = null
            drawer.text("Origin", origin.x - 15.0, origin.y - 15.0)

            dividerRipple(drawer, debugPanelX - 10.0, debugPanelY - 10.0, 980.0)
            debugPanelY += lineHeight * 2.0

            drawer.text("Sample Feature Distances (First 10):", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Feature | Distance | Delay (ms) | Status", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("--------|----------|------------|--------", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            // Render features and collect stats
            var waitingCount = 0
            var animatingCount = 0
            var completedCount = 0

            rippleFeatures.take(10).forEachIndexed { index, (feature, distance, delayMs) ->
                val status = when {
                    elapsedTime < delayMs -> "WAITING"
                    elapsedTime < (delayMs + 1000) -> "ANIMATING"
                    else -> "COMPLETE"
                }

                drawer.text(
                    String.format("  %2d      | %8.1f  |   %4d     | %s",
                        index, distance, delayMs, status),
                    debugPanelX, debugPanelY
                )
                debugPanelY += lineHeight
            }

            // Visualize all features
            rippleFeatures.forEach { (feature, distance, delayMs) ->
                val (centerX, centerY) = feature.geometry.boundingBox.center
                val timeSinceStart = elapsedTime - delayMs

                when {
                    timeSinceStart < 0 -> {
                        // Not yet reached by wave
                        drawCircleRipple(drawer, centerX, centerY, 3.0, ColorRGBa.GRAY, 0.2)
                        waitingCount++
                    }
                    timeSinceStart < 1000 -> {
                        // Currently animating (1 second)
                        animatingCount++
                        val progress = timeSinceStart / 1000.0
                        val animatedSize = 3.0 + (progress * 10.0)
                        val color = ColorRGBa.GREEN.mix(ColorRGBa.WHITE, 0.3).opacify(progress)

                        drawCircleRipple(drawer, centerX, centerY, animatedSize, color, 1.0)

                        // Distance label for animating features
                        drawer.fill = ColorRGBa.WHITE.opacify(0.7)
                        drawer.stroke = null
                        drawer.text(distance.toInt().toString(), centerX - 10.0, centerY - 12.0)
                    }
                    else -> {
                        // Animation complete
                        completedCount++

                        drawCircleRipple(drawer, centerX, centerY, 13.0, ColorRGBa.CYAN.opacify(0.6), 1.0)

                        // Small distance label
                        drawer.fill = ColorRGBa.WHITE.opacify(0.5)
                        drawer.stroke = null
                        drawer.text(distance.toInt().toString(), centerX - 10.0, centerY - 12.0)
                    }
                }
            }

            drawer.text("Status: Waiting: $waitingCount | Animating: $animatingCount | Completed: $completedCount", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            if (completedCount == rippleFeatures.size) {
                drawer.text("✓ Ripple complete! All features animated.", debugPanelX, debugPanelY)
            }
        }
    }
}

fun createRandomFeatures(count: Int): List<Feature> {
    val features = mutableListOf<Feature>()

    for (i in 0 until count) {
        // Random position in visible area (100-900 x, 50-750 y)
        val x = Math.random() * 800.0 + 100.0
        val y = Math.random() * 700.0 + 50.0

        val point = Point(x, y)
        val feature = Feature(
            geometry = point,
            properties = mapOf("distance" to 0.0)
        )

        features.add(feature)
    }

    return features
}

private fun drawCircleRipple(
    drawer: Drawer,
    x: Double,
    y: Double,
    radius: Double,
    color: ColorRGBa,
    opacity: Double
) {
    drawer.fill = color.opacify(opacity)
    drawer.stroke = color.mix(ColorRGBa.BLACK, 0.2).opacify(opacity)
    drawer.strokeWeight = 1.0
    drawer.circle(x, y, radius)
}

private fun dividerRipple(drawer: Drawer, x: Double, y: Double, width: Double) {
    drawer.stroke = ColorRGBa.WHITE.opacify(0.3)
    drawer.strokeWeight = 2.0
    drawer.lineStrip(listOf(Vector2(x, y), Vector2(x + width, y)))
}