package geo.internal

import geo.Bounds
import geo.Feature
import geo.GeoSource

/**
 * A GeoSource wrapper that holds features with optimized geometries.
 *
 * This internal class is used when `optimize=true` is passed to load functions.
 * It provides the same API as GeoSource but the features contain optimized
 * geometry variants that support batch projection.
 *
 * The optimized geometries are stored as Any (since they don't extend Geometry),
 * so rendering code must check for both standard and optimized variants.
 */
internal class OptimizedGeoSource(
    private val optimizedFeatures: Sequence<OptimizedFeature>,
    crs: String = "EPSG:4326"
) : GeoSource(crs) {

    /**
     * Returns the features as a Sequence<Feature> for API compatibility.
     * Note: The geometries in these features are the optimized variants (stored as Any).
     */
    @Suppress("UNCHECKED_CAST")
    override val features: Sequence<Feature>
        get() = optimizedFeatures.map { it.toFeature() }

    /**
     * Returns the raw optimized features for batch-aware rendering.
     */
    val optimizedFeatureSequence: Sequence<OptimizedFeature> = optimizedFeatures

    /**
     * Returns the bounding box of all features.
     * Computes from optimized geometry bounding boxes.
     */
    override fun boundingBox(): Bounds {
        return optimizedFeatures.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox())
        }
    }

}

/**
 * A feature with an optimized geometry stored as Any.
 */
internal data class OptimizedFeature(
    val optimizedGeometry: Any,
    val properties: Map<String, Any?>
) {
    /**
     * Converts to a standard Feature for API compatibility.
     * The geometry is the optimized variant (stored as Any).
     */
    @Suppress("UNCHECKED_CAST")
    fun toFeature(): Feature {
        return Feature(
            geometry = optimizedGeometry as geo.Geometry,
            properties = properties
        )
    }

    /**
     * Returns the bounding box of this feature's optimized geometry.
     */
    fun boundingBox(): geo.Bounds {
        return when (val geom = optimizedGeometry) {
            is geo.internal.geometry.OptimizedPoint -> geom.boundingBox
            is geo.internal.geometry.OptimizedLineString -> geom.boundingBox
            is geo.internal.geometry.OptimizedPolygon -> geom.boundingBox
            is geo.internal.geometry.OptimizedMultiPoint -> geom.boundingBox
            is geo.internal.geometry.OptimizedMultiLineString -> geom.boundingBox
            is geo.internal.geometry.OptimizedMultiPolygon -> geom.boundingBox
            else -> geo.Bounds.empty()
        }
    }
}
