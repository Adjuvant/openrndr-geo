package geo.projection

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import org.locationtech.proj4j.UnknownAuthorityCodeException
import org.openrndr.math.Vector2
import geo.exception.CRSTransformationException

/**
 * Lightweight wrapper around proj4j's CoordinateTransform for CRS transformations.
 *
 * Creates a single CoordinateTransform instance in constructor for reuse across
 * many coordinate transformations (avoids O(n^2) performance from creating in loop).
 *
 * Fails fast with CRSTransformationException if CRS codes are invalid or
 * proj4j-epsg dependency is missing.
 *
 * @property sourceCRS Source CRS identifier (e.g., "EPSG:27700" for BNG)
 * @property targetCRS Target CRS identifier (e.g., "EPSG:4326" for WGS84)
 */
class CRSTransformer(
    sourceCRS: String,
    targetCRS: String
) {
    private val crsFactory = CRSFactory()
    private val transformFactory = CoordinateTransformFactory()

    private val sourceCrs = try {
        crsFactory.createFromName(sourceCRS.lowercase())
            ?: throw CRSTransformationException(
                "Unsupported source CRS: $sourceCRS. " +
                "Ensure proj4j-epsg dependency is included."
            )
    } catch (e: UnknownAuthorityCodeException) {
        throw CRSTransformationException(
            "Unsupported source CRS: $sourceCRS. " +
            "Ensure proj4j-epsg dependency is included."
        )
    }

    private val targetCrs = try {
        crsFactory.createFromName(targetCRS.lowercase())
            ?: throw CRSTransformationException(
                "Unsupported target CRS: $targetCRS. " +
                "Ensure proj4j-epsg dependency is included."
            )
    } catch (e: UnknownAuthorityCodeException) {
        throw CRSTransformationException(
            "Unsupported target CRS: $targetCRS. " +
            "Ensure proj4j-epsg dependency is included."
        )
    }

    private val transform = transformFactory.createTransform(sourceCrs, targetCrs)

    /**
     * Transforms a single coordinate pair.
     *
     * @param x X coordinate (longitude in degrees or easting in meters)
     * @param y Y coordinate (latitude in degrees or northing in meters)
     * @return Transformed coordinates as Vector2
     */
    fun transform(x: Double, y: Double): Vector2 {
        val src = ProjCoordinate(x, y)
        val dst = ProjCoordinate()
        transform.transform(src, dst)
        return Vector2(dst.x, dst.y)
    }
}
