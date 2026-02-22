package geo.render

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

/**
 * Write (draw) a Polygon geometry to the screen using the specified style.
 *
 * Renders a closed polygon shape with configurable fill and stroke.
 * Creates a closed ShapeContour from the exterior ring points.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val style = Style {
 *         fill = ColorRGBa.RED.withAlpha(0.5)  // Semi-transparent fill
 *         stroke = ColorRGBa.BLACK
 *         strokeWeight = 2.0
 *         lineCap = LineCap.ROUND
 *         lineJoin = LineJoin.ROUND
 *     }
 *     writePolygon(drawer, polygon.exterior, style)
 * }
 * ```
 *
 * ## Fill Opacity
 * Use ColorRGBa.withAlpha() to set fill opacity:
 * ```kotlin
 * fill = ColorRGBa.BLUE.withAlpha(0.3)  // 30% opacity
 * ```
 *
 * ## Line Caps and Joins
 * Applied to polygon outline (stroke):
 * - Caps affect how stroke ends are rendered
 * - Joins affect how stroke corners are rendered
 *
 * ## Performance
 * For animation, reuse the same Style object and mutate properties:
 * ```kotlin
 * val style = Style { fill = ColorRGBa.RED.withAlpha(0.5) }
 * animate {
 *     style.fill = ColorRGBa.BLUE.withAlpha(0.5)  // No allocation
 *     writePolygon(drawer, points, style)
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param points List of Vector2 points defining the polygon exterior ring
 * @param style Style configuration with fill, stroke, strokeWeight, lineCap, lineJoin
 */
fun writePolygon(
    drawer: Drawer,
    points: List<Vector2>,
    style: Style
) {
    // Guard clause: need at least 3 points to form a polygon
    if (points.size < 3) return

    // Apply style properties to drawer
    drawer.fill = style.fill ?: ColorRGBa.WHITE.withAlpha(0.0)  // Transparent fill default
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    // Create closed contour from points
    // closed = true enables filling and connects last point to first
    val contour = ShapeContour.fromPoints(points, closed = true)

    // Draw the contour (renders both fill and stroke based on drawer properties)
    drawer.contour(contour)
}
