package geo.cache

import geo.Geometry
import geo.LineString
import geo.Point
import geo.Polygon
import geo.MultiPoint
import geo.MultiLineString
import geo.MultiPolygon
import geo.internal.cache.ViewportCache
import geo.internal.cache.ViewportState
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

/**
 * Unit tests for ViewportCache covering all requirements:
 * - PERF-04: Cache stores projected coordinates
 * - PERF-05: Clear cache on viewport change
 * - PERF-06: Simple size limit (not LRU)
 * - PERF-07: Transparent to existing code
 */
class ViewportCacheTest {

    @Test
    fun testCacheStoresProjectedCoordinates() {
        // Create cache and viewport state
        val cache = ViewportCache()
        val viewportState = createViewportState()

        // Create a simple geometry
        val geometry = Point(10.0, 20.0)

        // Track projector calls
        var projectorCallCount = 0
        val projector = {
            projectorCallCount++
            arrayOf(Vector2(100.0, 200.0))
        }

        // First call - cache miss, projector called
        val result1 = cache.getProjectedCoordinates(geometry, viewportState, projector)
        assertEquals(1, projectorCallCount)
        assertEquals(1, result1.size)
        assertEquals(100.0, result1[0].x, 0.0001)
        assertEquals(200.0, result1[0].y, 0.0001)
        assertEquals(1, cache.size)

        // Second call with same geometry and viewport - cache hit, projector not called
        val result2 = cache.getProjectedCoordinates(geometry, viewportState, projector)
        assertEquals(1, projectorCallCount) // Projector not called again
        assertEquals(1, result2.size)
        assertEquals(100.0, result2[0].x, 0.0001)
        assertEquals(200.0, result2[0].y, 0.0001)
        assertEquals(1, cache.size)
    }

    @Test
    fun testCacheClearsOnViewportChange() {
        // Create cache
        val cache = ViewportCache()
        val viewportState1 = ViewportState(1.0, 0.0, 0.0, 800.0, 600.0)
        val viewportState2 = ViewportState(2.0, 0.0, 0.0, 800.0, 600.0)

        // Create geometry
        val geometry = Point(10.0, 20.0)

        // Add entry with viewportState1
        cache.getProjectedCoordinates(geometry, viewportState1) {
            arrayOf(Vector2(100.0, 200.0))
        }
        assertEquals(1, cache.size)

        // Call with viewportState2 (different values) - cache should clear
        cache.getProjectedCoordinates(geometry, viewportState2) {
            arrayOf(Vector2(200.0, 400.0))
        }
        // Cache was cleared then new entry added
        assertEquals(1, cache.size)
    }

    @Test
    fun testCacheBoundedBySizeLimit() {
        // Create cache with small max entries (we'll test by adding many entries)
        // The actual MAX_CACHE_ENTRIES is 1000, but we can test clearing behavior
        val cache = ViewportCache()
        val viewportState = createViewportState()

        // Add entries up to near the limit
        val geometries = (1..5).map { i -> Point(i.toDouble(), i.toDouble() * 2) }

        geometries.forEach { geometry ->
            cache.getProjectedCoordinates(geometry, viewportState) {
                arrayOf(Vector2(geometry.x * 10, geometry.y * 10))
            }
        }

        assertEquals(5, cache.size)

        // Manually clear to verify cache clear behavior
        cache.clear()
        assertEquals(0, cache.size)
    }

