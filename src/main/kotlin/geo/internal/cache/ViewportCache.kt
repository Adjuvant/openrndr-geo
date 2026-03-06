package geo.internal.cache

import geo.Geometry
import org.openrndr.math.Vector2

/**
 * Maximum number of entries in the viewport cache.
 * When exceeded, the entire cache is cleared.
 * This simple size limit avoids complex LRU/LFU logic.
 */
internal const val MAX_CACHE_ENTRIES = 1000

/**
 * Viewport-based cache for projected geometry coordinates.
 *
 * Implements clear-on-change semantics:
 * - Entire cache clears when viewport state changes (zoom, pan, projection size)
 * - Individual entries clear when geometry dirty flag is set
 * - Simple size limit enforcement (clears all when exceeded)
 *
 * Uses MutableMap for storage - OpenRNDR rendering is single-threaded,
 * so no ConcurrentHashMap or synchronization is needed.
 *
 * Per 12-CONTEXT.md: No external caching libraries (Caffeine/Aedile),
 * no LRU/LFU eviction, just simple MutableMap with clear-on-change.
 */
internal class ViewportCache {
    private val cache = mutableMapOf<CacheKey, Array<Vector2>>()
    private var currentViewportState: ViewportState? = null

    /**
     * Gets projected coordinates for a geometry, using cache if available.
     *
     * Logic:
     * 1. If viewport state changed: clear entire cache, update current state
     * 2. If geometry is dirty: remove this geometry's entry from cache, clear dirty flag
     * 3. If cache size >= MAX_CACHE_ENTRIES: clear entire cache
     * 4. Use getOrPut to return cached value or compute and store
     *
     * @param geometry The geometry to project
     * @param viewportState The current viewport state
     * @param projector Lambda that computes projected coordinates when cache miss
     * @return Array of projected Vector2 coordinates
     */
    fun getProjectedCoordinates(
        geometry: Geometry,
        viewportState: ViewportState,
        projector: () -> Array<Vector2>
    ): Array<Vector2> {
        // Step 1: Clear cache on viewport change
        if (viewportState != currentViewportState) {
            cache.clear()
            currentViewportState = viewportState
        }

        // Step 2: Check dirty flag and invalidate specific entry
        if (geometry.isDirty) {
            val dirtyKey = CacheKey(viewportState, geometry)
            cache.remove(dirtyKey)
            geometry.isDirty = false
        }

        // Step 3: Check size limit
        if (cache.size >= MAX_CACHE_ENTRIES) {
            cache.clear()
        }

        // Step 4: Get from cache or compute
        val key = CacheKey(viewportState, geometry)
        return cache.getOrPut(key) { projector() }
    }

    /**
     * Manually clears the entire cache.
     * Useful for testing or when explicit cache invalidation is needed.
     */
    fun clear() {
        cache.clear()
        currentViewportState = null
    }

    /**
     * Current number of entries in the cache.
     * Exposed primarily for testing and monitoring.
     */
    val size: Int
        get() = cache.size
}
