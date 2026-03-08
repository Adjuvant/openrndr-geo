package geo.render.geometry

import geo.MultiPolygon
import geo.Polygon
import org.openrndr.math.Vector2

/**
 * Normalizes a polygon by applying all normalization steps:
 * 1. Validate interior rings (drop degenerate, log warnings)
 * 2. Split exterior at antimeridian if needed
 * 3. Normalize winding of exterior and all interiors
 *
 * @param polygon The polygon to normalize
 * @param featureId Optional feature ID for logging
 * @return The normalized polygon
 */
fun normalizePolygon(polygon: Polygon, featureId: String? = null): Polygon {
    // Step 1: Validate interior rings
    val validInteriors = validateInteriorRings(polygon.exterior, polygon.interiors, featureId)
    
    // Step 2: Check if exterior crosses antimeridian
    val exteriorRings = if (crossesAntimeridian(polygon.exterior)) {
        splitAtAntimeridian(polygon.exterior)
    } else {
        listOf(polygon.exterior)
    }
    
    // For now, return first exterior ring with normalized winding
    // TODO: Handle multiple exterior rings from antimeridian split
    val normalizedExterior = if (exteriorRings.isNotEmpty()) {
        val (ext, _) = normalizePolygonWinding(exteriorRings[0], validInteriors)
        ext
    } else {
        polygon.exterior
    }
    
    // Step 3: Normalize winding of all interiors
    val (_, normalizedInteriors) = normalizePolygonWinding(normalizedExterior, validInteriors)
    
    return Polygon(normalizedExterior, normalizedInteriors)
}

/**
 * Normalizes a MultiPolygon by normalizing each constituent polygon.
 *
 * @param multiPolygon The MultiPolygon to normalize
 * @param featureId Optional feature ID for logging
 * @return The normalized MultiPolygon
 */
fun normalizeMultiPolygon(multiPolygon: MultiPolygon, featureId: String? = null): MultiPolygon {
    val normalizedPolygons = multiPolygon.polygons.map { polygon ->
        normalizePolygon(polygon, featureId)
    }
    return MultiPolygon(normalizedPolygons)
}

/**
 * Extension function to normalize a Polygon.
 */
fun Polygon.normalized(featureId: String? = null): Polygon = normalizePolygon(this, featureId)

/**
 * Extension function to normalize a MultiPolygon.
 */
fun MultiPolygon.normalized(featureId: String? = null): MultiPolygon = normalizeMultiPolygon(this, featureId)
