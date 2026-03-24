package geo.internal.geometry

import geo.core.Bounds
import geo.internal.batch.CoordinateBatch
import geo.internal.batch.batchProjectBatch
import geo.projection.GeoProjection
import org.openrndr.math.Vector2

/**
 * Optimized geometry implementations using CoordinateBatch for internal storage.
 * 
 * These internal classes store coordinates as DoubleArray pairs instead of List<Vector2>,
 * enabling efficient batch projection and reduced allocation during rendering.
 * They are not part of the Geometry sealed class hierarchy but provide compatible
 * screen coordinate projection methods.
 * 
 * Per RESEARCH.md Pattern 3: Geometry-type-agnostic approach using CoordinateBatch
 */

/**
 * Optimized Point geometry storing a single coordinate in a CoordinateBatch.
 */
internal class OptimizedPoint(
    val coord: CoordinateBatch
) {
    
    init {
        require(coord.size == 1) { "OptimizedPoint must have exactly 1 coordinate" }
    }
    
    /**
     * Bounding box of this point.
     */
    val boundingBox: Bounds by lazy {
        Bounds(coord.x[0], coord.y[0], coord.x[0], coord.y[0])
    }
    
    /**
     * Total number of coordinates in this geometry.
     */
    val coordinateCount: Int = 1
    
    /**
     * Projects this point to screen coordinates using batch projection.
     */
    fun toScreenCoordinates(projection: GeoProjection): Array<Vector2> {
        return batchProjectBatch(coord, projection)
    }
    
    /**
     * Projects this point to screen coordinates as a List.
     */
    fun toScreenCoordinatesList(projection: GeoProjection): List<Vector2> {
        return toScreenCoordinates(projection).toList()
    }
}

/**
 * Optimized LineString geometry storing coordinates in a CoordinateBatch.
 */
internal class OptimizedLineString(
    val coords: CoordinateBatch
) {
    
    init {
        require(coords.size >= 2) { "OptimizedLineString must have at least 2 points" }
    }
    
    /**
     * Bounding box of this line string.
     */
    val boundingBox: Bounds by lazy {
        val minX = coords.x.min()
        val minY = coords.y.min()
        val maxX = coords.x.max()
        val maxY = coords.y.max()
        Bounds(minX, minY, maxX, maxY)
    }
    
    /**
     * Total number of coordinates in this geometry.
     */
    val coordinateCount: Int get() = coords.size
    
    /**
     * Projects this line string to screen coordinates using batch projection.
     */
    fun toScreenCoordinates(projection: GeoProjection): Array<Vector2> {
        return batchProjectBatch(coords, projection)
    }
    
    /**
     * Projects this line string to screen coordinates as a List.
     */
    fun toScreenCoordinatesList(projection: GeoProjection): List<Vector2> {
        return toScreenCoordinates(projection).toList()
    }
}

/**
 * Optimized Polygon geometry storing rings as List<CoordinateBatch>.
 * First ring is the exterior, subsequent rings are holes.
 */
