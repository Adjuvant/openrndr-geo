package geo.internal.cache

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

    /**
     * Retrieves a cached value for given feature and viewport state.
     * If missing, uses factory lambda to create and cache.
     * Clears cache completely on viewport state change.
     * Clears entire cache if size limit is exceeded.
     *
     * @param feature key representing the feature
     * @param viewportState current viewport state
     * @param factory lambda to create value if missing
     * @return cached or newly created value
     */
    fun get(feature: K, viewportState: ViewportState, factory: () -> V): V {
        if (viewportState != currentViewportState) {
            cache.clear()
            currentViewportState = viewportState
        }

        if (cache.size >= maxEntries) {
            cache.clear()
        }

        val compositeKey = CompositeKey(viewportState, feature)
        return cache.getOrPut(compositeKey, factory)
    }

    /** Clears the entire cache manually. */
    fun clear() {
        cache.clear()
        currentViewportState = null
    }

    /** Current cache size for monitoring/testing. */
    val size: Int
        get() = cache.size
}
