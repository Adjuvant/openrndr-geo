package geo.projection

import org.openrndr.math.Vector2
import org.openrndr.shapes.Rectangle

/**
 * Clamp latitude to valid Mercator range (avoid pole overflow).
 *
 * @param latitude Latitude in degrees
 * @param max Maximum latitude, defaults to 85.05112878 (Web Mercator limit)
 * @return Clamped latitude in degrees
 */
fun clampLatitude(latitude: Double, max: Double = 85.05112878): Double {
    return latitude.coerceIn(-max, max)
}

/**
 * Normalize longitude to standard range [-180, 180].
 * Earth wraps around, -181° is same as 179°.
 *
 * @param longitude Longitude in degrees
 * @return Normalized longitude in degrees [-180, 180]
 */
fun normalizeLongitude(longitude: Double): Double {
    var normalized = ((longitude % 360.0) + 360.0) % 360.0
    if (normalized > 180.0) normalized -= 360.0
    return normalized
}

/**
 * Normalize geographic coordinates to standard range.
 * Longitude to [-180, 180], latitude is already valid input [-90, 90].
 *
 * @param latLng Geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @return Normalized geographic coordinates
 */
fun normalizeCoordinate(latLng: Vector2): Vector2 {
    val lon = normalizeLongitude(latLng.x)
    val lat = latLng.y // Latitude is already in [-90, 90] for valid input
    return Vector2(lon, lat)
}

/**
 * Check if screen coordinate is visible on screen.
 *
 * Off-screen coordinates are valid (CONTEXT.md decision) — user should filter.
 * This helper provides visibility check for rendering optimization.
 *
 * @param point Screen coordinate to check
 * @param bounds Screen bounds as Rectangle
 * @return true if point is on screen, false otherwise
 */
fun isOnScreen(point: Vector2, bounds: Rectangle): Boolean {
    return point.x >= bounds.x &&
           point.x < bounds.x + bounds.width &&
           point.y >= bounds.y &&
           point.y < bounds.y + bounds.height
}

/**
 * Check if coordinate is within valid geographic range.
 *
 * @param latLng Geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @return true if coordinates are valid (lat in [-90, 90], lon in [-180, 180])
 */
fun isValidCoordinate(latLng: Vector2): Boolean {
    return latLng.y in -90.0..90.0 && latLng.x in -180.0..180.0
}

/**
 * Check if coordinate is within UK bounds for BNG transformation.
 *
 * @param bng BNG coordinates as Vector2 (x=easting, y=northing) in meters
 * @return true if coordinates are within UK grid area
 */
fun isBNGValid(bng: Vector2): Boolean {
    return bng.x in 0.0..700000.0 && bng.y in 0.0..1300000.0
}
