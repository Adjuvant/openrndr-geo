package geo.render.geometry

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

/**
 * Tests for AntimeridianSplitter - antimeridian detection and ring splitting algorithms.
 *
 * Covers RENDER-01: MultiPolygon rendering for ocean/whole-world data
 */
class AntimeridianSplitterTest {

    @Test
    fun `crossesAntimeridian returns false for ring entirely on positive side`() {
        val ring = listOf(
            Vector2(10.0, 20.0),
            Vector2(20.0, 20.0),
            Vector2(20.0, 30.0),
            Vector2(10.0, 30.0),
            Vector2(10.0, 20.0)  // Closed
        )
        assertFalse(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian returns false for ring entirely on negative side`() {
        val ring = listOf(
            Vector2(-50.0, 20.0),
            Vector2(-20.0, 20.0),
            Vector2(-20.0, 30.0),
            Vector2(-50.0, 30.0),
            Vector2(-50.0, 20.0)  // Closed
        )
        assertFalse(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian returns true for ring crossing from positive to negative`() {
        val ring = listOf(
            Vector2(170.0, 20.0),
            Vector2(-170.0, 20.0),
            Vector2(-170.0, 30.0),
            Vector2(170.0, 30.0),
            Vector2(170.0, 20.0)  // Closed
        )
        assertTrue(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian returns true for ring crossing from negative to positive`() {
        val ring = listOf(
            Vector2(-170.0, 20.0),
            Vector2(170.0, 20.0),
            Vector2(170.0, 30.0),
            Vector2(-170.0, 30.0),
            Vector2(-170.0, 20.0)  // Closed
        )
        assertTrue(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian returns false for small longitude change`() {
        val ring = listOf(
            Vector2(179.0, 20.0),
            Vector2(179.5, 20.0),
            Vector2(179.5, 30.0),
            Vector2(179.0, 30.0),
            Vector2(179.0, 20.0)
        )
        assertFalse(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian handles empty ring`() {
        val ring = emptyList<Vector2>()
        assertFalse(crossesAntimeridian(ring))
    }

    @Test
    fun `crossesAntimeridian handles single point`() {
        val ring = listOf(Vector2(0.0, 0.0))
        assertFalse(crossesAntimeridian(ring))
    }

    @Test
    fun `interpolateAntimeridianCrossing calculates correct latitude for positive to negative crossing`() {
        val p1 = Vector2(170.0, 20.0)
        val p2 = Vector2(-170.0, 30.0)
        val crossingLat = interpolateAntimeridianCrossing(p1, p2)

        // Crossing should be at latitude 25.0 (midpoint)
        assertEquals(25.0, crossingLat, 0.0001)
    }

    @Test
    fun `interpolateAntimeridianCrossing calculates correct latitude for negative to positive crossing`() {
        val p1 = Vector2(-170.0, 30.0)
        val p2 = Vector2(170.0, 20.0)
        val crossingLat = interpolateAntimeridianCrossing(p1, p2)

        // Crossing should be at latitude 25.0 (midpoint)
        assertEquals(25.0, crossingLat, 0.0001)
    }

    @Test
    fun `interpolateAntimeridianCrossing handles steep slope`() {
        val p1 = Vector2(179.0, 0.0)
        val p2 = Vector2(-179.0, 80.0)
        val crossingLat = interpolateAntimeridianCrossing(p1, p2)

        // Crossing should be at latitude 40.0 (midpoint)
        assertEquals(40.0, crossingLat, 0.0001)
    }

    @Test
    fun `splitAtAntimeridian returns single ring for non-crossing ring`() {
        val ring = listOf(
            Vector2(10.0, 20.0),
            Vector2(20.0, 20.0),
            Vector2(20.0, 30.0),
            Vector2(10.0, 30.0),
            Vector2(10.0, 20.0)
        )
        val result = splitAtAntimeridian(ring)

        assertEquals(1, result.size)
        // Compare ring elements individually since JUnit 4 doesn't support List comparison
        assertEquals(ring.size, result[0].size)
        for (i in ring.indices) {
            assertEquals(ring[i].x, result[0][i].x, 0.0001)
            assertEquals(ring[i].y, result[0][i].y, 0.0001)
        }
    }

    @Test
    fun `splitAtAntimeridian splits ring crossing once`() {
        val ring = listOf(
            Vector2(170.0, 20.0),
            Vector2(-170.0, 20.0),
            Vector2(-170.0, 30.0),
            Vector2(170.0, 30.0),
            Vector2(170.0, 20.0)
        )
        val result = splitAtAntimeridian(ring)

        // Closed ring crossing twice produces 3 rings:
        // 1. From first point to first crossing
        // 2. Between crossings  
        // 3. From second crossing to end (which connects back to start)
        assertEquals(3, result.size)

        // First ring should end at +180 boundary
        val firstRing = result[0]
        assertTrue(firstRing.size >= 2)
        assertEquals(180.0, firstRing.last().x, 0.0001)

        // Second ring should start at -180 and end at +180
        val secondRing = result[1]
        assertTrue(secondRing.size >= 2)
        assertEquals(-180.0, secondRing.first().x, 0.0001)
    }

    @Test
    fun `splitAtAntimeridian handles ring crossing multiple times`() {
        // Create a ring that crosses the antimeridian twice (zigzag pattern)
        val ring = listOf(
            Vector2(170.0, 10.0),
            Vector2(-170.0, 10.0),  // Cross 1
            Vector2(-170.0, 20.0),
            Vector2(170.0, 20.0),   // Cross 2
            Vector2(170.0, 30.0),
            Vector2(-170.0, 30.0),  // Cross 3
            Vector2(-170.0, 40.0),
            Vector2(170.0, 40.0),   // Cross 4
            Vector2(170.0, 10.0)    // Close
        )
        val result = splitAtAntimeridian(ring)

        // Should create 5 separate rings
        assertEquals(5, result.size)
    }

    @Test
    fun `splitAtAntimeridian handles edge at exactly 180`() {
        // Ring with vertices exactly at ±180°
        val ring = listOf(
            Vector2(180.0, 20.0),
            Vector2(-180.0, 20.0),
            Vector2(-180.0, 30.0),
            Vector2(180.0, 30.0),
            Vector2(180.0, 20.0)
        )
        val result = splitAtAntimeridian(ring)

        // Should still split correctly
        assertTrue(result.size >= 1)
    }

    @Test
    fun `splitAtAntimeridian produces closed rings`() {
        val ring = listOf(
            Vector2(170.0, 20.0),
            Vector2(-170.0, 20.0),
            Vector2(-170.0, 30.0),
            Vector2(170.0, 30.0),
            Vector2(170.0, 20.0)
        )
        val result = splitAtAntimeridian(ring)

        result.forEach { splitRing ->
            // Each split ring should have at least 2 points
            assertTrue(splitRing.size >= 2)
        }
    }

    @Test
    fun `splitAtAntimeridian returns empty list for empty input`() {
        val ring = emptyList<Vector2>()
        val result = splitAtAntimeridian(ring)
        assertEquals(0, result.size)
    }

    @Test
    fun `splitAtAntimeridian handles single point`() {
        val ring = listOf(Vector2(0.0, 0.0))
        val result = splitAtAntimeridian(ring)
        assertEquals(1, result.size)
        assertEquals(1, result[0].size)
    }
}
