package geo.internal.cache

import org.junit.Assert.*
import org.junit.Test
import org.openrndr.math.Vector2
import geo.Geometry
import geo.Point
import geo.LineString
import geo.internal.cache.ViewportCache
import geo.internal.cache.ViewportState

class ViewportCacheTest {

    @Test
    fun testCacheBasicOperations() {
        val cache = ViewportCache<Geometry, Array<Vector2>>()
        val viewportState = ViewportState(1.0, 0.0, 0.0, 100.0, 100.0)

        val geometry = Point(0.0, 0.0)

        val projectedPoints = arrayOf(Vector2(1.0, 1.0), Vector2(2.0, 2.0))

        val first = cache.get(geometry, viewportState) { projectedPoints }
        assertArrayEquals(projectedPoints, first)

        val second = cache.get(geometry, viewportState) { arrayOf() }
        assertArrayEquals(projectedPoints, second) // Cached value returned
    }

    @Test
    fun testCacheClearsOnViewportChange() {
        val cache = ViewportCache<Geometry, Array<Vector2>>()
        val viewport1 = ViewportState(1.0, 0.0, 0.0, 100.0, 100.0)
        val viewport2 = ViewportState(2.0, 0.0, 0.0, 100.0, 100.0)

        val geometry = Point(0.0, 0.0)

        val projected1 = arrayOf(Vector2(1.0, 1.0))
        val projected2 = arrayOf(Vector2(2.0, 2.0))

        cache.get(geometry, viewport1) { projected1 }
        val afterChange = cache.get(geometry, viewport2) { projected2 }

        assertArrayEquals(projected2, afterChange)
    }

    @Test
    fun testCacheEvictionBySize() {
        val cache = ViewportCache<Geometry, Array<Vector2>>(10)
        val viewportState = ViewportState(1.0, 0.0, 0.0, 100.0, 100.0)

        repeat(10) { i ->
            val geometry = Point(i.toDouble(), i.toDouble())
            val projected = arrayOf(Vector2(i.toDouble(), i.toDouble()))
            cache.get(geometry, viewportState) { projected }
        }

        // Adding one more causes cache clear
        val freshGeom = Point(42.0, 42.0)
        val freshProj = arrayOf(Vector2(42.0, 42.0))

        val result = cache.get(freshGeom, viewportState) { freshProj }
        assertArrayEquals(freshProj, result)
    }
}
