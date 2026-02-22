package geo.animation.interpolators

import org.openrndr.math.Vector2
import kotlin.math.*

/**
 * Earth's mean radius in meters for great-circle calculations.
 */
internal const val EARTH_RADIUS_METERS = 6371000.0

/**
 * Position in geographic coordinates (latitude, longitude in degrees).
 *
 * For the Haversine interpolator, x represents longitude and y represents latitude
 * to maintain consistency with OpenRNDR's Vector2 usage pattern.
 *
 * @property latitude Geographic latitude in degrees (-90 to 90)
 * @property longitude Geographic longitude in degrees (-180 to 180)
 */
data class Position(val latitude: Double, val longitude: Double) {
    /**
     * Convert to Vector2 with (x=longitude, y=latitude) for OpenRNDR compatibility.
     */
    fun toVector2(): Vector2 = Vector2(longitude, latitude)

    companion object {
        /**
         * Create Position from Vector2 where x=longitude, y=latitude.
         */
        fun fromVector2(v: Vector2): Position = Position(v.y, v.x)
    }
}

/**
 * Great-circle interpolation using the Haversine formula.
 *
 * Interpolates between two geographic positions along the shortest path
 * on Earth's surface (great-circle route). More accurate than linear
 * interpolation for long-distance paths, but computationally more expensive.
 *
 * ## Usage
 * ```kotlin
 * val london = Position(51.5074, -0.1278)      // (lat, lng)
 * val tokyo = Position(35.6762, 139.6503)
 * val midpoint = haversineInterpolate(london, tokyo, 0.5)
 * ```
 *
 * ## When to Use
 * - Long-distance geographic paths (>100km)
 * - Flight path visualizations
 * - Geodesic line animations
 *
 * ## When to Use Linear Instead
 * - Screen-space coordinates (already projected)
 * - Local maps where curvature is negligible
 * - Performance-critical animations
 *
 * @param from Starting position (latitude, longitude in degrees)
 * @param to Target position (latitude, longitude in degrees)
 * @param t Progress from 0.0 (start) to 1.0 (complete)
 * @return Interpolated position along the great-circle path
 *
 * @see linearInterpolate For fast screen-space interpolation
 * @see EARTH_RADIUS_METERS Earth's radius constant used in calculations
 * @author Phase 05-animation - 05-02
 */
fun haversineInterpolate(from: Position, to: Position, t: Double): Position {
    // Convert to radians
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)

    // Calculate angular distance using Haversine formula
    val dLat = lat2 - lat1
    val dLon = lon2 - lon1
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val angularDistance = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Handle case where points are the same
    if (angularDistance < 1e-10) {
        return from
    }

    // Spherical linear interpolation (SLERP-like)
    val sinAngularDistance = sin(angularDistance)
    val a1 = sin((1 - t) * angularDistance) / sinAngularDistance
    val a2 = sin(t * angularDistance) / sinAngularDistance

    // Calculate interpolated Cartesian coordinates
    val x = a1 * cos(lat1) * cos(lon1) + a2 * cos(lat2) * cos(lon2)
    val y = a1 * cos(lat1) * sin(lon1) + a2 * cos(lat2) * sin(lon2)
    val z = a1 * sin(lat1) + a2 * sin(lat2)

    // Convert back to lat/lng in degrees
    val lat = Math.toDegrees(atan2(z, sqrt(x.pow(2) + y.pow(2))))
    val lon = Math.toDegrees(atan2(y, x))

    return Position(lat, lon)
}