internal class OptimizedPolygon(
    val rings: List<CoordinateBatch>
) {
    
    init {
        require(rings.isNotEmpty()) { "OptimizedPolygon must have at least one ring (exterior)" }
        require(rings[0].size >= 3) { "OptimizedPolygon exterior ring must have at least 3 points" }
    }
    
    /**
     * Bounding box of this polygon (from exterior ring only).
     */
    val boundingBox: Bounds by lazy {
        // Bounding box comes from exterior ring only
        val exterior = rings[0]
        val minX = exterior.x.min()
        val minY = exterior.y.min()
        val maxX = exterior.x.max()
        val maxY = exterior.y.max()
        Bounds(minX, minY, maxX, maxY)
    }
    
    /**
     * Total number of coordinates across all rings.
     */
    val coordinateCount: Int by lazy {
        rings.sumOf { it.size }
    }
    
    /**
     * Returns true if this polygon has interior rings (holes).
     */
    fun hasHoles(): Boolean = rings.size > 1
    
    /**
     * Projects this polygon's exterior ring to screen coordinates.
     */
    fun exteriorToScreenCoordinates(projection: GeoProjection): Array<Vector2> {
        return batchProjectBatch(rings[0], projection)
    }
    
    /**
     * Projects this polygon's interior rings (holes) to screen coordinates.
     */
    fun interiorsToScreenCoordinates(projection: GeoProjection): List<Array<Vector2>> {
        return if (rings.size > 1) {
            rings.drop(1).map { ring ->
                batchProjectBatch(ring, projection)
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Projects all rings to screen coordinates.
     * Returns pair of (exterior, interiors).
     */
    fun toScreenCoordinates(projection: GeoProjection): Pair<Array<Vector2>, List<Array<Vector2>>> {
        return Pair(
            exteriorToScreenCoordinates(projection),
            interiorsToScreenCoordinates(projection)
        )
    }
}

/**
 * Optimized MultiPoint geometry storing all points in a single CoordinateBatch.
 */
internal class OptimizedMultiPoint(
    val coords: CoordinateBatch
) {
    
    init {
        require(coords.size >= 1) { "OptimizedMultiPoint must have at least 1 point" }
    }
    
    /**
     * Bounding box of all points.
     */
    val boundingBox: Bounds by lazy {
        val minX = coords.x.min()
        val minY = coords.y.min()
        val maxX = coords.x.max()
        val maxY = coords.y.max()
        Bounds(minX, minY, maxX, maxY)
    }
    
    /**
     * Total number of coordinates in this geometry.
     */
    val coordinateCount: Int get() = coords.size
    
    /**
     * Projects all points to screen coordinates using batch projection.
     */
    fun toScreenCoordinates(projection: GeoProjection): Array<Vector2> {
        return batchProjectBatch(coords, projection)
    }
    
    /**
     * Projects all points to screen coordinates as a List.
     */
    fun toScreenCoordinatesList(projection: GeoProjection): List<Vector2> {
        return toScreenCoordinates(projection).toList()
    }
}

/**
 * Optimized MultiLineString geometry storing lines as List<CoordinateBatch>.
 */
internal class OptimizedMultiLineString(
    val lines: List<CoordinateBatch>
) {
    
    init {
        require(lines.isNotEmpty()) { "OptimizedMultiLineString must have at least 1 line" }
    }
    
    /**
     * Bounding box of all lines.
     */
    val boundingBox: Bounds by lazy {
        lines.fold(Bounds.empty()) { acc, line ->
            val minX = line.x.min()
            val minY = line.y.min()
            val maxX = line.x.max()
            val maxY = line.y.max()
            acc.expandToInclude(minX, minY).expandToInclude(maxX, maxY)
        }
    }
    
    /**
     * Total number of coordinates across all lines.
     */
    val coordinateCount: Int by lazy {
        lines.sumOf { it.size }
    }
    
    /**
     * Projects all lines to screen coordinates using batch projection.
     */
    fun toScreenCoordinates(projection: GeoProjection): List<Array<Vector2>> {
        return lines.map { line ->
            batchProjectBatch(line, projection)
        }
    }
    
    /**
     * Projects all lines to screen coordinates as List<List<Vector2>>.
     */
    fun toScreenCoordinatesList(projection: GeoProjection): List<List<Vector2>> {
        return toScreenCoordinates(projection).map { it.toList() }
    }
}

/**
 * Optimized MultiPolygon geometry storing polygons as List<List<CoordinateBatch>>.
 * Each polygon is a list of rings (exterior + holes).
 */
internal class OptimizedMultiPolygon(
    val polygons: List<List<CoordinateBatch>>
) {
    
    init {
        require(polygons.isNotEmpty()) { "OptimizedMultiPolygon must have at least 1 polygon" }
    }
    
    /**
     * Bounding box of all polygons.
     */
    val boundingBox: Bounds by lazy {
        polygons.fold(Bounds.empty()) { acc, poly ->
            // Use exterior ring for bounding box
            val exterior = poly[0]
            val minX = exterior.x.min()
            val minY = exterior.y.min()
            val maxX = exterior.x.max()
            val maxY = exterior.y.max()
            acc.expandToInclude(minX, minY).expandToInclude(maxX, maxY)
        }
    }
    
    /**
     * Total number of coordinates across all polygons and rings.
     */
    val coordinateCount: Int by lazy {
        polygons.sumOf { poly ->
            poly.sumOf { ring -> ring.size }
        }
    }
    
    /**
     * Projects all polygons to screen coordinates.
     * Returns list of (exterior, interiors) pairs.
     */
    fun toScreenCoordinates(projection: GeoProjection): List<Pair<Array<Vector2>, List<Array<Vector2>>>> {
        return polygons.map { poly ->
            val exterior = batchProjectBatch(poly[0], projection)
            val interiors = if (poly.size > 1) {
                poly.drop(1).map { ring ->
                    batchProjectBatch(ring, projection)
                }
            } else {
                emptyList()
            }
            Pair(exterior, interiors)
        }
    }
    
    /**
     * Projects exterior rings only to screen coordinates.
     */
    fun exteriorsToScreenCoordinates(projection: GeoProjection): List<Array<Vector2>> {
        return polygons.map { poly ->
            batchProjectBatch(poly[0], projection)
        }
    }
}
