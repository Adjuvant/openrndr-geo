package geo.render.geometry

import geo.MultiPolygon
import geo.Polygon
import org.openrndr.math.Vector2

/**
 * Normalizes a polygon by applying all normalization steps:
 * 1. Validate interior rings (drop degenerate, log warnings)
 * 2. Split exterior at antimeridian if needed
 * 3. Normalize winding of exterior and all interiors for each split part
 *
 * When a polygon's exterior crosses the antimeridian multiple times, it is split
 * into multiple polygons, each with a portion of the exterior and all valid interiors.
 *
 * @param polygon The polygon to normalize
 * @param featureId Optional feature ID for logging
 * @return List of normalized polygons (one per exterior ring, or single polygon if no split)
 */
fun normalizePolygon(polygon: Polygon, featureId: String? = null): List<Polygon> {
    // Step 1: Validate interior rings
    val validInteriors = validateInteriorRings(polygon.exterior, polygon.interiors, featureId)

    // Step 2: Handle antimeridian - split exterior into separate rings
    // Use split approach for ALL antimeridian cases - more reliable
    val processedExteriors: List<List<Vector2>> = when {
        crossesAntimeridian(polygon.exterior) -> {
            // Crosses the seam - split into separate rings at the seam
            splitAtAntimeridian(polygon.exterior)
        }
        else -> {
            // No antimeridian involvement
            listOf(polygon.exterior)
        }
    }

    // Step 3: Close all rings properly (add first point as last if not already closed)
    val closedExteriors = processedExteriors.map { closeRing(it) }.filter { it.size >= 4 }

    // Step 4: Process interiors - determine which exterior each interior belongs to
    val result = mutableListOf<Polygon>()
    
    for (extRing in closedExteriors) {
        // Find interiors that are contained within this exterior
        val assignedInteriors = validInteriors.filter { interior ->
            interior.size >= 3 && pointInPolygon(interior.first(), extRing)
        }.map { closeRing(it) }.filter { it.size >= 4 }
        
        // Normalize winding for this exterior and its assigned interiors together
        val (normalizedExterior, normalizedInteriors) = normalizePolygonWinding(extRing, assignedInteriors)
        
        result.add(Polygon(normalizedExterior, normalizedInteriors))
    }
    
    return result
}

/**
 * Checks if a ring has vertices on both +180 and -180 sides.
 */
private fun hasBothSides(ring: List<Vector2>): Boolean {
    val hasPositive = ring.any { it.x > 0 }
    val hasNegative = ring.any { it.x < 0 }
    return hasPositive && hasNegative
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

/**
 * Simple point-in-polygon test using ray casting.
 */
private fun pointInPolygon(point: Vector2, polygon: List<Vector2>): Boolean {
    if (polygon.size < 3) return false
    
    var crossings = 0
    val n = polygon.size
    for (i in 0 until n) {
        val j = (i + 1) % n
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y
        
        if (((yi > point.y) != (yj > point.y)) &&
            (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi)) {
            crossings++
        }
    }
    
    return crossings % 2 == 1
}

/**
 * Normalizes a MultiPolygon by normalizing each constituent polygon.
 *
 * Polygons that cross the antimeridian are split into multiple polygons,
 * which are flattened into the resulting MultiPolygon.
 *
 * @param multiPolygon The MultiPolygon to normalize
 * @param featureId Optional feature ID for logging
 * @return The normalized MultiPolygon with all split parts flattened
 */
fun normalizeMultiPolygon(multiPolygon: MultiPolygon, featureId: String? = null): MultiPolygon {
    val normalizedPolygons = multiPolygon.polygons.flatMap { polygon ->
        normalizePolygon(polygon, featureId)
    }
    return MultiPolygon(normalizedPolygons)
}

/**
 * Extension function to normalize a Polygon.
 *
 * @return List of normalized polygons (single polygon if no antimeridian split,
 *         multiple polygons if split occurred)
 */
fun Polygon.normalized(featureId: String? = null): List<Polygon> = normalizePolygon(this, featureId)

/**
 * Extension function to normalize a MultiPolygon.
 *
 * @return Normalized MultiPolygon with all antimeridian splits flattened
 */
fun MultiPolygon.normalized(featureId: String? = null): MultiPolygon = normalizeMultiPolygon(this, featureId)
