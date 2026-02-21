package geo.projection

import org.openrndr.math.Vector2

/**
 * Procedural style: transform lat/lng to screen coordinates.
 * @param latitude Latitude in degrees
 * @param longitude Longitude in degrees
 * @param projection Projection to use for transformation
 * @return Screen coordinates as Vector2
 */
fun toScreen(latitude: Double, longitude: Double, projection: GeoProjection): Vector2 {
    return projection.project(Vector2(longitude, latitude))
}

/**
 * Extension method style: transform lat/lng to screen coordinates.
 * @param geoProjection Projection to use for transformation
 * @return Screen coordinates as Vector2
 */
fun Vector2.toScreen(geoProjection: GeoProjection): Vector2 {
    return geoProjection.project(this)
}

/**
 * Batch transformation: convert multiple lat/lng coordinates to screen coordinates.
 * Performance optimization for rendering large datasets.
 *
 * @param points Sequence of geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @param projection Projection to use for transformation
 * @return List of screen coordinates as Vector2
 */
fun toScreen(points: Sequence<Vector2>, projection: GeoProjection): List<Vector2> {
    return points.map { latLng -> projection.project(latLng) }.toList()
}

/**
 * Batch transformation: convert multiple lat/lng coordinates to screen coordinates.
 * Overload for List input (common use case).
 *
 * @param points List of geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @param projection Projection to use for transformation
 * @return List of screen coordinates as Vector2
 */
fun toScreen(points: List<Vector2>, projection: GeoProjection): List<Vector2> {
    return points.map { latLng -> projection.project(latLng) }
}

/**
 * Inverse transformation: screen coordinates to lat/lng (procedural style).
 * @param screenX Screen X coordinate
 * @param screenY Screen Y coordinate
 * @param projection Projection to use for inverse transformation
 * @return Geographic coordinates as Vector2 (x=longitude, y=latitude)
 */
fun fromScreen(screenX: Double, screenY: Double, projection: GeoProjection): Vector2 {
    return projection.unproject(Vector2(screenX, screenY))
}

/**
 * Inverse transformation: screen coordinates to lat/lng (extension style).
 * @param geoProjection Projection to use for inverse transformation
 * @return Geographic coordinates as Vector2 (x=longitude, y=latitude)
 */
fun Vector2.fromScreen(geoProjection: GeoProjection): Vector2 {
    return geoProjection.unproject(this)
}

/**
 * Batch inverse transformation: screen coordinates to lat/lng.
 *
 * @param points Sequence/List of screen coordinates as Vector2
 * @param projection Projection to use for inverse transformation
 * @return List of geographic coordinates as Vector2 (x=longitude, y=latitude)
 */
fun fromScreen(points: Sequence<Vector2>, projection: GeoProjection): List<Vector2> {
    return points.map { screen -> projection.unproject(screen) }.toList()
}
