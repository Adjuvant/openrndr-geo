package geo.render

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.Shape
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.*
import org.openrndr.shape.*
import geo.render.geometry.normalizePolygonWinding

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

    // Normalize winding: exterior clockwise, interiors counter-clockwise
    val (normalizedExterior, normalizedInteriors) = normalizePolygonWinding(exterior, interiors)

    // Use non-zero winding rule: add all contours to a single Shape
    // Exterior clockwise = positive area, Interior counter-clockwise = negative area
    // Non-zero rule: contributions with opposite signs cancel, leaving holes transparent
    val allContours = mutableListOf<ShapeContour>()
    
    val extContour = ShapeContour.fromPoints(normalizedExterior, closed = true)
    allContours.add(extContour)
    
    val holeContours = normalizedInteriors.filter { it.size >= 3 }.map { ring ->
        ShapeContour.fromPoints(ring, closed = true)
    }
    allContours.addAll(holeContours)

    drawer.shape(Shape(allContours))
}

fun writePolygon(
    drawer: Drawer,
    points: List<Vector2>,
    style: Style
) {
    if (points.size < 3) return

    drawer.fill = style.fill ?: ColorRGBa.WHITE.withAlpha(0.0)
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    val contour = ShapeContour.fromPoints(points, closed = true)
    drawer.shape(contour.shape)
}
