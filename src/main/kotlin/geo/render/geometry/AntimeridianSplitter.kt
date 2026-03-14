package geo.render.geometry

import org.openrndr.math.Vector2
import kotlin.math.abs

/**
 * Detects if a ring crosses the antimeridian (±180° longitude).
 * 
 * Includes the closing edge (last→first) in the check.
 * A crossing exists when the SHORT path between two adjacent vertices crosses ±180°.
 *
 * @param ring The ring to check as a list of Vector2 points (longitude = x, latitude = y)
 * @return true if the ring crosses the antimeridian
 */
internal fun crossesAntimeridian(ring: List<Vector2>): Boolean {
    if (ring.size < 2) return false

    // Check all edges including closing edge (last -> first)
    for (i in ring.indices) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]
        val diff = next.x - current.x
        // True crossing: the short path between points crosses ±180°
        // This happens when |diff| > 180 (not just touching boundary)
        if (abs(diff) > 180.0) {
            return true
        }
    }
    return false
}

/**
 * Detects if a ring touches the antimeridian (±180° longitude).
 * 
 * Returns true if any vertex has abs(lon) == 180 (within epsilon).
 * This indicates the data is already split at the seam.
 *
 * @param ring The ring to check
 * @return true if the ring has vertices at ±180°
 */
internal fun touchesAntimeridian(ring: List<Vector2>): Boolean {
    val epsilon = 0.0001
    return ring.any { abs(abs(it.x) - 180.0) < epsilon }
}

/**
 * Interpolates the latitude where a segment crosses the antimeridian boundary.
 *
 * Linearly interpolates between two points crossing the ±180° boundary to find
 * the exact latitude of the crossing point.
 *
 * @param p1 First point (longitude = x, latitude = y)
 * @param p2 Second point (longitude = x, latitude = y)
 * @return The latitude where the segment crosses the antimeridian
 */
internal fun interpolateAntimeridianCrossing(p1: Vector2, p2: Vector2): Double {
    // Calculate the shortest longitude difference (handles wraparound)
    var lonDiff = p2.x - p1.x
    if (lonDiff > 180.0) lonDiff -= 360.0
    if (lonDiff < -180.0) lonDiff += 360.0
    
    // How much longitude change to reach the boundary from p1
    val lonToBoundary = if (p1.x > 0) {
        180.0 - p1.x  // heading toward +180
    } else {
        -180.0 - p1.x  // heading toward -180
    }
    
    // Interpolation factor (fraction of the segment where crossing occurs)
    val t = lonToBoundary / lonDiff
    
    // Interpolate latitude at the crossing point
    return p1.y + t * (p2.y - p1.y)
}

/**
 * Makes coordinates continuous by adjusting longitude values when crossing the antimeridian.
 * Shifts coordinates by ±360° to maintain a single continuous polygon that renders correctly.
 * 
 * This handles both:
 * - Small jumps (e.g., 179° to -179°) - adds offset to make continuous
 * - Data already at ±180° (e.g., 180° to -180°) - keeps them continuous in the 0-360 range
 *
 * @param ring The input ring as a list of Vector2 points
 * @return A single continuous ring
 */
internal fun makeCoordinatesContinuous(ring: List<Vector2>): List<Vector2> {
    if (ring.isEmpty()) return emptyList()
    if (ring.size < 3) return ring
    
    // If ring doesn't cross antimeridian, return as-is
    if (!crossesAntimeridian(ring)) {
        return ring
    }
    
    val result = mutableListOf<Vector2>()
    var cumulativeOffset = 0.0
    var prevX = ring[0].x
    
    // First point - decide initial offset to keep in 0-360 range if crossing boundary
    var currentX = ring[0].x
    if (currentX < 0 && ring.any { it.x > 0 }) {
        // Ring spans both sides, normalize to 0-360
        if (currentX < 0) cumulativeOffset = 360.0
    }
    result.add(Vector2(currentX + cumulativeOffset, ring[0].y))
    
    for (i in 1 until ring.size) {
        val point = ring[i]
        var x = point.x
        
        // Calculate the actual difference considering wraparound
        var diff = x - prevX
        
        // If the raw difference is > 180 or < -180, we've crossed the antimeridian
        if (diff > 180.0) {
            // Crossed from positive to negative side - subtract 360
            cumulativeOffset -= 360.0
        } else if (diff < -180.0) {
            // Crossed from negative to positive side - add 360
            cumulativeOffset += 360.0
        }
        
        result.add(Vector2(x + cumulativeOffset, point.y))
        prevX = x
    }
    
    return result
}

