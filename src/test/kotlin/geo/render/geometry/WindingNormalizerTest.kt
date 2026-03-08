package geo.render.geometry

import org.junit.Test
import org.openrndr.math.Vector2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD scaffold for winding order normalization utilities.
 *
 * These tests define expected behavior for enforcing consistent winding order
 * on polygon rings after projection to screen space. Tests will initially fail
 * (RED phase) until WindingNormalizer.kt is implemented in plan 16-01.
 *
 * Screen space convention (Y inverted from geographic):
 * - Exterior rings → clockwise winding (positive fill)
 * - Interior rings (holes) → counter-clockwise winding (negative fill = hole)
 *
 * @see WindingNormalizer
 */
class WindingNormalizerTest {

    // ============================================
    // calculateSignedArea tests
    // ============================================

    @Test
    fun `calculateSignedArea returns positive for counterClockwise square in screen space`() {
        // In screen space with Y down: CCW square has positive signed area
        val square = listOf(
            Vector2(0.0, 0.0),   // top-left
            Vector2(0.0, 10.0),  // bottom-left
            Vector2(10.0, 10.0), // bottom-right
            Vector2(10.0, 0.0)   // top-right
        )

        val area = calculateSignedArea(square)

        assertTrue(
            area > 0,
            "Counter-clockwise square in screen space should have positive signed area"
        )
        assertEquals(
            100.0,
            area,
            0.0001,
            "10x10 square should have area 100"
        )
    }

    @Test
    fun `calculateSignedArea returns negative for clockwise square in screen space`() {
        // In screen space with Y down: CW square has negative signed area
        val square = listOf(
            Vector2(0.0, 0.0),   // top-left
            Vector2(10.0, 0.0),  // top-right
            Vector2(10.0, 10.0), // bottom-right
            Vector2(0.0, 10.0)   // bottom-left
        )

        val area = calculateSignedArea(square)

        assertTrue(
            area < 0,
            "Clockwise square in screen space should have negative signed area"
        )
        assertEquals(
            -100.0,
            area,
            0.0001,
            "10x10 clockwise square should have area -100"
        )
    }

