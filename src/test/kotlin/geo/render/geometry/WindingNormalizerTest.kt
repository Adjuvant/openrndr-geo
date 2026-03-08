package geo.render.geometry

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class WindingNormalizerTest {

    @Test
    fun testCalculateSignedAreaForClockwiseSquare() {
        // In screen space (Y down): clockwise = negative area
        val square = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        val area = calculateSignedArea(square)
        assertTrue(area < 0)
        assertEquals(-100.0, area, 0.0001)
    }

    @Test
    fun testCalculateSignedAreaForCounterClockwiseSquare() {
        // In screen space (Y down): counter-clockwise = positive area
        val square = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )
        val area = calculateSignedArea(square)
        assertTrue(area > 0)
        assertEquals(100.0, area, 0.0001)
    }

    @Test
    fun testCalculateSignedAreaForDegenerateRing() {
        val line = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0)
        )
        assertEquals(0.0, calculateSignedArea(line), 0.0001)
    }

    @Test
    fun testNeedsWindingReversalReturnsFalseWhenAlreadyCorrect() {
        val clockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        assertFalse(needsWindingReversal(clockwiseRing, desiredClockwise = true))
    }

    @Test
    fun testNeedsWindingReversalReturnsTrueWhenReversalNeeded() {
        val counterClockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )
        assertTrue(needsWindingReversal(counterClockwiseRing, desiredClockwise = true))
    }

    @Test
    fun testEnforceWindingReturnsSameListWhenAlreadyCorrect() {
        val clockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        val result = enforceWinding(clockwiseRing, desiredClockwise = true)
        assertEquals(clockwiseRing.size, result.size)
        for (i in clockwiseRing.indices) {
            assertEquals(clockwiseRing[i].x, result[i].x, 0.0001)
            assertEquals(clockwiseRing[i].y, result[i].y, 0.0001)
        }
    }

    @Test
    fun testEnforceWindingReversesListWhenWrongWinding() {
        val ccwRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )
        val result = enforceWinding(ccwRing, desiredClockwise = true)
        // Result should be reversed
        assertEquals(ccwRing.size, result.size)
        assertEquals(ccwRing[0].x, result[3].x, 0.0001)
        assertEquals(ccwRing[3].x, result[0].x, 0.0001)
    }

    @Test
    fun testNormalizePolygonWindingMakesExteriorClockwise() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 100.0),
            Vector2(100.0, 100.0),
            Vector2(100.0, 0.0)
        ) // CCW in screen space
        val hole = listOf(
            Vector2(10.0, 10.0),
            Vector2(40.0, 10.0),
            Vector2(40.0, 40.0)
        ) // CW in screen space

        val (normalizedExterior, normalizedHoles) = normalizePolygonWinding(exterior, listOf(hole))

        // Exterior should now be CW (negative area)
        assertTrue(calculateSignedArea(normalizedExterior) < 0)
        // Hole should now be CCW (positive area)
        assertTrue(calculateSignedArea(normalizedHoles[0]) > 0)
    }

    @Test
    fun testNormalizePolygonWindingHandlesEmptyHoles() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        val (normalizedExterior, normalizedHoles) = normalizePolygonWinding(exterior, emptyList())
        assertEquals(exterior.size, normalizedExterior.size)
        assertEquals(0, normalizedHoles.size)
    }

    @Test
    fun testEnforceWindingHandlesEmptyList() {
        val result = enforceWinding(emptyList(), desiredClockwise = true)
        assertEquals(0, result.size)
    }

    @Test
    fun testEnforceWindingHandlesSinglePoint() {
        val single = listOf(Vector2(5.0, 5.0))
        val result = enforceWinding(single, desiredClockwise = true)
        assertEquals(1, result.size)
    }
}
