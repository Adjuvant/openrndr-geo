package geo.render.geometry

import org.junit.Test
import org.openrndr.math.Vector2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertEquals(ring, result[0])
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

        assertEquals(2, result.size)

        // First ring should end at +180 boundary
        val firstRing = result[0]
        assertEquals(3, firstRing.size)  // 170.0, 180.0 boundary, 170.0 (closed)
        assertEquals(180.0, firstRing[1].x, 0.0001)

        // Second ring should start at -180 boundary
        val secondRing = result[1]
        assertEquals(3, secondRing.size)  // -180.0 boundary, -170.0, -180.0 (closed)
        assertEquals(-180.0, secondRing[0].x, 0.0001)
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
            assertTrue(splitRing.size >= 3, "Ring should have at least 3 vertices")
            // Check if ring is closed (first equals last)
            assertEquals(splitRing.first().x, splitRing.last().x, 0.0001)
            assertEquals(splitRing.first().y, splitRing.last().y, 0.0001)
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

// TODO: Implement these functions in AntimeridianSplitter.kt (plan 16-01)
fun crossesAntimeridian(ring: List<Vector2>): Boolean {
    TODO("Implement in plan 16-01: Detect if ring crosses ±180° longitude")
}

fun interpolateAntimeridianCrossing(p1: Vector2, p2: Vector2): Double {
    TODO("Implement in plan 16-01: Interpolate latitude at antimeridian boundary")
}

fun splitAtAntimeridian(ring: List<Vector2>): List<List<Vector2>> {
    TODO("Implement in plan 16-01: Split ring at antimeridian crossings")
}
