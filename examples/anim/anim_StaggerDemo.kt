package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.Point
import geo.render.Shape
import geo.render.drawPoint
import geo.render.Style
import geo.Feature
import geo.animation.AnimationWrapper

/**
 * Stagger Effect Visualizer
 *
 * This example demonstrates index-based stagger animation:
 * 1. Creates a grid of sample features
 * 2. Shows staggered animation with computed delays
 * 3. Visualizes the cascade/ripple effect
 *
 * To run this example:
 * ./gradlew run --main=geo.examples.StaggerDemo
 *
 * Debug output shows:
 * - Number of features in grid
 * - Current animation state
 * - Index-based delay calculations
 */
fun main() = application {
    configure {
        width = 1000
        height = 800
    }

    program {
        // Create a grid of sample features for stagger demonstration
        val gridFeatures = createFeatureGrid(rows = 10, cols = 12)

        // Apply stagger effect: each feature starts 80ms after the previous one
        val staggeredFeatures = gridFeatures.asSequence()
            .withIndex()
            .map { (index, feature) ->
                AnimationWrapper(
                    feature = feature,
                    delay = index * 80L  // 80ms delay per feature
                )
            }
            .toList()

        val startTime = System.currentTimeMillis()

        extend {
            drawer.clear(ColorRGBa.fromHex("#0a0a1a"))

            val currentTime = System.currentTimeMillis()
            val elapsedTime = (currentTime - startTime)

            val debugPanelX = 20.0
            var debugPanelY = 20.0
            val lineHeight = 24.0

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            drawer.text("STAGGERED ANIMATION VISUALIZER", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            drawer.text("Elased Time: ${elapsedTime}ms", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Total Features: ${staggeredFeatures.size}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Delay per Feature: 80ms", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Total Animation Duration: ${staggeredFeatures.size * 80}ms", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            drawer.text("Visualization:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Gray circles: Features not yet visible", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Cyan circles: Animated (size + color)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Yellow glow: Currently animating", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            drawer.text("Animation Progress:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            var animatingCount = 0
            var completedCount = 0

            // Render each staggered feature
            staggeredFeatures.forEachIndexed { index, wrapper ->
                val timeSinceStart = elapsedTime - wrapper.delay

                when {
                    timeSinceStart < 0 -> {
                        // Not yet started - show placeholder
                        val (centerX, centerY) = wrapper.feature.geometry.boundingBox.center
                        drawCircle(drawer, centerX, centerY, 4.0, ColorRGBa.GRAY, 0.3)
                    }

                    timeSinceStart < 1000 -> {
                        // Currently animating (1 second animation)
                        animatingCount++
                        val progress = timeSinceStart / 1000.0
                        val animatedSize = 4.0 + (progress * 12.0)
                        val color = ColorRGBa.CYAN.mix(ColorRGBa.WHITE, 0.5).opacify(progress)

                        val (centerX, centerY) = wrapper.feature.geometry.boundingBox.center
                        drawCircle(drawer, centerX, centerY, animatedSize, color, 1.0)

                        // Add glow for animating features
                        drawer.fill = ColorRGBa.YELLOW.opacify(0.3)
                        drawer.stroke = null
                        drawer.circle(centerX, centerY, animatedSize * 1.5)
                    }

                    else -> {
                        // Animation complete
                        completedCount++

                        val (centerX, centerY) = wrapper.feature.geometry.boundingBox.center
                        drawCircle(drawer, centerX, centerY, 16.0, ColorRGBa.CYAN.opacify(0.7), 1.0)
                    }
                }
            }

            drawer.text(
                "  Animating: ${animatingCount} / Completed: ${completedCount} / Total: ${staggeredFeatures.size}",
                debugPanelX,
                debugPanelY
            )
            debugPanelY += lineHeight * 2

            divider(drawer, debugPanelX - 10.0, debugPanelY - 10.0, 980.0)
            debugPanelY += lineHeight * 1.5

            drawer.text("Stagger Pattern Details (First 10):", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Index | Delay (ms) | Animation Time", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("------|------------|----------------", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            staggeredFeatures.take(10).forEachIndexed { index, wrapper ->
                val status = when (val elapsed = elapsedTime - wrapper.delay) {
                    in 0..999 -> "ANIMATING"
                    in 1000..Long.MAX_VALUE -> "COMPLETE"
                    else -> "WAITING"
                }
                drawer.text(
                    "  $index  |   ${wrapper.delay}    | $status",
                    debugPanelX, debugPanelY
                )
                debugPanelY += lineHeight
            }

            if (completedCount == staggeredFeatures.size) {
                debugPanelY += lineHeight * 2
                drawer.text("✓ All features animated! Animation complete.", debugPanelX, debugPanelY)
            }
        }
    }
}

fun createFeatureGrid(rows: Int, cols: Int): List<Feature> {
    val features = mutableListOf<Feature>()

    // Create a grid of points covering the logical coordinate space
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val x = col * 80.0 + 150.0  // Offset from left edge
            val y = row * 70.0 + 50.0    // Offset from top edge

            val point = Point(x, y)
            val feature = Feature(
                geometry = point,
                properties = mapOf("row" to row, "col" to col)
            )

            features.add(feature)
        }
    }

    return features
}

fun drawCircle(
    drawer: org.openrndr.draw.Drawer,
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

fun divider(drawer: org.openrndr.draw.Drawer, x: Double, y: Double, width: Double) {
    drawer.stroke = ColorRGBa.WHITE.opacify(0.3)
    drawer.strokeWeight = 2.0
    drawer.lineStrip(listOf(Vector2(x, y), Vector2(x + width, y)))
}