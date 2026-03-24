package geo.internal.cache

import org.junit.Assert.*
import org.junit.Test
import org.openrndr.math.Vector2
import geo.core.Geometry
import geo.core.Point
import geo.core.LineString
import geo.core.Polygon
import geo.core.MultiPolygon

/**
 * Integration tests for ViewportCache.
 * 
 * Tests cache behavior across multiple viewports, size limits,
 * shared cache instances, and eviction policies.
 */
class ViewportCacheIntegrationTest {

    /**
     * Helper to create ViewportState with configurable parameters.
     */
    private fun createViewport(
        zoomLevel: Double = 1.0,
        centerX: Double = 0.0,
        centerY: Double = 0.0,
        width: Double = 800.0,
        height: Double = 600.0
    ): ViewportState {
        return ViewportState(zoomLevel, centerX, centerY, width, height)
    }

    // --- Cache Hit/Miss Behavior ---

    @Test
    fun `cache returns same value on same viewport and key`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        val geometry = Point(0.0, 0.0)
        val computedValue = "projected_result"

        var factoryCallCount = 0
        val factory = { 
            factoryCallCount++
            computedValue 
        }

        val result1 = cache.get(geometry, viewport, factory)
        val result2 = cache.get(geometry, viewport, factory)

        assertSame("Should return same instance", result1, result2)
        assertEquals("Factory should only be called once", 1, factoryCallCount)
    }

    @Test
    fun `cache miss when viewport changes`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport1 = createViewport(zoomLevel = 1.0)
        val viewport2 = createViewport(zoomLevel = 2.0)
        val geometry = Point(0.0, 0.0)

        var factoryCallCount = 0
        val factory = { 
            factoryCallCount++
            "result" 
        }

        cache.get(geometry, viewport1, factory)
        assertEquals("First call should invoke factory", 1, factoryCallCount)

        cache.get(geometry, viewport2, factory)
        assertEquals("Different viewport should invoke factory again", 2, factoryCallCount)
    }

    @Test
    fun `cache miss when geometry changes`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        val geometry1 = Point(0.0, 0.0)
        val geometry2 = Point(1.0, 1.0)

        var factoryCallCount = 0
        val factory = { 
            factoryCallCount++
            "result" 
        }

        cache.get(geometry1, viewport, factory)
        assertEquals("First geometry should invoke factory", 1, factoryCallCount)

        cache.get(geometry2, viewport, factory)
        assertEquals("Different geometry should invoke factory again", 2, factoryCallCount)
    }

    // --- Eviction on Viewport Change ---

    @Test
    fun `cache clears when viewport state changes`() {
        val cache = ViewportCache<Geometry, String>()
        
        // Add entries with viewport1
        val viewport1 = createViewport(zoomLevel = 1.0)
        val geom1 = Point(0.0, 0.0)
        cache.get(geom1, viewport1) { "value1" }
        assertEquals("Cache should have 1 entry", 1, cache.size)

        // Add entry with viewport2 (different zoom)
        val viewport2 = createViewport(zoomLevel = 2.0)
        val geom2 = Point(1.0, 1.0)
        cache.get(geom2, viewport2) { "value2" }
        
        // Cache should be cleared and have only the new entry
        assertEquals("Cache should only have new entry after viewport change", 1, cache.size)
    }

    @Test
    fun `cache clears when center changes`() {
        val cache = ViewportCache<Geometry, String>()
        
        val viewport1 = createViewport(centerX = 0.0, centerY = 0.0)
        cache.get(Point(0.0, 0.0), viewport1) { "centered_at_origin" }
        assertEquals(1, cache.size)

        val viewport2 = createViewport(centerX = 10.0, centerY = 10.0)
        cache.get(Point(0.0, 0.0), viewport2) { "centered_at_10_10" }
        
        assertEquals("Cache should clear on center change", 1, cache.size)
    }

    @Test
    fun `cache preserves entries when same viewport reused`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        
        val geometries = listOf(
            Point(0.0, 0.0),
            Point(1.0, 1.0),
            LineString(listOf(Vector2(0.0, 0.0), Vector2(1.0, 1.0)))
        )

        // Add multiple entries with same viewport
        geometries.forEachIndexed { index, geom ->
            cache.get(geom, viewport) { "value_$index" }
        }

        assertEquals("Cache should have all entries", 3, cache.size)

        // Access them again - should hit cache
        var factoryCalls = 0
        geometries.forEachIndexed { index, geom ->
            val result = cache.get(geom, viewport) { 
                factoryCalls++
                "new_value_$index" 
            }
            assertEquals("Should return cached value", "value_$index", result)
        }

        assertEquals("Factory should not be called for cached entries", 0, factoryCalls)
    }

    // --- Size Limit Enforcement ---

    @Test
    fun `cache enforces size limit of 500 entries`() {
        val cache = ViewportCache<Geometry, String>(maxEntries = 500)
        val viewport = createViewport()

        // Add exactly 500 entries
        repeat(500) { i ->
            cache.get(Point(i.toDouble(), i.toDouble()), viewport) { "value_$i" }
        }
        assertEquals("Cache should have 500 entries", 500, cache.size)

        // Adding one more should trigger eviction (clear all)
        cache.get(Point(-1.0, -1.0), viewport) { "eviction_trigger" }
        assertEquals("Cache should have only new entry after eviction", 1, cache.size)
    }

    @Test
    fun `cache clears when reaching max size`() {
        val cache = ViewportCache<Geometry, String>(maxEntries = 3)
        val viewport = createViewport()

        // Add 3 entries
        cache.get(Point(0.0, 0.0), viewport) { "v0" }
        cache.get(Point(1.0, 1.0), viewport) { "v1" }
        cache.get(Point(2.0, 2.0), viewport) { "v2" }
        assertEquals(3, cache.size)

        // Adding 4th entry clears cache
        cache.get(Point(3.0, 3.0), viewport) { "v3" }
        assertEquals("Cache should have only 1 entry after clear", 1, cache.size)

        // All previous entries should be gone
        val cachedValue = cache.get(Point(0.0, 0.0), viewport) { "NOT_CACHED" }
        assertEquals("Should compute new value since cache was cleared", "NOT_CACHED", cachedValue)
    }

    @Test
    fun `cache size resets after eviction`() {
        val cache = ViewportCache<Geometry, String>(maxEntries = 5)
        val viewport = createViewport()

        // Fill to capacity
        repeat(5) { i ->
            cache.get(Point(i.toDouble(), i.toDouble()), viewport) { "v$i" }
        }
        assertEquals(5, cache.size)

        // 6th entry triggers eviction (clears when size >= 5)
        cache.get(Point(100.0, 100.0), viewport) { "trigger" }
        assertEquals(1, cache.size)

        // Adding 4 more entries: size goes 1 -> 2 -> 3 -> 4 -> 5
        // No eviction until 6th entry
        repeat(4) { i ->
            cache.get(Point((200 + i).toDouble(), (200 + i).toDouble()), viewport) { "v2_$i" }
        }
        assertEquals("Should have 5 entries (4 new + 1 from trigger, all different points)", 5, cache.size)
        
        // 5th new entry (6th total after trigger) triggers eviction
        cache.get(Point(300.0, 300.0), viewport) { "fifth" }
        assertEquals("6th entry after trigger should trigger eviction", 1, cache.size)
    }

    // --- Shared Cache Instance Behavior ---

    @Test
    fun `cache instances are independent`() {
        val cache1 = ViewportCache<Geometry, String>()
        val cache2 = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        val geometry = Point(0.0, 0.0)

        cache1.get(geometry, viewport) { "from_cache1" }
        cache2.get(geometry, viewport) { "from_cache2" }

        assertEquals("Each cache should have independent storage", 1, cache1.size)
        assertEquals(1, cache2.size)
    }

    @Test
    fun `clear manually empties cache`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()

        repeat(5) {
            cache.get(Point(it.toDouble(), it.toDouble()), viewport) { "v$it" }
        }
        assertEquals(5, cache.size)

        cache.clear()
        assertEquals("Cache should be empty after clear", 0, cache.size)
    }

    @Test
    fun `clear resets viewport state tracking`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport1 = createViewport(zoomLevel = 1.0)
        val viewport2 = createViewport(zoomLevel = 2.0)

        cache.get(Point(0.0, 0.0), viewport1) { "v1" }
        assertEquals(1, cache.size)

        cache.clear()
        assertEquals("Cache should be empty after clear", 0, cache.size)

        // After clear, viewport state is reset to null
        // Adding with viewport1 triggers clear (viewport1 != null), then adds entry
        cache.get(Point(1.0, 1.0), viewport1) { "v1_again" }
        assertEquals("Should have one entry after clear and add", 1, cache.size)

        // Adding with viewport2 triggers clear (viewport2 != viewport1), then adds entry
        cache.get(Point(2.0, 2.0), viewport2) { "v2" }
        assertEquals("Should have one entry (previous cleared on viewport change)", 1, cache.size)
    }

    // --- Composite Key Behavior ---

    @Test
    fun `composite key equality depends on viewport and geometry value`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport1 = createViewport(zoomLevel = 1.0)
        val geometry = Point(0.0, 0.0)

        // First call with geometry - computes and caches
        val result1 = cache.get(geometry, viewport1) { "first" }
        assertEquals("first", result1)

        // Same geometry instance on same viewport - returns cached value
        val result2 = cache.get(geometry, viewport1) { "second" }
        assertEquals("first", result2) // Still returns first cached value

        // Same viewport but different geometry value - computes new value
        val geometrySameCoords = Point(0.0, 0.0) // Different instance, same values
        val result3 = cache.get(geometrySameCoords, viewport1) { "third" }
        // Since Point has value equality, this uses the same cache entry
        assertEquals("first", result3)
    }

    // --- Different Geometry Types ---

    @Test
    fun `cache works with Polygon geometry`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        
        val polygon = Polygon(
            listOf(Vector2(0.0, 0.0), Vector2(10.0, 0.0), Vector2(10.0, 10.0), Vector2(0.0, 10.0)),
            emptyList()
        )

        val result = cache.get(polygon, viewport) { "polygon_projected" }
        assertEquals("polygon_projected", result)
        assertEquals(1, cache.size)

        // Second access should be cached
        val cached = cache.get(polygon, viewport) { "SHOULD_NOT_BE_CALLED" }
        assertEquals("polygon_projected", cached)
    }

    @Test
    fun `cache works with MultiPolygon geometry`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()
        
        // Simple multipolygon with one polygon
        val multipolygon = MultiPolygon(
            listOf(
                Polygon(
                    listOf(Vector2(0.0, 0.0), Vector2(5.0, 0.0), Vector2(5.0, 5.0), Vector2(0.0, 5.0)),
                    emptyList()
                )
            )
        )

        val result = cache.get(multipolygon, viewport) { "multipolygon_projected" }
        assertEquals("multipolygon_projected", result)
    }

    // --- Viewport State Details ---

    @Test
    fun `viewport state equality depends on all fields`() {
        val base = createViewport(zoomLevel = 1.0, centerX = 0.0, centerY = 0.0, width = 800.0, height = 600.0)
        
        val same = createViewport(zoomLevel = 1.0, centerX = 0.0, centerY = 0.0, width = 800.0, height = 600.0)
        assertEquals("Same parameters should equal", base, same)

        val differentZoom = createViewport(zoomLevel = 2.0, centerX = 0.0, centerY = 0.0, width = 800.0, height = 600.0)
        assertNotEquals("Different zoom should not equal", base, differentZoom)

        val differentCenterX = createViewport(zoomLevel = 1.0, centerX = 10.0, centerY = 0.0, width = 800.0, height = 600.0)
        assertNotEquals("Different centerX should not equal", base, differentCenterX)

        val differentCenterY = createViewport(zoomLevel = 1.0, centerX = 0.0, centerY = 10.0, width = 800.0, height = 600.0)
        assertNotEquals("Different centerY should not equal", base, differentCenterY)

        val differentWidth = createViewport(zoomLevel = 1.0, centerX = 0.0, centerY = 0.0, width = 1024.0, height = 600.0)
        assertNotEquals("Different width should not equal", base, differentWidth)

        val differentHeight = createViewport(zoomLevel = 1.0, centerX = 0.0, centerY = 0.0, width = 800.0, height = 768.0)
        assertNotEquals("Different height should not equal", base, differentHeight)
    }

    // --- Edge Cases ---

    @Test
    fun `cache can store and retrieve empty string values`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()

        // Empty string is a valid cached value
        val result = cache.get(Point(0.0, 0.0), viewport) { "" }
        assertEquals("", result)

        // Second access should return cached empty string
        val cached = cache.get(Point(0.0, 0.0), viewport) { "SHOULD_NOT_BE_CALLED" }
        assertEquals("", cached)
    }

    @Test
    fun `cache handles empty geometry collection`() {
        val cache = ViewportCache<Geometry, String>()
        val viewport = createViewport()

        // Should work fine even with edge cases
        cache.get(Point(Double.MIN_VALUE, Double.MIN_VALUE), viewport) { "min" }
        cache.get(Point(Double.MAX_VALUE, Double.MAX_VALUE), viewport) { "max" }
        
        assertEquals("Should cache both extreme values", 2, cache.size)
    }

    @Test
    fun `cache returns correct value after complex viewport sequence`() {
        val cache = ViewportCache<Geometry, String>()
        
        // Sequence: add with viewport1, viewport2, back to viewport1
        val viewport1 = createViewport(zoomLevel = 1.0)
        val viewport2 = createViewport(zoomLevel = 2.0)
        val geometry = Point(0.0, 0.0)

        cache.get(geometry, viewport1) { "v1" }
        cache.get(geometry, viewport2) { "v2" }
        
        // Back to viewport1 - cache was cleared when viewport2 was set
        // So this should compute a new value
        val result = cache.get(geometry, viewport1) { "v1_again" }
        
        // Since viewport1 -> viewport2 cleared the cache, v1_again is the expected value
        assertEquals("Should compute new value after viewport switch", "v1_again", result)
    }
}
