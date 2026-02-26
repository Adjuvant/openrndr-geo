package geo

import geo.projection.CRSTransformer
import geo.projection.GeoProjection
import geo.projection.MAX_MERCATOR_LAT
import geo.projection.ProjectionMercator
import org.openrndr.math.Vector2

/**
 * Maximum valid latitude for Web Mercator projection.
 * Coordinates beyond this will be clamped to prevent projection overflow.
 */
val MAX_MERCATOR_COORD: Double
    get() = MAX_MERCATOR_LAT

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
    fun toScreen(projection: GeoProjection): List<Vector2> {
        return points.map { pt ->
            // Convert each Vector2 geographic point to screen space
            Point(pt.x, pt.y).toScreen(projection)
        }
    }

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

    fun exteriorToScreen(projection: GeoProjection): List<Vector2> {
        return exterior.map { point ->
            Point(point.x, point.y).toScreen(projection)
        }
    }

    fun interiorsToScreen(projection: GeoProjection): List<List<Vector2>>{
        TODO("Not yet implemented")
    }
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

/**
 * Transforms geometry coordinates using the given CRS transformer.
 * Uses sealed class pattern for type-safe, exhaustive transformation of all geometry types.
 *
 * Always creates new Geometry instances (immutable), ensuring structural integrity
 * for nested coordinates (e.g., Polygon holes).
 *
 * @param transformer The CRSTransformer to apply to all coordinates
 * @return A new Geometry with transformed coordinates
 */
fun Geometry.transform(transformer: CRSTransformer): Geometry = when (this) {
    is Point -> transformer.transform(x, y).let { Point(it.x, it.y) }

    is LineString -> LineString(
        points.map { transformer.transform(it.x, it.y) }
    )

    is Polygon -> Polygon(
        exterior.map { transformer.transform(it.x, it.y) },
        interiors.map { ring ->
            ring.map { transformer.transform(it.x, it.y) }
        }
    )

    is MultiPoint -> MultiPoint(
        points.map { point ->
            transformer.transform(point.x, point.y).let { Point(it.x, it.y) }
        }
    )

    is MultiLineString -> MultiLineString(
        lineStrings.map { line ->
            LineString(line.points.map { transformer.transform(it.x, it.y) })
        }
    )

    is MultiPolygon -> MultiPolygon(
        polygons.map { poly ->
            Polygon(
                poly.exterior.map { transformer.transform(it.x, it.y) },
                poly.interiors.map { ring ->
                    ring.map { transformer.transform(it.x, it.y) }
                }
            )
        }
    )
}

// ============================================================================
// Mercator Bounds Validation and Clamping
// ============================================================================

/**
 * Validates that all coordinates in this geometry are within valid Mercator bounds.
 * Returns true if all latitudes are within [-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT]
 * and all longitudes are within [-180, 180].
 *
 * @return true if geometry is valid for Mercator projection
 */
fun Geometry.validateMercatorBounds(): Boolean {
    return when (this) {
        is Point -> isValidCoordinate(Vector2(x, y))
        is LineString -> points.all { isValidCoordinate(it) }
        is Polygon -> exterior.all { isValidCoordinate(it) } && interiors.flatten().all { isValidCoordinate(it) }
        is MultiPoint -> points.all { isValidCoordinate(Vector2(it.x, it.y)) }
        is MultiLineString -> lineStrings.all { ls -> ls.points.all { isValidCoordinate(it) } }
        is MultiPolygon -> polygons.all { poly ->
            poly.exterior.all { isValidCoordinate(it) } && poly.interiors.flatten().all { isValidCoordinate(it) }
        }
    }
}

/**
 * Checks if a coordinate is valid for Mercator projection.
 * Valid range: latitude in [-85.05112878, 85.05112878], longitude in [-180, 180]
 */
fun isValidCoordinate(coord: Vector2): Boolean {
    return coord.x in -180.0..180.0 && coord.y in -MAX_MERCATOR_LAT..MAX_MERCATOR_LAT
}

/**
 * Clamps all coordinates in this geometry to valid Mercator bounds.
 * Latitude is clamped to [-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT].
 * Longitude remains unchanged (it wraps naturally in Mercator).
 *
 * This prevents projection overflow and artifacts at the poles.
 *
 * @return A new Geometry with all coordinates clamped to valid Mercator range
 */
fun Geometry.clampToMercator(): Geometry {
    return when (this) {
        is Point -> Point(
            x = x,
            y = y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        )

        is LineString -> LineString(
            points = points.map { pt ->
                Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            }
        )

        is Polygon -> Polygon(
            exterior = exterior.map { pt ->
                Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            },
            interiors = interiors.map { ring ->
                ring.map { pt ->
                    Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
                }
            }
        )

        is MultiPoint -> MultiPoint(
            points = points.map { pt ->
                Point(
                    x = pt.x,
                    y = pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                )
            }
        )

        is MultiLineString -> MultiLineString(
            lineStrings = lineStrings.map { ls ->
                LineString(
                    ls.points.map { pt ->
                        Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
                    }
                )
            }
        )

        is MultiPolygon -> MultiPolygon(
            polygons = polygons.map { poly ->
                Polygon(
                    exterior = poly.exterior.map { pt ->
                        Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
                    },
                    interiors = poly.interiors.map { ring ->
                        ring.map { pt ->
                            Vector2(pt.x, pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
                        }
                    }
                )
            }
        )
    }
}

/**
 * Normalizes longitude to [-180, 180] range.
 * Handles dateline crossing for geometries that span across the international date line.
 *
 * @return Longitude normalized to [-180, 180]
 */
fun normalizeLongitude(lng: Double): Double {
    var normalized = ((lng % 360.0) + 360.0) % 360.0
    if (normalized > 180.0) normalized -= 360.0
    return normalized
}

/**
 * Clamps this geometry to Mercator bounds AND normalizes longitude.
 * Use this for geometries that may span the dateline or include polar regions.
 *
 * @return A new Geometry with coordinates clamped and normalized
 */
fun Geometry.clampAndNormalize(): Geometry {
    return when (this) {
        is Point -> Point(
            x = normalizeLongitude(x),
            y = y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        )

        is LineString -> LineString(
            points = points.map { pt ->
                Vector2(
                    normalizeLongitude(pt.x),
                    pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                )
            }
        )

        is Polygon -> Polygon(
            exterior = exterior.map { pt ->
                Vector2(
                    normalizeLongitude(pt.x),
                    pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                )
            },
            interiors = interiors.map { ring ->
                ring.map { pt ->
                    Vector2(
                        normalizeLongitude(pt.x),
                        pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                    )
                }
            }
        )

        is MultiPoint -> MultiPoint(
            points = points.map { pt ->
                Point(
                    x = normalizeLongitude(pt.x),
                    y = pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                )
            }
        )

        is MultiLineString -> MultiLineString(
            lineStrings = lineStrings.map { ls ->
                LineString(
                    ls.points.map { pt ->
                        Vector2(
                            normalizeLongitude(pt.x),
                            pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                        )
                    }
                )
            }
        )

        is MultiPolygon -> MultiPolygon(
            polygons = polygons.map { poly ->
                Polygon(
                    exterior = poly.exterior.map { pt ->
                        Vector2(
                            normalizeLongitude(pt.x),
                            pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                        )
                    },
                    interiors = poly.interiors.map { ring ->
                        ring.map { pt ->
                            Vector2(
                                normalizeLongitude(pt.x),
                                pt.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                            )
                        }
                    }
                )
            }
        )
    }
}
