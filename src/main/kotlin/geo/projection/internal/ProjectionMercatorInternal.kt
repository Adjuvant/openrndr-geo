package geo.projection.internal

import geo.exception.ProjectionOverflowException
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

private const val EARTH_RADIUS = 6378137.0
private const val MAX_LATITUDE = 85.05112878 // 180/π * atan(e^π)

/**
 * Internal Mercator projection implementation.
 * Uses Web Mercator formula with sphere radius 6378137m.
 * Throws ProjectionOverflowException for poles (±90°).
 */
class ProjectionMercatorInternal(
    private val config: ProjectionConfig
) : GeoProjection {

    override fun project(latLng: Vector2): Vector2 {
        val lat = latLng.y
        val lon = latLng.x

        // Throw for exact poles (CONTEXT.md decision)
        if (lat == 90.0 || lat == -90.0) {
            throw ProjectionOverflowException(
                "Latitude $lat is exactly at pole. Mercator cannot represent ±90°. " +
                "Use clampLatitude(lat, max = 89.999)"
            )
        }

        // Clamp to valid range
        val clampedLat = lat.coerceIn(-MAX_LATITUDE, MAX_LATITUDE)

        // Web Mercator formula
        val x = EARTH_RADIUS * Math.toRadians(lon)
        val y = EARTH_RADIUS * ln(tan(PI / 4 + Math.toRadians(clampedLat) / 2))

        // Apply screen offset and scale
        return toScreenOffset(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        val projX = fromScreenOffset(screen.x, screen.y).x
        val projY = fromScreenOffset(screen.x, screen.y).y

        val lon = Math.toDegrees(projX / EARTH_RADIUS)
        val lat = Math.toDegrees(2 * kotlin.math.atan(kotlin.math.exp(projY / EARTH_RADIUS)) - PI / 2)

        return Vector2(lon, lat)
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionMercatorInternal(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        // Web Mercator is cylindrical, fit to full extent
        return ProjectionMercatorInternal(config.copy(
            center = Vector2(0.0, 0.0),
            scale = 1.0
        ))
    }

    private fun toScreenOffset(x: Double, y: Double): Vector2 {
        // Center world coordinates
        val centerX = config.center?.x ?: 0.0
        val centerY = config.center?.y ?: 0.0

        // Apply scale
        val scaledX = (x - centerX) * config.scale
        val scaledY = (y - centerY) * config.scale

        // Offset to screen origin
        return Vector2(
            config.width / 2.0 + scaledX,
            config.height / 2.0 - scaledY // Y flipped for screen
        )
    }

    private fun fromScreenOffset(x: Double, y: Double): Vector2 {
        val centerX = config.center?.x ?: 0.0
        val centerY = config.center?.y ?: 0.0

        val unscaledX = (x - config.width / 2.0) / config.scale + centerX
        val unscaledY = (config.height / 2.0 - y) / config.scale + centerY

        return Vector2(unscaledX, unscaledY)
    }
}
