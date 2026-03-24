package geo.projection

import geo.exception.AccuracyWarningException
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import org.openrndr.math.Vector2

/**
 * British National Grid projection using EPSG:27700 (OSGB36).
 *
 * Uses proj4j for CRS transformation from WGS84 (EPSG:4326).
 * ~3-5m accuracy with Helmert transformation (good for creative coding).
 *
 * For ~1cm accuracy (OSTN15), use PROJ C library or external Grid InQuestII.
 *
 * Note: Only valid for UK grid area. Throws AccuracyWarningException outside.
 *
 * DSL usage:
 * ```kotlin
 * val bng = ProjectionBNG {
 *     width = 800
 *     height = 600
 * }
 * val uk = latLng(-0.1, 51.5).toBNG() // Longitude, Latitude
 * ```
 */
class ProjectionBNG(
    private val _config: ProjectionConfig
) : GeoProjection {

    companion object {
        // CRS definitions
        private val crsFactory = CRSFactory()
        private val WGS84 = crsFactory.createFromName("epsg:4326")   // Lat/lng
        private val BNG = crsFactory.createFromName("epsg:27700")    // British National Grid

        // Transform factory (created once, reused)
        private val transformFactory = CoordinateTransformFactory()
        private val wgsToBng = transformFactory.createTransform(WGS84, BNG)
        private val bngToWgs = transformFactory.createTransform(BNG, WGS84)

        // UK bounds (easting: 0-700km, northing: 0-1300km)
        private const val MIN_EASTING = 0.0
        private const val MAX_EASTING = 700000.0
        private const val MIN_NORTHING = 0.0
        private const val MAX_NORTHING = 1300000.0

        /**
         * Create ProjectionBNG with DSL configuration.
         */
        operator fun invoke(block: ProjectionConfigBuilder.() -> Unit): ProjectionBNG {
            val builder = ProjectionConfigBuilder()
            builder.block()
            return ProjectionBNG(builder.build())
        }

        /**
         * Transform lat/lng to BNG (convenience method).
         * @param latLng Vector2 (x=longitude, y=latitude)
         * @return BNG coordinates as Vector2 (x=easting, y=northing) in meters
         */
        fun latLngToBNG(latLng: Vector2): Vector2 {
            val point = ProjCoordinate(latLng.x, latLng.y) // lon, lat
            val result = ProjCoordinate()
            wgsToBng.transform(point, result)
            return Vector2(result.x, result.y)
        }

        /**
         * Transform BNG to lat/lng (convenience method).
         * @param bng Vector2 (x=easting, y=northing) in meters
         * @return Geographic coordinates as Vector2 (x=longitude, y=latitude)
         */
        fun bngToLatLng(bng: Vector2): Vector2 {
            val point = ProjCoordinate(bng.x, bng.y) // easting, northing
            val result = ProjCoordinate()
            bngToWgs.transform(point, result)
            return Vector2(result.x, result.y)
        }
    }

    override fun project(latLng: Vector2): Vector2 {
        val bng = latLngToBNG(latLng)

        // Validate BNG coordinates are within UK grid
        if (bng.x < MIN_EASTING || bng.x > MAX_EASTING ||
            bng.y < MIN_NORTHING || bng.y > MAX_NORTHING) {
            throw AccuracyWarningException(
                "BNG coordinates (${bng.x}, ${bng.y}) are outside UK grid area. " +
                "Valid range: easting [0, 700000], northing [0, 1300000]. " +
                "BNG with Helmert transformation has ~3-5m accuracy."
            )
        }

        // Project BNG to screen (easting/northing in meters)
        val x = (bng.x / MAX_EASTING) * _config.width
        val y = _config.height - (bng.y / MAX_NORTHING) * _config.height

        return Vector2(x, y)
    }

    override fun unproject(screen: Vector2): Vector2 {
        // De-project screen to BNG
        val easting = (screen.x / _config.width) * MAX_EASTING
        val northing = ((_config.height - screen.y) / _config.height) * MAX_NORTHING

        // Transform BNG to lat/lng
        return bngToLatLng(Vector2(easting, northing))
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionBNG(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        // No fitWorld for BNG (UK-only)
        return ProjectionBNG(config)
    }
}
