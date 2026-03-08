package geo.render

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.compound

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

/**
 * Write (draw) a Polygon geometry with interior rings (holes) to the screen.
 *
 * Uses OPENRNDR's compound { difference {} } boolean operation for explicit
 * hole subtraction:
 * - Exterior shape is the base
 * - Each hole contour is converted to a shape and subtracted from the base
 * - Returns List<Shape>, drawn with drawer.shapes()
 * - No manual winding enforcement needed
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val exterior = listOf(Vector2(0.0, 0.0), Vector2(100.0, 0.0), Vector2(50.0, 100.0))
 *     val holes = listOf(
 *         listOf(Vector2(20.0, 20.0), Vector2(40.0, 20.0), Vector2(30.0, 40.0))
 *     )
 *     writePolygonWithHoles(drawer, exterior, holes, Style {
 *         fill = ColorRGBa.RED.withAlpha(0.5)
 *         stroke = ColorRGBa.BLACK
 *     })
 * }
 * ```
 *
 * ## Hole Rendering
 * Holes appear as transparent cutouts in the polygon fill, showing the
 * background behind. The stroke is applied to both exterior and interior
 * ring boundaries.
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param exterior List of Vector2 points defining the polygon exterior ring
 * @param interiors List of interior rings (holes), each as a list of Vector2 points
 * @param style Style configuration with fill, stroke, strokeWeight, lineCap, lineJoin
 */
fun writePolygonWithHoles(
    drawer: Drawer,
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    style: Style
) {
    // Guard clause: need at least 3 points for exterior
    if (exterior.size < 3) return

    // Apply style properties to drawer
    drawer.fill = style.fill ?: ColorRGBa.WHITE.withAlpha(0.0)
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    val extContour = ShapeContour.fromPoints(exterior, closed = true)

    val holeContours = interiors.filter { it.size >= 3 }.map { ring ->
        ShapeContour.fromPoints(ring, closed = true)
    }

    if (holeContours.isEmpty()) {
        drawer.shape(extContour.shape)
    } else {
        // compound { difference {} } performs explicit boolean subtraction.
        // No manual winding enforcement needed - the first shape is the
        // base, all subsequent shapes are subtracted from it.
        // Returns List<Shape>.
        val result = compound {
            difference {
                shape(extContour.shape)
                for (hole in holeContours) {
                    shape(hole.shape)
                }
            }
        }
        drawer.shapes(result)
    }
}
