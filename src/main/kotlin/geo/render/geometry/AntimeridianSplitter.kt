package geo.render.geometry

import org.openrndr.math.Vector2
import kotlin.math.abs

/**
 * Detects if a ring crosses the antimeridian (±180° longitude).
 *
 * Uses the heuristic: if any adjacent pair has a longitude jump > 180°, it's a crossing.
 *
 * @param ring The ring to check as a list of Vector2 points (longitude = x, latitude = y)
 * @return true if the ring crosses the antimeridian
 */
internal fun crossesAntimeridian(ring: List<Vector2>): Boolean {
    if (ring.size < 2) return false

    for (i in ring.indices) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]
        if (abs(next.x - current.x) > 180.0) {
            return true
        }
    }
    return false
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
    // Calculate the total longitude delta accounting for wraparound
    val deltaLon = if (p1.x > 0) {
        // Crossing from + to -: (180 - p1.x) + (p2.x + 180)
        (180.0 - p1.x) + (p2.x + 180.0)
    } else {
        // Crossing from - to +: (-180 - p1.x) + (p2.x - 180)
        (-180.0 - p1.x) + (p2.x - 180.0)
    }

    // Calculate how far along the segment the crossing occurs
    val t = if (p1.x > 0) {
        (180.0 - p1.x) / deltaLon
    } else {
        (-180.0 - p1.x) / deltaLon
    }

    // Interpolate latitude linearly
    return p1.y + t * (p2.y - p1.y)
}

/**
 * Splits a ring that crosses the antimeridian into separate rings.
 *
 * Walks the coordinate pairs, detects crossings, interpolates boundary vertices,
 * and splits the ring into separate closed rings on each side of the antimeridian.
 *
 * @param ring The input ring as a list of Vector2 points
 * @return List of closed rings, each entirely on one side of the antimeridian
 */
internal fun splitAtAntimeridian(ring: List<Vector2>): List<List<Vector2>> {
    if (ring.isEmpty()) return emptyList()
    if (ring.size < 2) return listOf(ring)

    val result = mutableListOf<MutableList<Vector2>>()
    var currentRing = mutableListOf<Vector2>()

    // Check if this is a closed ring (first == last)
    val isClosed = ring.size > 1 && ring.first().x == ring.last().x && ring.first().y == ring.last().y
    val iterations = if (isClosed) ring.size - 1 else ring.size

    for (i in 0 until iterations) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]

        currentRing.add(current)

        // Check if this segment crosses the antimeridian
        if (abs(next.x - current.x) > 180.0) {
            // Interpolate the crossing point
            val crossingLat = interpolateAntimeridianCrossing(current, next)
            val boundaryLon = if (current.x > 0) 180.0 else -180.0

            // Add boundary point to close this side of the ring
            currentRing.add(Vector2(boundaryLon, crossingLat))
            result.add(currentRing)

            // Start new ring from the opposite side of the boundary
            currentRing = mutableListOf(Vector2(-boundaryLon, crossingLat))
        }
    }

    // Add the final ring if it has content
    if (currentRing.isNotEmpty()) {
        result.add(currentRing)
    }

    return result
}
