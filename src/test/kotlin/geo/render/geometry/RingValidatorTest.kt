package geo.render.geometry

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class RingValidatorTest {

    @Test
    fun testIsDegenerateRingReturnsTrueForLessThan3Points() {
        val line = listOf(Vector2(0.0, 0.0), Vector2(10.0, 0.0))
        assertTrue(isDegenerateRing(line))
    }

    @Test
    fun testIsDegenerateRingReturnsTrueForZeroArea() {
        val collinear = listOf(
            Vector2(0.0, 0.0),
            Vector2(5.0, 0.0),
            Vector2(10.0, 0.0)
        )
        assertTrue(isDegenerateRing(collinear))
    }

    @Test
    fun testIsDegenerateRingReturnsFalseForValidRing() {
        val square = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        assertFalse(isDegenerateRing(square))
    }

    @Test
    fun testCalculateBoundsForPoints() {
        val points = listOf(
            Vector2(10.0, 20.0),
            Vector2(30.0, 40.0),
            Vector2(5.0, 50.0)
        )
        val bounds = calculateBounds(points)
        assertEquals(5.0, bounds.minX, 0.0001)
        assertEquals(30.0, bounds.maxX, 0.0001)
        assertEquals(20.0, bounds.minY, 0.0001)
        assertEquals(50.0, bounds.maxY, 0.0001)
    }

    @Test
    fun testIsPointInBounds() {
        val points = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 10.0)
        )
        val bounds = calculateBounds(points)
        
        assertTrue(isPointInBounds(Vector2(5.0, 5.0), bounds))
        assertTrue(isPointInBounds(Vector2(0.0, 0.0), bounds))
        assertTrue(isPointInBounds(Vector2(10.0, 10.0), bounds))
        assertFalse(isPointInBounds(Vector2(15.0, 5.0), bounds))
        assertFalse(isPointInBounds(Vector2(5.0, 15.0), bounds))
    }

    @Test
    fun testValidateInteriorRingsDropsDegenerateRings() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )
        val validHole = listOf(
            Vector2(10.0, 10.0),
            Vector2(20.0, 10.0),
            Vector2(20.0, 20.0),
            Vector2(10.0, 20.0)
        )
        val degenerateHole = listOf(Vector2(50.0, 50.0), Vector2(60.0, 50.0))
        
        val result = validateInteriorRings(exterior, listOf(validHole, degenerateHole))
        
        assertEquals(1, result.size)
        assertEquals(validHole.size, result[0].size)
    }

    @Test
    fun testValidateInteriorRingsReturnsEmptyForNoHoles() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        val result = validateInteriorRings(exterior, emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun testValidateInteriorRingsKeepsValidRings() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )
        val hole1 = listOf(
            Vector2(10.0, 10.0),
            Vector2(30.0, 10.0),
            Vector2(30.0, 30.0),
            Vector2(10.0, 30.0)
        )
        val hole2 = listOf(
            Vector2(50.0, 50.0),
            Vector2(60.0, 50.0),
            Vector2(60.0, 60.0),
            Vector2(50.0, 60.0)
        )
        
        val result = validateInteriorRings(exterior, listOf(hole1, hole2))
        
        assertEquals(2, result.size)
    }
}
