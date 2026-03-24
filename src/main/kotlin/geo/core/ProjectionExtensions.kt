package geo

import geo.internal.batch.CoordinateBatch
import geo.internal.batch.batchProject
import geo.projection.GeoProjection
import org.openrndr.math.Vector2

/**
 * Extension functions for projecting geometries to screen space.
 * These use the projection.project() method to convert geographic coordinates to screen space.
 *
 * Implementation uses batch projection internally to eliminate allocation churn
 * from `.map { projection.project(it) }` patterns. Per RESEARCH.md Pattern 1 & 2.
 */

/**
 * Project a Point to screen coordinates.
 */
fun Point.projectToScreen(projection: GeoProjection): Vector2 {
    return projection.project(Vector2(x, y))
}

/**
 * Project a LineString to screen coordinates.
 *
 * Uses batch projection internally for improved performance:
 * - Converts points to DoubleArray via CoordinateBatch
 * - Uses indexed loop projection instead of per-point map
 * - Converts result back to List<Vector2> for API compatibility
 */
fun LineString.projectToScreen(projection: GeoProjection): List<Vector2> {
    val batch = CoordinateBatch.fromPoints(points)
    val output = Array(points.size) { Vector2.ZERO }
    batchProject(batch.x, batch.y, projection, output)
    return output.toList()
}

/**
 * Project a Polygon's exterior ring to screen coordinates.
 *
 * Uses batch projection internally for improved performance.
 */
fun Polygon.projectToScreen(projection: GeoProjection): List<Vector2> {
    val batch = CoordinateBatch.fromPoints(exterior)
    val output = Array(exterior.size) { Vector2.ZERO }
    batchProject(batch.x, batch.y, projection, output)
    return output.toList()
}

/**
 * Project a Polygon's exterior ring to screen coordinates as Array<Vector2>.
 *
 * This variant returns an Array instead of List for zero-copy scenarios
 * where OPENRNDR Drawer can use the array directly.
 */
fun Polygon.projectToScreenArray(projection: GeoProjection): Array<Vector2> {
    val batch = CoordinateBatch.fromPoints(exterior)
    val output = Array(exterior.size) { Vector2.ZERO }
    return batchProject(batch.x, batch.y, projection, output)
}

/**
 * Project a MultiPoint to screen coordinates.
 *
 * Uses batch projection internally for improved performance.
 */
fun MultiPoint.projectToScreen(projection: GeoProjection): List<Vector2> {
    val batch = CoordinateBatch.fromPoints(points.map { Vector2(it.x, it.y) })
    val output = Array(points.size) { Vector2.ZERO }
    batchProject(batch.x, batch.y, projection, output)
    return output.toList()
}

/**
 * Project a MultiLineString to screen coordinates.
 *
 * Uses batch projection internally for improved performance.
 */
fun MultiLineString.projectToScreen(projection: GeoProjection): List<List<Vector2>> {
    return lineStrings.map { line ->
        val batch = CoordinateBatch.fromPoints(line.points)
        val output = Array(line.points.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
        output.toList()
    }
}

/**
 * Project a MultiPolygon's exterior rings to screen coordinates.
 *
 * Uses batch projection internally for improved performance.
 */
fun MultiPolygon.projectToScreen(projection: GeoProjection): List<List<Vector2>> {
    return polygons.map { poly ->
        val batch = CoordinateBatch.fromPoints(poly.exterior)
        val output = Array(poly.exterior.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
        output.toList()
    }
}

// ============================================================================
// Internal Batch Projection Helpers
// ============================================================================

/**
 * Projects a Geometry to screen coordinates using batch projection.
 * Returns Array<Vector2> for zero-copy pipeline scenarios.
 *
 * This is an internal helper for optimized rendering paths.
 * Public APIs should use the typed projectToScreen() variants above.
 *
 * @param projection The projection to use for coordinate transformation
 * @return Array of projected Vector2 coordinates
 */
internal fun Geometry.projectToScreenBatch(projection: GeoProjection): Array<Vector2> = when (this) {
    is Point -> {
        Array(1) { projection.project(Vector2(x, y)) }
    }

    is LineString -> {
        val batch = CoordinateBatch.fromPoints(points)
        val output = Array(points.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
    }

    is Polygon -> {
        // For Polygon, project exterior ring only
        val batch = CoordinateBatch.fromPoints(exterior)
        val output = Array(exterior.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
    }

    is MultiPoint -> {
        val batch = CoordinateBatch.fromPoints(points.map { Vector2(it.x, it.y) })
        val output = Array(points.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
    }

    is MultiLineString -> {
        // Flatten all linestring points for batch projection
        val allPoints = lineStrings.flatMap { it.points }
        val batch = CoordinateBatch.fromPoints(allPoints)
        val output = Array(allPoints.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
    }

    is MultiPolygon -> {
        // Flatten all polygon exteriors for batch projection
        val allPoints = polygons.flatMap { it.exterior }
        val batch = CoordinateBatch.fromPoints(allPoints)
        val output = Array(allPoints.size) { Vector2.ZERO }
        batchProject(batch.x, batch.y, projection, output)
    }
}

/**
 * Projects a CoordinateBatch directly to a pre-allocated output array.
 *
 * This is the lowest-level batch projection helper for internal use.
 *
 * @param batch The coordinate batch to project
 * @param projection The projection to use
 * @param output Pre-allocated output array to fill
 * @return The filled output array
 */
internal fun CoordinateBatch.projectToArray(
    projection: GeoProjection,
    output: Array<Vector2>
): Array<Vector2> {
    return batchProject(x, y, projection, output)
}
