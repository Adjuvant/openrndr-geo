package geo.projection.internal

import geo.exception.ProjectionOverflowException
import geo.projection.GeoProjection
import geo.projection.MAX_MERCATOR_LAT
import geo.projection.ProjectionConfig
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan
import kotlin.math.log2
import kotlin.math.min

private const val EARTH_RADIUS = 6378137.0

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

        if (lat == 90.0 || lat == -90.0) {
            throw ProjectionOverflowException(
                "Latitude $lat is exactly at pole. Mercator cannot represent ±90°. " +
                "Use clampLatitude(lat, max = 89.999)"
            )
        }

        val clampedLat = lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)

        val x = Math.toRadians(lon)
        val y = ln(tan(PI / 4 + Math.toRadians(clampedLat) / 2))

        return toScreenOffset(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        val proj = fromScreenOffset(screen.x, screen.y)

        val lon = Math.toDegrees(proj.x)
        val lat = Math.toDegrees(2 * kotlin.math.atan(kotlin.math.exp(proj.y)) - PI / 2)

        return Vector2(lon, lat)
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionMercatorInternal(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        val worldWidth = 2 * PI
        val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_MERCATOR_LAT) / 2))

        val scaleX = config.width / worldWidth
        val scaleY = config.height / worldHeight
        val scale = min(scaleX, scaleY)

        // Convert scale to zoom: zoom = log2(scale / 256)
        val zoom = log2(scale / 256.0)

        return ProjectionMercatorInternal(config.copy(
            center = Vector2(0.0, 0.0),
            zoomLevel = zoom
        ))
    }

    private fun toScreenOffset(x: Double, y: Double): Vector2 {
        val centerX = config.center?.x?.let { Math.toRadians(it) } ?: 0.0
        val centerY = config.center?.y?.let { ln(tan(PI / 4 + Math.toRadians(it) / 2)) } ?: 0.0

        // Use config.scale which is now a computed property from zoomLevel
        val scaledX = (x - centerX) * config.scale
        val scaledY = (y - centerY) * config.scale

        return Vector2(
            config.width / 2.0 + scaledX,
            config.height / 2.0 - scaledY
        )
    }

    private fun fromScreenOffset(x: Double, y: Double): Vector2 {
        val centerX = config.center?.x?.let { Math.toRadians(it) } ?: 0.0
        val centerY = config.center?.y?.let { ln(tan(PI / 4 + Math.toRadians(it) / 2)) } ?: 0.0

        val projX = (x - config.width / 2.0) / config.scale + centerX
        val projY = (config.height / 2.0 - y) / config.scale + centerY

        return Vector2(projX, projY)
    }
}
