package geo.render

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3

/**
 * Write (draw) a LineString geometry to the screen using the specified style.
 *
 * Renders a connected sequence of line segments from a list of points.
 * Applies stroke color, weight, line caps, and joins from the style.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val style = Style {
 *         stroke = ColorRGBa.RED
 *         strokeWeight = 2.0
 *         lineCap = LineCap.ROUND
 *         lineJoin = LineJoin.ROUND
 *     }
 *     writeLineString(drawer, lineString.points, style)
 * }
 * ```
 *
 * ## Line Caps and Joins
 * - **BUTT cap**: Line ends flush with endpoint
 * - **ROUND cap**: Line ends with semicircle
 * - **SQUARE cap**: Line ends with square extension
 * - **MITER join**: Sharp corners (clipped at miterLimit)
 * - **ROUND join**: Rounded corners
 * - **BEVEL join**: Chamfered corners
 *
 * **Note:** Line caps only work in 2D drawing mode (Vector2 coordinates).
 *
 * ## Performance
 * For animation, reuse the same Style object and mutate properties:
 * ```kotlin
 * val style = Style { strokeWeight = 1.0 }
 * animate {
 *     style.strokeWeight += 0.1  // No allocation
 *     writeLineString(drawer, points, style)
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param points List of Vector2 points defining the line string
 * @param style Style configuration with stroke, strokeWeight, lineCap, lineJoin
 */
fun writeLineString(
    drawer: Drawer,
    points: List<Vector2>,
    style: Style
) {
    // Guard clause: need at least 2 points to draw a line
    if (points.size < 2) return

    // Apply style properties to drawer
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    // Convert points to Vector3 for lineStrip (z = 0.0 for 2D drawing)
    // lineStrip connects points sequentially, creating a continuous polyline
    drawer.lineStrip(points.map { Vector3(it.x, it.y, 0.0) })
}