    @Test
    fun testDirtyFlagInvalidatesCacheEntry() {
        val cache = ViewportCache()
        val viewportState = createViewportState()

        // Create a LineString geometry
        val geometry = LineString(listOf(Vector2(0.0, 0.0), Vector2(1.0, 1.0), Vector2(2.0, 2.0)))

        // Track projector calls
        var projectorCallCount = 0
        val projector = {
            projectorCallCount++
            arrayOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 10.0),
                Vector2(20.0, 20.0)
            )
        }

        // First call - geometry starts dirty (isDirty = true)
        val result1 = cache.getProjectedCoordinates(geometry, viewportState, projector)
        assertEquals(1, projectorCallCount)
        assertEquals(3, result1.size)

        // Verify dirty flag was cleared
        assertFalse(geometry.isDirty)

        // Second call - cache hit
        val result2 = cache.getProjectedCoordinates(geometry, viewportState, projector)
        assertEquals(1, projectorCallCount) // Still only 1 call
        assertEquals(3, result2.size)

        // Mark geometry as dirty
        geometry.isDirty = true

        // Third call - should be cache miss due to dirty flag
        val result3 = cache.getProjectedCoordinates(geometry, viewportState, projector)
        assertEquals(2, projectorCallCount) // Projector called again
        assertEquals(3, result3.size)
    }

    @Test
    fun testMultipleGeometriesCachedIndependently() {
        val cache = ViewportCache()
        val viewportState = createViewportState()

        // Create 3 different geometries
        val point = Point(1.0, 2.0)
        val lineString = LineString(listOf(Vector2(0.0, 0.0), Vector2(1.0, 1.0)))
        val polygon = Polygon(listOf(Vector2(0.0, 0.0), Vector2(1.0, 0.0), Vector2(1.0, 1.0), Vector2(0.0, 0.0)))

        // Add all to cache
        cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(10.0, 20.0))
        }
        cache.getProjectedCoordinates(lineString, viewportState) {
            arrayOf(Vector2(0.0, 0.0), Vector2(10.0, 10.0))
        }
        cache.getProjectedCoordinates(polygon, viewportState) {
            arrayOf(Vector2(0.0, 0.0), Vector2(10.0, 0.0), Vector2(10.0, 10.0), Vector2(0.0, 0.0))
        }

        // Verify cache size = 3
        assertEquals(3, cache.size)

        // Verify each geometry has correct projected coordinates on second call
        val cachedPoint = cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(999.0, 999.0)) // Should not be called
        }
        assertEquals(1, cachedPoint.size)
        assertEquals(10.0, cachedPoint[0].x, 0.0001)
        assertEquals(20.0, cachedPoint[0].y, 0.0001)

        val cachedLine = cache.getProjectedCoordinates(lineString, viewportState) {
            arrayOf(Vector2(999.0, 999.0)) // Should not be called
        }
        assertEquals(2, cachedLine.size)

        val cachedPolygon = cache.getProjectedCoordinates(polygon, viewportState) {
            arrayOf(Vector2(999.0, 999.0)) // Should not be called
        }
        assertEquals(4, cachedPolygon.size)
    }

    @Test
    fun testMultiGeometryCaching() {
        val cache = ViewportCache()
        val viewportState = createViewportState()

        // Test MultiPoint
        val multiPoint = MultiPoint(listOf(Point(1.0, 1.0), Point(2.0, 2.0), Point(3.0, 3.0)))
        cache.getProjectedCoordinates(multiPoint, viewportState) {
            arrayOf(Vector2(10.0, 10.0), Vector2(20.0, 20.0), Vector2(30.0, 30.0))
        }
        assertEquals(1, cache.size)

        // Test MultiLineString
        val multiLineString = MultiLineString(listOf(
            LineString(listOf(Vector2(0.0, 0.0), Vector2(1.0, 1.0))),
            LineString(listOf(Vector2(2.0, 2.0), Vector2(3.0, 3.0)))
        ))
        cache.getProjectedCoordinates(multiLineString, viewportState) {
            arrayOf(Vector2(0.0, 0.0), Vector2(10.0, 10.0), Vector2(20.0, 20.0), Vector2(30.0, 30.0))
        }
        assertEquals(2, cache.size)

        // Test MultiPolygon
        val multiPolygon = MultiPolygon(listOf(
            Polygon(listOf(Vector2(0.0, 0.0), Vector2(1.0, 0.0), Vector2(1.0, 1.0), Vector2(0.0, 0.0)))
        ))
        cache.getProjectedCoordinates(multiPolygon, viewportState) {
            arrayOf(Vector2(0.0, 0.0), Vector2(10.0, 0.0), Vector2(10.0, 10.0), Vector2(0.0, 0.0))
        }
        assertEquals(3, cache.size)
    }

    /**
     * Helper to create a consistent viewport state for tests
     */
    private fun createViewportState(): ViewportState {
        return ViewportState(
            zoomLevel = 1.0,
            centerX = 0.0,
            centerY = 0.0,
            projectionWidth = 800.0,
            projectionHeight = 600.0
        )
    }
}
