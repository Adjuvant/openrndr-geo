@file:JvmName("TimelineDemo")
package examples.anim

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.animation.*
import geo.animation.composition.GeoTimeline
import org.openrndr.draw.Drawer

/**
 * Timeline Composition Visualizer
 *
 * This example demonstrates timeline-based animation composition:
 * 1. Creates multiple animations with explicit timing offsets
 * 2. Shows coordinated animation sequences
 * 3. Visualizes timeline progress and active animations
 *
 * To run this example:
 * ./gradlew run --main=geo/examples/TimelineDemo
 *
 * Debug output shows:
 * - Timeline start time and elapsed time
 * - Active animations based on offset
 * - Animation progress for each timeline entry
 */
fun main() = application {
    configure {
        width = 1000
        height = 800
    }

    program {
        val startTime = System.currentTimeMillis()
        // Create timeline with multiple animations at different offsets
        val timeline = GeoTimeline {
            // Animation 1: Circle starts immediately (offset = 0)
            val anim1 = GeoAnimator().apply {
                val targetValue: Double = 60.0
                val duration: Long = 2000
            }
            add(anim1, offset = 0)

            // Animation 2: Square starts after 500ms
            val anim2 = GeoAnimator().apply {
                val targetValue: Double = 60.0
                val duration: Long = 2000
            }
            add(anim2, offset = 500)

            // Animation 3: Triangle starts after 1000ms
            val anim3 = GeoAnimator().apply {
                val targetValue: Double = 60.0
                val duration: Long = 2000
            }
            add(anim3, offset = 1000)
        }

        extend {
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            // Update timeline (only active animations get updated)
            timeline.update()

            val debugPanelX = 20.0
            var debugPanelY = 20.0
            val lineHeight = 24.0

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            drawer.text("TIMELINE COMPOSITION DEMO", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            val isComplete = timeline.isComplete()
            drawer.text("Status: ${if (isComplete) "COMPLETE" else "RUNNING"}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Total Animations: ${timeline.size()}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            dividerTimeline(drawer, debugPanelX - 10.0, debugPanelY - 10.0, 980.0)
            debugPanelY += lineHeight * 2

            drawer.text("Visualization:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Red circle: Starts at 0ms (offset = 0)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Blue square: Starts at 500ms (offset = 500)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Green triangle: Starts at 1000ms (offset = 1000)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Shape expands from size 10→60 over 2000ms", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            drawer.text("Timeline Structure:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("GeoTimeline {", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  add(anim1, offset = 0)      // Circle, red", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  add(anim2, offset = 500)    // Square, blue", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  add(anim3, offset = 1000)   // Triangle, green", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            dividerTimeline(drawer, debugPanelX - 10.0, debugPanelY - 10.0, 980.0)
            debugPanelY += lineHeight * 2

            drawer.text("Animation State:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Anim | Offset | Status    | Size", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("-----|--------|-----------|-----", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            val elapsed = System.currentTimeMillis() - startTime

            // Animation 1 state
            val anim1Status = when {
                elapsed < 0 -> "WAITING"
                elapsed < 2000 -> "ANIMATING"
                else -> "DONE"
            }
            val anim1Size = if (elapsed in 0..1999) {
                val progress = elapsed / 2000.0
                10.0 + progress * 50.0
            } else {
                60.0
            }
            drawer.text("  1   |  0ms   | $anim1Status | ${anim1Size.toInt()}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            // Animation 2 state
            val anim2Status = when {
                elapsed < 500 -> "WAITING"
                elapsed < 2500 -> "ANIMATING"
                else -> "DONE"
            }
            val anim2Elapsed = elapsed - 500
            val anim2Size = if (anim2Elapsed in 0..1999) {
                val progress = anim2Elapsed / 2000.0
                10.0 + progress * 50.0
            } else {
                60.0
            }
            drawer.text("  2   |  500ms | $anim2Status | ${anim2Size.toInt()}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            // Animation 3 state
            val anim3Status = when {
                elapsed < 1000 -> "WAITING"
                elapsed < 3000 -> "ANIMATING"
                else -> "DONE"
            }
            val anim3Elapsed = elapsed - 1000
            val anim3Size = if (anim3Elapsed in 0..1999) {
                val progress = anim3Elapsed / 2000.0
                10.0 + progress * 50.0
            } else {
                60.0
            }
            drawer.text("  3   | 1000ms | $anim3Status | ${anim3Size.toInt()}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2

            // Visual rendering
            val centerX = 500.0
            val centerY = 500.0
            val spacing = 200.0

            // Draw timeline bar
            val timelineWidth = 800.0
            drawTimelineBar(drawer, centerX - 400.0, centerY - 250.0, timelineWidth, elapsed)

            // Draw shapes with animations
            drawCircle(drawer, centerX - spacing, centerY, anim1Size, ColorRGBa.RED)
            drawSquare(drawer, centerX, centerY, anim2Size, ColorRGBa.BLUE)
            drawTriangle(drawer, centerX + spacing, centerY, anim3Size, ColorRGBa.GREEN)

            // Draw labels
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            drawer.text("Red Circle", centerX - spacing - 30.0, centerY + 80.0)
            drawer.text("(offset = 0ms)", centerX - spacing - 30.0, centerY + 100.0)

            drawer.text("Blue Square", centerX - 25.0, centerY + 80.0)
            drawer.text("(offset = 500ms)", centerX - 25.0, centerY + 100.0)

            drawer.text("Green Triangle", centerX + spacing - 35.0, centerY + 80.0)
            drawer.text("(offset = 1000ms)", centerX + spacing - 35.0, centerY + 100.0)

            if (isComplete) {
                drawer.fill = ColorRGBa.YELLOW
                drawer.text("✓ All animations complete", centerX - 100.0, centerY + 150.0)
            }
        }
    }
}

// Drawing helpers
fun drawCircle(drawer: Drawer, x: Double, y: Double, size: Double, color: ColorRGBa) {
    drawer.fill = color
    drawer.stroke = ColorRGBa.WHITE
    drawer.strokeWeight = 2.0
    drawer.circle(x, y, size)
}

fun drawSquare(drawer: Drawer, x: Double, y: Double, size: Double, color: ColorRGBa) {
    drawer.fill = color
    drawer.stroke = ColorRGBa.WHITE
    drawer.strokeWeight = 2.0
    val half = size / 2.0
    drawer.rectangle(x - half, y - half, size, size)
}

fun drawTriangle(drawer: Drawer, x: Double, y: Double, size: Double, color: ColorRGBa) {
    drawer.fill = color
    drawer.stroke = ColorRGBa.WHITE
    drawer.strokeWeight = 2.0
    val halfSize = size / 2.0

    // Draw triangle using lines
    val top = Vector2(x, y - halfSize)
    val bottomLeft = Vector2(x - halfSize * Math.sqrt(3.0) / 2.0, y + halfSize)
    val bottomRight = Vector2(x + halfSize * Math.sqrt(3.0) / 2.0, y + halfSize)

    drawer.lineStrip(listOf(top, bottomLeft, bottomRight, top))
}

fun drawTimelineBar(drawer: Drawer, x: Double, y: Double, width: Double, elapsed: Long) {
    // Draw bar background
    drawer.fill = ColorRGBa.fromHex("#333333")
    drawer.stroke = ColorRGBa.WHITE.opacify(0.3)
    drawer.strokeWeight = 2.0
    drawer.rectangle(x, y, width, 30.0)

    // Draw progress (3 seconds total timeline)
    val progress = (elapsed.toDouble() / 3000.0).coerceAtMost(1.0)
    drawer.fill = ColorRGBa.WHITE.opacify(0.3)
    drawer.stroke = null
    drawer.rectangle(x, y, width * progress, 30.0)

    // Draw offset markers
    drawer.stroke = ColorRGBa.YELLOW
    drawer.strokeWeight = 2.0

    // Offset 0ms marker
    drawer.lineSegment(
        Vector2(x, y - 5.0),
        Vector2(x, y + 35.0)
    )

    // Offset 500ms marker
    val xOffset500 = x + width * (500.0 / 3000.0)
    drawer.lineSegment(
        Vector2(xOffset500, y - 5.0),
        Vector2(xOffset500, y + 35.0)
    )

    // Offset 1000ms marker
    val xOffset1000 = x + width * (1000.0 / 3000.0)
    drawer.lineSegment(
        Vector2(xOffset1000, y - 5.0),
        Vector2(xOffset1000, y + 35.0)
    )
}

fun dividerTimeline(drawer: Drawer, x: Double, y: Double, width: Double) {
    drawer.stroke = ColorRGBa.WHITE.opacify(0.3)
    drawer.strokeWeight = 2.0
    drawer.lineStrip(listOf(Vector2(x, y), Vector2(x + width, y)))
}