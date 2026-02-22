package geo

import geo.projection.GeoProjection
import org.openrndr.math.Vector2

/**
 * Sealed class hierarchy for all geometry types.
 * Using sealed class enables exhaustive when expressions for type-safe rendering.
 */
sealed class Geometry {
    /**
     * The bounding box of this geometry.
     * Computed lazily since it may be expensive to calculate.
     */
    abstract val boundingBox: Bounds
}

/**
 * Represents a single point in 2D space.
 *
 * @property x The X coordinate
 * @property y The Y coordinate
 */
data class Point(val x: Double, val y: Double) : Geometry() {
    override val boundingBox: Bounds = Bounds(x, y, x, y)

    /**
     * Converts this point to an OpenRNDR Vector2.
     */
    fun toVector2() = Vector2(x, y)

    /**
     * Projects this geographic point to screen coordinates using the given projection.
     *
     * @param projection The projection to use for coordinate transformation
     * @return Screen coordinates as Vector2
     */
    fun toScreen(projection: GeoProjection): Vector2 {
        return projection.project(Vector2(x, y))
    }

    /**
     * Converts an OpenRNDR Vector2 to a Point.
     */
    companion object {
        fun fromVector2(v: Vector2) = Point(v.x, v.y)
    }
}

/**
 * Represents a line string (polyline) - a sequence of connected points.
 *
 * @property points The list of points defining the line string
 */
data class LineString(val points: List<Vector2>) : Geometry() {
    init {
        require(points.size >= 2) { "LineString must have at least 2 points" }
    }

    override val boundingBox: Bounds by lazy {
        val xs = points.map { it.x }
        val ys = points.map { it.y }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }

    /**
     * Returns the number of points in this line string.
     */
    val size: Int
        get() = points.size
}

/**
 * Represents a polygon with an exterior ring and optional interior rings (holes).
 *
 * @property exterior The exterior ring (outer boundary) as a list of points
 * @property interiors List of interior rings (holes), each as a list of points
 */
data class Polygon(
    val exterior: List<Vector2>,
    val interiors: List<List<Vector2>> = emptyList()
) : Geometry() {
    init {
        require(exterior.size >= 3) { "Polygon exterior must have at least 3 points" }
    }

    override val boundingBox: Bounds by lazy {
        val xs = exterior.map { it.x }
        val ys = exterior.map { it.y }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }

    /**
     * Returns true if this polygon has interior rings (holes).
     */
    fun hasHoles(): Boolean = interiors.isNotEmpty()
}

/**
 * Represents a collection of points.
 *
 * @property points The list of points
 */
data class MultiPoint(val points: List<Point>) : Geometry() {
    init {
        require(points.isNotEmpty()) { "MultiPoint must have at least 1 point" }
    }

    override val boundingBox: Bounds by lazy {
        points.fold(Bounds.empty()) { acc, pt ->
            acc.expandToInclude(pt.x, pt.y)
        }
    }

    /**
     * Returns the number of points in this multi-point.
     */
    val size: Int
        get() = points.size
}

/**
 * Represents a collection of line strings.
 *
 * @property lineStrings The list of line strings
 */
data class MultiLineString(val lineStrings: List<LineString>) : Geometry() {
    init {
        require(lineStrings.isNotEmpty()) { "MultiLineString must have at least 1 LineString" }
    }

    override val boundingBox: Bounds by lazy {
        lineStrings.fold(Bounds.empty()) { acc, ls ->
            acc.expandToInclude(ls.boundingBox)
        }
    }

    /**
     * Returns the number of line strings in this multi-line-string.
     */
    val size: Int
        get() = lineStrings.size
}

/**
 * Represents a collection of polygons.
 *
 * @property polygons The list of polygons
 */
data class MultiPolygon(val polygons: List<Polygon>) : Geometry() {
    init {
        require(polygons.isNotEmpty()) { "MultiPolygon must have at least 1 Polygon" }
    }

    override val boundingBox: Bounds by lazy {
        polygons.fold(Bounds.empty()) { acc, poly ->
            acc.expandToInclude(poly.boundingBox)
        }
    }

    /**
     * Returns the number of polygons in this multi-polygon.
     */
    val size: Int
        get() = polygons.size
}
