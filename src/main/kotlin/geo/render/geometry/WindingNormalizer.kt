package geo.render.geometry

import org.openrndr.math.Vector2
import kotlin.math.abs

/**
 * Calculates the signed area of a polygon ring using the shoelace formula.
 *
 * Positive area = counter-clockwise winding
 * Negative area = clockwise winding
 *
 * @param points The ring as a list of Vector2 points
 * @return The signed area (positive for CCW, negative for CW)
 */
internal fun calculateSignedArea(points: List<Vector2>): Double {
    if (points.size < 3) return 0.0

    var sum = 0.0
    val n = points.size

    for (i in points.indices) {
        val current = points[i]
        val next = points[(i + 1) % n]
        sum += (next.x - current.x) * (next.y + current.y)
    }

    return sum / 2.0
}

/**
 * Determines if a ring needs winding reversal to achieve the desired winding.
 *
 * @param points The ring as a list of Vector2 points
 * @param desiredClockwise True if clockwise winding is desired, false for counter-clockwise
 * @return True if the ring needs to be reversed
 */
internal fun needsWindingReversal(points: List<Vector2>, desiredClockwise: Boolean): Boolean {
    val signedArea = calculateSignedArea(points)
    val isCurrentlyClockwise = signedArea < 0
    return isCurrentlyClockwise != desiredClockwise
}

/**
 * Enforces a specific winding direction on a ring.
 *
 * @param points The ring as a list of Vector2 points
 * @param desiredClockwise True for clockwise, false for counter-clockwise
 * @return The ring with the specified winding direction
 */
internal fun enforceWinding(points: List<Vector2>, desiredClockwise: Boolean): List<Vector2> {
    if (points.size < 2) return points

    return if (needsWindingReversal(points, desiredClockwise)) {
        points.reversed()
    } else {
        points
    }
}

/**
 * Normalizes the winding of a polygon's exterior and interior rings.
 *
 * In screen space (Y inverted):
 * - Exterior rings should be clockwise (for proper fill)
 * - Interior rings (holes) should be counter-clockwise
 *
 * @param exterior The exterior ring as a list of Vector2 points
 * @param interiors The interior rings (holes) as a list of point lists
 * @return Pair of (normalized exterior, normalized interiors)
 */
internal fun normalizePolygonWinding(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>
): Pair<List<Vector2>, List<List<Vector2>>> {
    val normalizedExterior = enforceWinding(exterior, desiredClockwise = true)
    val normalizedInteriors = interiors.map { ring ->
        enforceWinding(ring, desiredClockwise = false)
    }
    return Pair(normalizedExterior, normalizedInteriors)
}
