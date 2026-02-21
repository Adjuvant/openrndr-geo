package geo.projection.internal

import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import org.openrndr.math.Vector2

/**
 * Internal Equirectangular projection implementation.
 * Simple linear mapping: lon [-180, 180] → [0, width], lat [-90, 90] → [height, 0].
 */
class ProjectionEquirectangularInternal(
    private val config: ProjectionConfig
) : GeoProjection {

    override fun project(latLng: Vector2): Vector2 {
        val lat = latLng.y
        val lon = latLng.x

        // Normalize longitude to [-180, 180]
        val normalizedLon = normalizeLongitude(lon)

        // Linear mapping
        val x = (normalizedLon + 180.0) / 360.0 * config.width
        val y = config.height - ((lat + 90.0) / 180.0 * config.height)

        return Vector2(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        val lon = (screen.x / config.width) * 360.0 - 180.0
        val lat = 90.0 - (screen.y / config.height) * 180.0
        return Vector2(lon, lat)
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionEquirectangularInternal(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        return ProjectionEquirectangularInternal(config)
    }

    private fun normalizeLongitude(longitude: Double): Double {
        // Map to [-180, 180] (CONTEXT.md decision: normalize automatically)
        var normalized = ((longitude % 360.0) + 360.0) % 360.0
        if (normalized > 180.0) normalized -= 360.0
        return normalized
    }
}
