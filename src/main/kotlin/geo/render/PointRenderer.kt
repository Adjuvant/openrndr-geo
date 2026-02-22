package geo.render

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.math.sqrt

/**
 * Draw a Point geometry at screen coordinates with the specified style.
 *
 * Supports three shapes: Circle, Square, and equilateral Triangle.
 * Applies fill, stroke, and strokeWeight from style before drawing.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val style = Style {
 *         fill = ColorRGBa.RED
 *         stroke = ColorRGBa.BLACK
 *         size = 10.0
 *         shape = Shape.Circle
 *     }
 *     drawPoint(drawer, screenX, screenY, style)
 * }
 * ```
 *
 * ## Shape Details
 * - **Circle**: Drawn with drawer.circle(), radius = size / 2
 * - **Square**: Drawn with drawer.rectangle(), centered at (screenX, screenY)
 * - **Triangle**: Equilateral triangle calculated using ShapeContour.fromPoints()
 *
 * ## Performance
 * For animation, reuse the same Style object and mutate properties:
 * ```kotlin
 * val style = Style { size = 5.0 }
 * animate {
 *     style.size += 0.1  // No allocation
 *     points.forEach { drawPoint(drawer, it.x, it.y, style) }
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param screenX X coordinate in screen space (pixels)
 * @param screenY Y coordinate in screen space (pixels)
 * @param userStyle Style configuration (null = use defaultPointStyle)
 */
fun drawPoint(
    drawer: Drawer,
    screenX: Double,
    screenY: Double,
    userStyle: Style? = null
) {
    val style = mergeStyles(StyleDefaults.defaultPointStyle, userStyle)

    // Apply style properties to drawer
    drawer.fill = style.fill
    drawer.stroke = style.stroke
    drawer.strokeWeight = style.strokeWeight

    when (style.shape) {
        Shape.Circle -> {
            drawer.circle(screenX, screenY, style.size / 2)
        }

        Shape.Square -> {
            val halfSize = style.size / 2
            drawer.rectangle(
                screenX - halfSize,
                screenY - halfSize,
                style.size,
                style.size
            )
        }

        Shape.Triangle -> {
            // Equilateral triangle
            // Height of equilateral triangle = side * sqrt(3) / 2
            val height = style.size * sqrt(3.0) / 2

            // Top vertex
            val top = Vector2(screenX, screenY - height / 2)
            // Bottom left
            val bottomLeft = Vector2(screenX - style.size / 2, screenY + height / 2)
            // Bottom right
            val bottomRight = Vector2(screenX + style.size / 2, screenY + height / 2)

            val triangle = ShapeContour.fromPoints(
                listOf(top, bottomLeft, bottomRight),
                closed = true
            )

            drawer.contour(triangle)
        }
    }
}

/**
 * Extension function to draw a Point as Vector2 coordinate.
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param point Vector2 screen coordinate
 * @param style Style configuration (null = use defaults)
 */
fun drawPoint(
    drawer: Drawer,
    point: Vector2,
    style: Style? = null
) {
    drawPoint(drawer, point.x, point.y, style)
}
