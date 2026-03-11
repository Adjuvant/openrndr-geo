import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.Shape
import org.openrndr.draw.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.*
import org.openrndr.shape.*

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

    val newExterior = exterior.clockwise()
    val newInteriors = interiors.map { it.counterClockwise() }

    val extContour = ShapeContour.fromPoints(newExterior, closed = true)
    val holeContours = newInteriors.filter { it.size >= 3 }.map { ring ->
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