/**
 * Splits a ring that crosses the antimeridian into separate rings.
 * Only uses interpolation-based splitting - more reliable than exact boundary handling.
 *
 * @param ring The input ring as a list of Vector2 points
 * @return List of closed rings, each entirely on one side of the antimeridian
 */
internal fun splitAtAntimeridian(ring: List<Vector2>): List<List<Vector2>> {
    if (ring.isEmpty()) return emptyList()
    if (ring.size < 3) return listOf(ring)

    // Only split if it actually crosses
    if (!crossesAntimeridian(ring)) {
        return listOf(ring)
    }

    // Split using enhanced interpolation - return all fragments without filtering
    return splitWithEnhancedInterpolation(ring)
}

private fun splitWithEnhancedInterpolation(ring: List<Vector2>): List<List<Vector2>> {
    if (ring.size < 3) return listOf(ring)
    
    val result = mutableListOf<MutableList<Vector2>>()
    var currentFragment = mutableListOf<Vector2>()

    val numEdges = ring.size
    for (i in 0 until numEdges) {
        val current = ring[i]
        currentFragment.add(current)

        val nextIndex = (i + 1) % ring.size
        val next = ring[nextIndex]

        val diff = next.x - current.x
        if (abs(diff) > 180.0) {
            // This edge crosses the antimeridian
            val crossingLat = interpolateAntimeridianCrossing(current, next)
            val boundaryLon = if (current.x > 0) 180.0 else -180.0
            
            // Add boundary intersection point - this becomes the LAST point
            currentFragment.add(Vector2(boundaryLon, crossingLat))
            
            // Save fragment (don't close with first - boundary should be last)
            result.add(currentFragment)
            
            // Start new fragment with opposite boundary point
            currentFragment = mutableListOf(Vector2(-boundaryLon, crossingLat))
        }
    }

    // Add final fragment 
    if (currentFragment.isNotEmpty()) {
        result.add(currentFragment)
    }

    return result
}

/**
 * Checks if a ring is valid for rendering:
 * - Has at least 4 points (3 distinct + closing)
 * - Is properly closed (first == last)
 * - No edges cross the antimeridian (including closing edge)
 */
private fun isValidRing(ring: List<Vector2>): Boolean {
    // Allow fragments with 3 points (boundary + one vertex + boundary closure)
    // This handles the case where a fragment has just the crossing edge
    if (ring.size < 3) return false
    if (ring.first() != ring.last()) return false
    
    // Check all edges including closing edge
    for (i in ring.indices) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]
        if (abs(next.x - current.x) > 180.0) {
            return false
        }
    }
    return true
}

/**
 * Splits a ring that crosses the antimeridian with interpolation at the boundary.
 * Handles ALL edges including the closing edge (last → first).
 * Returns properly closed rings.
 */
private fun splitWithInterpolation(ring: List<Vector2>): List<List<Vector2>> {
    if (ring.size < 3) return listOf(ring)
    
    val result = mutableListOf<MutableList<Vector2>>()
    var currentFragment = mutableListOf<Vector2>()

    // Process all edges including closing edge
    val numEdges = ring.size
    
    for (i in 0 until numEdges) {
        val current = ring[i]
        currentFragment.add(current)
        
        val nextIndex = (i + 1) % ring.size
        val next = ring[nextIndex]
        
        // Check if this edge crosses the antimeridian (|diff| > 180 means crossing)
        val diff = next.x - current.x
        if (abs(diff) > 180.0) {
            // This edge crosses the seam - interpolate the crossing point
            val crossingLat = interpolateAntimeridianCrossing(current, next)
            val boundaryLon = if (current.x > 0) 180.0 else -180.0
            
            // Add boundary point to close current fragment
            currentFragment.add(Vector2(boundaryLon, crossingLat))
            
            // Save current fragment if it has enough points
            if (currentFragment.size >= 3) {
                // Close the ring
                currentFragment.add(currentFragment.first())
                result.add(currentFragment)
            }
            
            // Start new fragment from opposite boundary
            currentFragment = mutableListOf(Vector2(-boundaryLon, crossingLat))
        }
    }
    
    // Add remaining fragment
    if (currentFragment.size >= 3) {
        currentFragment.add(currentFragment.first())
        result.add(currentFragment)
    }

    // If no splits happened (shouldn't get here if crossesAntimeridian returned true)
    if (result.isEmpty()) {
        return listOf(closeRing(ring))
    }
    
    return result
}

/**
 * Closes a ring by adding the first point at the end if not already closed.
 */
private fun closeRing(ring: List<Vector2>): List<Vector2> {
    if (ring.size < 3) return ring
    return if (ring.first() == ring.last()) {
        ring
    } else {
        ring + ring.first()
    }
}
