package geo.internal.cache

import geo.core.Geometry

internal const val DEFAULT_MAX_ENTRIES = 500

/**
 * Composite key combining viewport state and feature key.
 */
private data class CompositeKey<K>(val viewportState: ViewportState, val feature: K)

/**
 * Generic viewport cache implementation with clear-on-change semantics.
 * Stores Map<CompositeKey<K>, V> internally.
 * Clears cache if viewport state changes or size limit exceeded.
 *
 * @param K key type representing the feature reference (Geometry or OptimizedFeature)
 * @param V value type cached (e.g., List<Shape>)
 * @param maxEntries max number of entries before eviction (default 500)
 */
internal class ViewportCache<K, V>(private val maxEntries: Int = DEFAULT_MAX_ENTRIES) {
    private val cache = mutableMapOf<CompositeKey<K>, V>()
    private var currentViewportState: ViewportState? = null
    // Track which feature instances have been cached (for dirty flag support)
    private val cachedInstances = mutableSetOf<K>()

    /**
     * Retrieves a cached value for given feature and viewport state.
     * If missing, uses factory lambda to create and cache.
     * Clears cache completely on viewport state change.
     * Clears entire cache if size limit is exceeded.
     * 
     * For Geometry types with isDirty flag:
     * - Only bypasses cache if the SAME instance (by identity) was previously cached AND is now dirty
     * - New instances with same values as cached ones do NOT bypass (uses value-based cache key)
     * - isDirty is cleared after each cache operation
     *
     * @param feature key representing the feature
     * @param viewportState current viewport state
     * @param factory lambda to create value if missing
     * @return cached or newly created value
     */
    fun get(feature: K, viewportState: ViewportState, factory: () -> V): V {
        if (viewportState != currentViewportState) {
            cache.clear()
            cachedInstances.clear()
            currentViewportState = viewportState
        }

        if (cache.size >= maxEntries) {
            cache.clear()
            cachedInstances.clear()
        }

        val compositeKey = CompositeKey(viewportState, feature)
        
        // Check if this EXACT instance (by identity) has been cached before AND is dirty
        // Use identity check (===) to only bypass for the SAME instance, not new instances with same values
        val isDirty = feature is Geometry && 
                      feature.isDirty && 
                      cachedInstances.any { it === feature }
        
        val result = if (isDirty) {
            // Feature is dirty AND has been cached before (same instance) - bypass cache
            val value = factory()
            cache[compositeKey] = value
            value
        } else {
            // Not dirty or new instance - use cache normally
            val wasInCache = cache.containsKey(compositeKey)
            val value = cache.getOrPut(compositeKey) {
                // Only add to cachedInstances when we actually compute (cache miss)
                cachedInstances.add(feature)
                factory()
            }
            // Track that this instance has been used with the cache (for dirty flag support)
            if (wasInCache) {
                cachedInstances.add(feature)
            }
            value
        }
        
        // Clear dirty flag after successful cache operation
        if (feature is Geometry) {
            feature.isDirty = false
        }
        
        return result
    }

    /** Clears the entire cache manually. */
    fun clear() {
        cache.clear()
        cachedInstances.clear()
        currentViewportState = null
    }

    /** Current cache size for monitoring/testing. */
    val size: Int
        get() = cache.size
}