    @Test
    fun `calculateSignedArea returns zero for degenerate ring`() {
        val line = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0)
        )
        val area = calculateSignedArea(line)

        assertEquals(
            0.0,
            area,
            0.0001,
            "Line has zero area"
        )
    }

    @Test
    fun `calculateSignedArea handles triangle`() {
        val triangle = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(5.0, 10.0)
        )

        val area = calculateSignedArea(triangle)

        // Area of triangle = 0.5 * base * height = 0.5 * 10 * 10 = 50
        assertEquals(
            -50.0,
            area,
            0.0001,
            "Clockwise triangle should have area -50"
        )
    }

    // ============================================
    // needsWindingReversal tests
    // ============================================

    @Test
    fun `needsWindingReversal returns false when already correct for exterior`() {
        val clockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )

        // Exterior should be clockwise
        assertFalse(
            needsWindingReversal(clockwiseRing, desiredClockwise = true),
            "Already clockwise ring should not need reversal for exterior"
        )
    }

    @Test
    fun `needsWindingReversal returns true when reversal needed for exterior`() {
        val counterClockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )

        // Exterior should be clockwise but this is CCW
        assertTrue(
            needsWindingReversal(counterClockwiseRing, desiredClockwise = true),
            "Counter-clockwise ring needs reversal to become clockwise exterior"
        )
    }

    @Test
    fun `needsWindingReversal returns false when already correct for interior`() {
        val counterClockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )

        // Interior (hole) should be counter-clockwise
        assertFalse(
            needsWindingReversal(counterClockwiseRing, desiredClockwise = false),
            "Already counter-clockwise ring should not need reversal for interior"
        )
    }

    @Test
    fun `needsWindingReversal returns true when reversal needed for interior`() {
        val clockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )

        // Interior should be counter-clockwise but this is CW
        assertTrue(
            needsWindingReversal(clockwiseRing, desiredClockwise = false),
            "Clockwise ring needs reversal to become counter-clockwise interior"
        )
    }

    // ============================================
    // enforceWinding tests
    // ============================================

    @Test
    fun `enforceWinding returns same list when already correct`() {
        val clockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )

        val result = enforceWinding(clockwiseRing, desiredClockwise = true)

        assertEquals(
            clockwiseRing,
            result,
            "Already clockwise ring should be returned unchanged"
        )
    }

    @Test
    fun `enforceWinding reverses list when wrong winding`() {
        val counterClockwiseRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0)
        )

        val result = enforceWinding(counterClockwiseRing, desiredClockwise = true)

        // Should be reversed to clockwise
        assertEquals(
            Vector2(10.0, 0.0),
            result[0],
            "First point should now be (10,0)"
        )
        assertEquals(
            Vector2(0.0, 0.0),
            result[3],
            "Last point should now be (0,0)"
        )
    }

    @Test
    fun `enforceWinding preserves ring closure when reversing`() {
        val closedRing = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 10.0),
            Vector2(10.0, 10.0),
            Vector2(10.0, 0.0),
            Vector2(0.0, 0.0)  // Closed
        )

        val result = enforceWinding(closedRing, desiredClockwise = true)

        // Reversed ring should also be closed
        assertEquals(
            result.first(),
            result.last(),
            "Reversed ring should remain closed"
        )
    }

    // ============================================
    // normalizePolygonWinding tests
    // ============================================

    @Test
    fun `normalizePolygonWinding makes exterior clockwise and holes counterClockwise`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(0.0, 100.0),
            Vector2(100.0, 100.0),
            Vector2(100.0, 0.0)
        )  // Counter-clockwise (wrong)

        val hole = listOf(
            Vector2(25.0, 25.0),
            Vector2(75.0, 25.0),
            Vector2(75.0, 75.0),
            Vector2(25.0, 75.0)
        )  // Clockwise (wrong)

        val (normalizedExterior, normalizedHoles) = normalizePolygonWinding(exterior, listOf(hole))

        // Exterior should now be clockwise (negative area in screen space)
        val exteriorArea = calculateSignedArea(normalizedExterior)
        assertTrue(
            exteriorArea < 0,
            "Normalized exterior should be clockwise (negative signed area in screen space)"
        )

        // Hole should now be counter-clockwise (positive area in screen space)
        val holeArea = calculateSignedArea(normalizedHoles[0])
        assertTrue(
            holeArea > 0,
            "Normalized hole should be counter-clockwise (positive signed area in screen space)"
        )
    }

    @Test
    fun `normalizePolygonWinding handles correct winding without change`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )  // Already clockwise

        val hole = listOf(
            Vector2(25.0, 25.0),
            Vector2(25.0, 75.0),
            Vector2(75.0, 75.0),
            Vector2(75.0, 25.0)
        )  // Already counter-clockwise

        val (normalizedExterior, normalizedHoles) = normalizePolygonWinding(exterior, listOf(hole))

        // Should remain unchanged
        assertEquals(exterior, normalizedExterior, "Already correct exterior should be unchanged")
        assertEquals(hole, normalizedHoles[0], "Already correct hole should be unchanged")
    }

    @Test
    fun `normalizePolygonWinding handles multiple holes`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        val holes = listOf(
            listOf(
                Vector2(10.0, 10.0),
                Vector2(30.0, 10.0),
                Vector2(30.0, 30.0),
                Vector2(10.0, 30.0)
            ),
            listOf(
                Vector2(60.0, 60.0),
                Vector2(90.0, 60.0),
                Vector2(90.0, 90.0),
                Vector2(60.0, 90.0)
            )
        )

        val (_, normalizedHoles) = normalizePolygonWinding(exterior, holes)

        assertEquals(2, normalizedHoles.size, "Should preserve all holes")
        normalizedHoles.forEachIndexed { index, hole ->
            val area = calculateSignedArea(hole)
            assertTrue(
                area > 0,
                "Hole $index should be counter-clockwise (positive area)"
            )
        }
    }

    @Test
    fun `normalizePolygonWinding handles empty holes list`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )

        val (normalizedExterior, normalizedHoles) = normalizePolygonWiring(exterior, emptyList())

        assertEquals(exterior, normalizedExterior, "Exterior should be normalized")
        assertTrue(normalizedHoles.isEmpty(), "Empty holes list should remain empty")
    }

    // ============================================
    // Degenerate case tests
    // ============================================

    @Test
    fun `enforceWinding handles empty list`() {
        val empty = emptyList<Vector2>()
        val result = enforceWinding(empty, desiredClockwise = true)
        assertTrue(result.isEmpty(), "Empty list should return empty")
    }

    @Test
    fun `enforceWinding handles single point`() {
        val single = listOf(Vector2(5.0, 5.0))
        val result = enforceWinding(single, desiredClockwise = true)
        assertEquals(single, result, "Single point should return unchanged")
    }

    @Test
    fun `enforceWinding handles two points`() {
        val line = listOf(Vector2(0.0, 0.0), Vector2(10.0, 0.0))
        val result = enforceWinding(line, desiredClockwise = true)
        assertEquals(line, result, "Two points should return unchanged")
    }

    @Test
    fun `calculateSignedArea handles empty list`() {
        assertEquals(0.0, calculateSignedArea(emptyList()), 0.0001)
    }

    @Test
    fun `calculateSignedArea handles single point`() {
        assertEquals(0.0, calculateSignedArea(listOf(Vector2(5.0, 5.0))), 0.0001)
    }
}

// TODO: Implement these functions in WindingNormalizer.kt (plan 16-01)
fun calculateSignedArea(ring: List<Vector2>): Double {
    TODO("Implement in plan 16-01: Calculate signed area using shoelace formula")
}

fun needsWindingReversal(ring: List<Vector2>, desiredClockwise: Boolean): Boolean {
    TODO("Implement in plan 16-01: Determine if ring needs winding reversal")
}

fun enforceWinding(ring: List<Vector2>, desiredClockwise: Boolean): List<Vector2> {
    TODO("Implement in plan 16-01: Enforce specific winding direction")
}

fun normalizePolygonWinding(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>
): Pair<List<Vector2>, List<List<Vector2>>> {
    TODO("Implement in plan 16-01: Normalize polygon winding (exterior CW, interiors CCW)")
}

// Typo in test - needs correct function name
fun normalizePolygonWiring(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>
): Pair<List<Vector2>, List<List<Vector2>>> {
    return normalizePolygonWinding(exterior, interiors)
}
