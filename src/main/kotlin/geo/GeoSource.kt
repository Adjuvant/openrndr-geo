package geo

import geo.projection.CRSTransformer

/**
 * Abstract base class for all geospatial data sources.
 * GeoSource provides a unified interface for accessing features from
 * different data formats (GeoJSON, GeoPackage, etc.).
 *
 * @property crs The Coordinate Reference System identifier (default: "EPSG:4326" for WGS84)
 */
abstract class GeoSource(
    val crs: String = "EPSG:4326" // WGS84 default
) {
    /**
     * The features in this data source as a lazy Sequence.
     * Using Sequence enables memory-efficient processing of large datasets.
     */
    abstract val features: Sequence<Feature>

    /**
     * Returns all features as a List.
     * Note: This loads all features into memory - use with caution for large datasets.
     */
    fun listFeatures(): List<Feature> = features.toList()

    /**
     * Returns the count of features in this source.
     * Default implementation iterates through all features.
     * Subclasses may provide more efficient implementations.
     */
    open fun countFeatures(): Long = features.count().toLong()

    /**
     * Filters features by a predicate.
     *
     * @param predicate The filter condition
     * @return A sequence of features matching the predicate
     */
    fun filterFeatures(predicate: (Feature) -> Boolean): Sequence<Feature> {
        return features.filter(predicate)
    }

    /**
     * Returns features that intersect the given bounding box.
     *
     * @param bounds The bounding box to test against
     * @return A sequence of features whose bounding boxes intersect the given bounds
     */
    fun featuresInBounds(bounds: Bounds): Sequence<Feature> {
        return features.filter { it.boundingBox.intersects(bounds) }
    }

    /**
     * Returns the bounding box of all features in this source.
     * Returns empty bounds if there are no features.
     */
    fun totalBoundingBox(): Bounds {
        return features.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox)
        }
    }

    /**
     * Transforms this data source to a different CRS.
     * Creates a new GeoSource with lazy transformed Sequence.
     *
     * Identity optimization: Returns this instance if source CRS equals target CRS.
     *
     * @param targetCRS The target CRS identifier (e.g., "EPSG:4326")
     * @return A GeoSource in the target CRS (same instance if CRS matches)
     */
    open fun autoTransformTo(targetCRS: String): GeoSource {
        if (crs == targetCRS) return this  // Identity optimization

        val transformer = CRSTransformer(crs, targetCRS)

        return object : GeoSource(targetCRS) {
            override val features: Sequence<Feature> =
                this@GeoSource.features.map { feature ->
                    Feature(
                        geometry = feature.geometry.transform(transformer),
                        properties = feature.properties
                    )
                }
        }
    }

    /**
     * Materializes lazy sequences into in-memory lists.
     * Use this for render loops where features are accessed multiple times.
     *
     * Tradeoff: Lazy = per-frame CRS transform cost; eager = upfront cost + memory.
     *
     * @return A new GeoSource backed by in-memory List
     */
    fun materialize(): GeoSource {
        val materializedFeatures = listFeatures()

        return object : GeoSource(crs) {
            override val features: Sequence<Feature> = materializedFeatures.asSequence()
        }
    }

    /**
     * Returns true if this source contains no features.
     */
    fun isEmpty(): Boolean = !features.any()

    /**
     * Returns the CRS identifier.
     */
    fun getCRS(): String = crs
}
