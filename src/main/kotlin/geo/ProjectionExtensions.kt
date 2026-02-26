package geo

import geo.projection.GeoProjection
import org.openrndr.math.Vector2

/**
 * Extension functions for projecting geometries to screen space.
 * These use the projection.project() method to convert geographic coordinates to screen space.
 */

/**
 * Project a Point to screen coordinates.
 */
fun Point.projectToScreen(projection: GeoProjection): Vector2 {
    return projection.project(Vector2(x, y))
}

/**
 * Project a LineString to screen coordinates.
 */
fun LineString.projectToScreen(projection: GeoProjection): List<Vector2> {
    return points.map { projection.project(it) }
}

/**
 * Project a Polygon's exterior ring to screen coordinates.
 */
fun Polygon.projectToScreen(projection: GeoProjection): List<Vector2> {
    return exterior.map { projection.project(it) }
}

/**
 * Project a MultiPoint to screen coordinates.
 */
fun MultiPoint.projectToScreen(projection: GeoProjection): List<Vector2> {
    return points.map { projection.project(Vector2(it.x, it.y)) }
}

/**
 * Project a MultiLineString to screen coordinates.
 */
fun MultiLineString.projectToScreen(projection: GeoProjection): List<List<Vector2>> {
    return lineStrings.map { line -> line.points.map { projection.project(it) } }
}

/**
 * Project a MultiPolygon's exterior rings to screen coordinates.
 */
fun MultiPolygon.projectToScreen(projection: GeoProjection): List<List<Vector2>> {
    return polygons.map { poly -> poly.exterior.map { projection.project(it) } }
}
