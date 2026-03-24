package geo.render.geometry

import geo.core.Bounds
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openrndr.math.Vector2
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

/**
 * Checks if a ring is degenerate (has < 3 distinct vertices or near-zero area).
 *
 * @param ring The ring to check
 * @return true if the ring is degenerate
 */
internal fun isDegenerateRing(ring: List<Vector2>): Boolean {
    if (ring.size < 3) return true
    
    // Check for near-zero signed area
    val area = calculateSignedArea(ring)
    return abs(area) < 1e-10
}

/**
 * Calculates the bounding box for a list of points.
 *
 * @param points The points to bound
 * @return Bounds containing all points
 */
internal fun calculateBounds(points: List<Vector2>): Bounds {
    if (points.isEmpty()) return Bounds.empty()
    
    var minX = points[0].x
    var maxX = points[0].x
    var minY = points[0].y
    var maxY = points[0].y
    
    for (point in points) {
        minX = kotlin.math.min(minX, point.x)
        maxX = kotlin.math.max(maxX, point.x)
        minY = kotlin.math.min(minY, point.y)
        maxY = kotlin.math.max(maxY, point.y)
    }
    
    return Bounds(minX, minY, maxX, maxY)
}

/**
 * Checks if a point is within bounds.
 *
 * @param point The point to check
 * @param bounds The bounds to check against
 * @return true if point is within bounds
 */
internal fun isPointInBounds(point: Vector2, bounds: Bounds): Boolean {
    return point.x >= bounds.minX && point.x <= bounds.maxX &&
           point.y >= bounds.minY && point.y <= bounds.maxY
}

/**
 * Validates interior rings (holes) of a polygon.
 *
 * Drops degenerate rings and logs warnings for issues.
 * Validates-but-don't-repair philosophy: warn but don't modify geometry.
 *
 * @param exterior The exterior ring of the polygon
 * @param interiors The interior rings (holes)
 * @param featureId Optional feature ID for logging
 * @return List of valid (non-degenerate) interior rings
 */
internal fun validateInteriorRings(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    featureId: String? = null
): List<List<Vector2>> {
    if (interiors.isEmpty()) return emptyList()
    
    val validRings = mutableListOf<List<Vector2>>()
    val exteriorBounds = calculateBounds(exterior)
    
    interiors.forEachIndexed { index, ring ->
        // Check 1: Minimum vertex count
        if (ring.size < 3) {
            logger.warn { 
                "Degenerate interior ring (feature=${featureId}, ring=$index): " +
                "only ${ring.size} vertices, minimum is 3"
            }
            return@forEachIndexed
        }
        
        // Check 2: Non-zero area
        if (isDegenerateRing(ring)) {
            logger.warn {
                "Degenerate interior ring (feature=${featureId}, ring=$index): " +
                "near-zero area"
            }
            return@forEachIndexed
        }
        
        // Check 3: Hole inside exterior bounds (fast check)
        if (ring.isNotEmpty()) {
            val firstPoint = ring[0]
            if (!isPointInBounds(firstPoint, exteriorBounds)) {
                logger.warn {
                    "Interior ring outside exterior bounds (feature=${featureId}, ring=$index): " +
                    "first vertex (${firstPoint.x}, ${firstPoint.y}) outside exterior bbox"
                }
                // Still render it - don't repair, just warn
            }
        }
        
        validRings.add(ring)
    }
    
    return validRings
}
