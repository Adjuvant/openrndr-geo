package geo.core

import geo.internal.OptimizedGeoSource
import geo.internal.cache.ViewportCache
import geo.internal.cache.ViewportState
import geo.projection.GeoProjection

/**
 * A GeoSource wrapper that provides automatic viewport-based caching.
 *
 * CachedGeoSource wraps any GeoSource and integrates with the drawer.geo()
 * rendering pipeline to automatically cache projected coordinates. This
 * eliminates redundant coordinate transformations when the viewport hasn't
 * changed, providing significant performance improvements.
 *
 * ## Usage
 * ```kotlin
 * val data = loadGeo("world.json")  // Returns CachedGeoSource
 * drawer.geo(data)  // Automatically uses viewport caching
 * ```
 *
 * ## Caching Behavior
 * - Cache key includes geometry identity and viewport state
 * - Cache is cleared automatically when viewport changes
 * - Manual clear available via [clearCache()]
 *
 * ## Integration
 * CachedGeoSource integrates seamlessly with:
 * - [drawer.geo()][geo.render.geo] - Uses cache automatically
 * - [projectToFit()][projectToFit] - Creates projections that work with cache
 *
 * @property delegate The underlying GeoSource being wrapped
 */
class CachedGeoSource(
    val delegate: GeoSource
) : GeoSource(delegate.crs) {

    /**
     * Internal viewport cache for projected coordinates.
     * Shared across all CachedGeoSource instances.
     */
    private val viewportCache = ViewportCache<Any, Any>()

    /**
     * Features from the underlying source.
     * Materializes to List for safe multiple iterations with caching.
     */
    override val features: Sequence<Feature> = delegate.materialize().features

    /**
     * Returns the bounding box of all features.
     * Delegates to the underlying source.
     */
    override fun boundingBox(): Bounds = delegate.boundingBox()

    /**
     * Returns the total bounding box of all features.
     * Delegates to the underlying source.
     */
    override fun totalBoundingBox(): Bounds = delegate.totalBoundingBox()

    /**
     * Returns the count of features in this source.
     * Delegates to the underlying source.
     */
    override fun countFeatures(): Long = delegate.countFeatures()

    /**
     * Check if empty - delegates to underlying source.
     * Note: isEmpty() is final in GeoSource, use this method instead.
     */
    fun checkEmpty(): Boolean = delegate.features.none()

    /**
     * Clear the viewport cache to free memory.
     *
     * Call this when:
     * - Memory usage is a concern
     * - Switching between different data sources
     * - After panning/zooming operations that won't be revisited
     */
    fun clearCache() {
        viewportCache.clear()
    }

    /**
     * Get the current cache size (number of cached geometries).
     * Useful for debugging and monitoring memory usage.
     */
    fun cacheSize(): Int = viewportCache.size

    /**
     * Internal access to the viewport cache for drawer.geo() integration.
     * Not intended for direct use - used by rendering pipeline.
     */
    internal fun getViewportCache(): ViewportCache<Any, Any> = viewportCache
}
