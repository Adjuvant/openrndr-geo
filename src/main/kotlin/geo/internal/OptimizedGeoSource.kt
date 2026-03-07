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
 *
 * IMPORTANT: Optimized geometries do NOT extend Geometry sealed class, so they
 * cannot be converted to standard Feature objects. Use optimizedFeatureSequence
 * for direct access to optimized data.
 */
internal class OptimizedGeoSource(
    private val optimizedFeatures: Sequence<OptimizedFeature>,
    crs: String = "EPSG:4326"
) : GeoSource(crs) {

    /**
     * Returns the raw optimized features for batch-aware rendering.
     * This is the primary access method - optimized features cannot be converted
     * to standard Feature objects due to type system constraints.
     */
    val optimizedFeatureSequence: Sequence<OptimizedFeature> = optimizedFeatures

    /**
     * NOT SUPPORTED: Optimized geometries cannot be converted to Feature objects.
     * Attempting to access this property will throw an exception.
     * Use optimizedFeatureSequence instead.
     */
    override val features: Sequence<Feature>
        get() = throw UnsupportedOperationException(
            "OptimizedGeoSource does not support standard Feature access. " +
            "Use optimizedFeatureSequence for batch-optimized rendering, " +
            "or load without optimize=true for standard Feature access."
        )

    /**
     * Returns the bounding box of all features.
     * Computes from optimized geometry bounding boxes.
     */
    override fun boundingBox(): Bounds {
        return optimizedFeatures.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox())
        }
    }

    /**
     * Returns the total bounding box of all features.
     * Alias for boundingBox() - computes from optimized geometry bounding boxes.
     */
    override fun totalBoundingBox(): Bounds {
        return boundingBox()
    }

    /**
     * Returns the count of features in this source.
     * Iterates through the optimized features directly.
     */
    override fun countFeatures(): Long {
        return optimizedFeatures.count().toLong()
    }

}

/**
 * A feature with an optimized geometry stored as Any.
 * 
 * Optimized features cannot be converted to standard Feature objects because
 * optimized geometries don't extend the Geometry sealed class (package restriction).
 * Use directly with batch-aware rendering code.
 */
internal data class OptimizedFeature(
    val optimizedGeometry: Any,
    val properties: Map<String, Any?>
) {
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
