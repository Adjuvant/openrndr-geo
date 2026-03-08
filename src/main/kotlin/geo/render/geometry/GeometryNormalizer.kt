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

    // Step 2: Check if exterior crosses antimeridian
    val exteriorRings = if (crossesAntimeridian(polygon.exterior)) {
        splitAtAntimeridian(polygon.exterior)
    } else {
        listOf(polygon.exterior)
    }

    // Step 3: Create a Polygon for each exterior ring with normalized winding
    return exteriorRings.mapNotNull { extRing ->
        // Filter out any empty or degenerate exterior rings
        if (extRing.size < 3) return@mapNotNull null

        // Normalize winding for this exterior and all interiors
        val (normalizedExterior, normalizedInteriors) = normalizePolygonWinding(extRing, validInteriors)
        Polygon(normalizedExterior, normalizedInteriors)
    }
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
