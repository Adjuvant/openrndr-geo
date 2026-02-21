package geo

import org.junit.Test
import org.junit.Assert.*

class BoundsTest {

    @Test
    fun testContainsWithPointInsideBounds() {
        val bounds = Bounds(0.0, 0.0, 10.0, 10.0)
        assertTrue(bounds.contains(5.0, 5.0))
    }

    @Test
    fun testContainsWithPointOutsideBounds() {
        val bounds = Bounds(0.0, 0.0, 10.0, 10.0)
        assertFalse(bounds.contains(15.0, 15.0))
    }

    @Test
    fun testIntersectsWithOverlappingBounds() {
        val bounds1 = Bounds(0.0, 0.0, 10.0, 10.0)
        val bounds2 = Bounds(5.0, 5.0, 15.0, 15.0)
        assertTrue(bounds1.intersects(bounds2))
    }

    @Test
    fun testIntersectsWithNonOverlappingBounds() {
        val bounds1 = Bounds(0.0, 0.0, 10.0, 10.0)
        val bounds2 = Bounds(20.0, 20.0, 30.0, 30.0)
        assertFalse(bounds1.intersects(bounds2))
    }

    @Test
    fun testEmptyBounds() {
        val empty = Bounds.empty()
        assertTrue(empty.isEmpty())
        assertFalse(empty.intersects(Bounds(0.0, 0.0, 10.0, 10.0)))
    }

    @Test
    fun testExpandToIncludeWithPoint() {
        val bounds = Bounds(0.0, 0.0, 5.0, 5.0)
        val expanded = bounds.expandToInclude(10.0, 10.0)
        assertEquals(0.0, expanded.minX, 0.0001)
        assertEquals(0.0, expanded.minY, 0.0001)
        assertEquals(10.0, expanded.maxX, 0.0001)
        assertEquals(10.0, expanded.maxY, 0.0001)
    }

    @Test
    fun testExpandToIncludeWithBounds() {
        val bounds1 = Bounds(0.0, 0.0, 5.0, 5.0)
        val bounds2 = Bounds(3.0, 3.0, 10.0, 10.0)
        val expanded = bounds1.expandToInclude(bounds2)
        assertEquals(0.0, expanded.minX, 0.0001)
        assertEquals(0.0, expanded.minY, 0.0001)
        assertEquals(10.0, expanded.maxX, 0.0001)
        assertEquals(10.0, expanded.maxY, 0.0001)
    }

    @Test
    fun testWidthHeightAndArea() {
        val bounds = Bounds(0.0, 0.0, 10.0, 5.0)
        assertEquals(10.0, bounds.width, 0.0001)
        assertEquals(5.0, bounds.height, 0.0001)
        assertEquals(50.0, bounds.area, 0.0001)
    }

    @Test
    fun testCenter() {
        val bounds = Bounds(0.0, 0.0, 10.0, 10.0)
        val centerX = bounds.center.first
        val centerY = bounds.center.second
        assertEquals(5.0, centerX, 0.0001)
        assertEquals(5.0, centerY, 0.0001)
    }
}
