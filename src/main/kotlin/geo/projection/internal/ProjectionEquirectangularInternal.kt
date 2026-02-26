package geo.projection.internal

import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import org.openrndr.math.Vector2

class ProjectionEquirectangularInternal(
    private val config: ProjectionConfig
) : GeoProjection {

    override fun project(latLng: Vector2): Vector2 {
        val lat = latLng.y
        val lon = latLng.x

        val normalizedLon = normalizeLongitude(lon)
        val centerLon = config.center?.x ?: 0.0
        val centerLat = config.center?.y ?: 0.0
        val scale = config.scale

        val x = config.width / 2.0 + (normalizedLon - centerLon) * (config.width / 360.0) * scale
        val y = config.height / 2.0 - (lat - centerLat) * (config.height / 180.0) * scale

        return Vector2(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        val centerLon = config.center?.x ?: 0.0
        val centerLat = config.center?.y ?: 0.0
        val scale = config.scale

        val lon = centerLon + (screen.x - config.width / 2.0) / (config.width / 360.0) / scale
        val lat = centerLat - (screen.y - config.height / 2.0) / (config.height / 180.0) / scale
        return Vector2(lon, lat)
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionEquirectangularInternal(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        // For equirectangular, zoomLevel 0 = full world view
        return ProjectionEquirectangularInternal(config.copy(center = Vector2(0.0, 0.0), zoomLevel = 0.0))
    }

    private fun normalizeLongitude(longitude: Double): Double {
        var normalized = ((longitude % 360.0) + 360.0) % 360.0
        if (normalized > 180.0) normalized -= 360.0
        return normalized
    }
}
