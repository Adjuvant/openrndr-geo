package geo.projection.internal

import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import org.openrndr.math.Vector2
import kotlin.math.pow

class ProjectionEquirectangularInternal(
    private val config: ProjectionConfig
) : GeoProjection {

    /**
     * Calculate base scale for equirectangular projection.
     * At zoom=0, world (360° x 180°) should fit in viewport.
     * baseScale = 1.0 (1 degree = 1 unit at zoom 0)
     */
    private fun equirectangularBaseScale(): Double = 1.0

    override fun project(latLng: Vector2): Vector2 {
        val lat = latLng.y
        val lon = latLng.x

        val normalizedLon = normalizeLongitude(lon)
        val centerLon = config.center?.x ?: 0.0
        val centerLat = config.center?.y ?: 0.0
        
        // For equirectangular: scale = baseScale * 2^(-zoom) where baseScale = 1.0
        // zoom=0: scale=1 (world fits), zoom=1: scale=0.5 (2x zoomed in)
        val scale = equirectangularBaseScale() * 2.0.pow(-config.zoomLevel)

        val x = config.width / 2.0 + (normalizedLon - centerLon) * (config.width / 360.0) * scale
        val y = config.height / 2.0 - (lat - centerLat) * (config.height / 180.0) * scale

        return Vector2(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        val centerLon = config.center?.x ?: 0.0
        val centerLat = config.center?.y ?: 0.0
        
        // For equirectangular: scale = baseScale * 2^(-zoom) where baseScale = 1.0
        val scale = equirectangularBaseScale() * 2.0.pow(-config.zoomLevel)

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
