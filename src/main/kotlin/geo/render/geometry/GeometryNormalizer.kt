package geo.render.geometry

import geo.MultiPolygon
import geo.Polygon
import org.openrndr.math.Vector2

/**
 * Normalizes a polygon by applying all normalization steps:
 * 1. Validate interior rings (drop degenerate, log warnings)
 * 2. Make coordinates continuous across antimeridian for seamless rendering
 * 3. Normalize winding of exterior and all interiors
 *
 * Uses continuous coordinates instead of splitting, so polygons render as
 * continuous bands across the antimeridian rather than separate fragments.
 *
 * @param polygon The polygon to normalize
 * @param featureId Optional feature ID for logging
 * @return List of normalized polygons (single polygon for antimeridian-crossing polygons)
 */
fun normalizePolygon(polygon: Polygon, featureId: String? = null): List<Polygon> {
    // Step 1: Validate interior rings
    val validInteriors = validateInteriorRings(polygon.exterior, polygon.interiors, featureId)

    // Step 2: Handle antimeridian - use continuous coordinates for rendering
    // This keeps the polygon as one piece that renders as a band across the antimeridian
    val continuousExterior = makeCoordinatesContinuous(polygon.exterior)

    // Step 3: Close ring properly
    val closedExterior = closeRing(continuousExterior)

    // Step 4: Process interiors - shift them by the same offset as exterior if needed
    val shiftedInteriors = if (continuousExterior != polygon.exterior) {
        // Calculate the offset applied to the first point
        val offset = continuousExterior.firstOrNull()?.x?.minus(polygon.exterior.firstOrNull()?.x ?: 0.0) ?: 0.0
        if (kotlin.math.abs(offset) > 1e-10) {
            polygon.interiors.map { interior ->
                val shiftedFirst = interior.first()
                val interiorOffset = if (shiftedFirst.x < 0 && polygon.exterior.any { it.x > 0 }) {
                    offset  // Exterior shifted positive, shift interiors the same
                } else 0.0
                interior.map { Vector2(it.x + interiorOffset, it.y) }
            }
        } else {
            polygon.interiors
        }
    } else {
        polygon.interiors
    }

    // Step 5: Find interiors that belong to this exterior
    val assignedInteriors = shiftedInteriors.map { closeRing(it) }.filter { it.size >= 4 }

    // Step 6: Normalize winding for exterior and its assigned interiors together
    val (normalizedExterior, normalizedInteriors) = normalizePolygonWinding(closedExterior, assignedInteriors)

    val result = mutableListOf<Polygon>()
    result.add(Polygon(normalizedExterior, normalizedInteriors))

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
