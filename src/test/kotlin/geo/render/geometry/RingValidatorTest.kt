package geo.render.geometry

import org.junit.Test
import org.openrndr.math.Vector2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD scaffold for ring validation utilities.
 *
 * These tests define expected behavior for validating polygon rings including
 * degenerate ring detection, bounds calculation, and hole-inside-exterior checks.
 * Tests will initially fail (RED phase) until RingValidator.kt is implemented
 * in plan 16-01 or 16-02.
 *
 * Per 16-CONTEXT.md: Validate-but-don't-repair — check problems, log warnings,
 * render original data.
 *
 * @see RingValidator
 */
class RingValidatorTest {

    // ============================================
    // isDegenerateRing - vertex count tests
    // ============================================

    @Test
    fun `isDegenerateRing returns true for empty ring`() {
        val ring = emptyList<Vector2>()
        assertTrue(
            isDegenerateRing(ring),
            "Empty ring is degenerate"
        )
    }

    @Test
    fun `isDegenerateRing returns true for single point`() {
        val ring = listOf(Vector2(0.0, 0.0))
        assertTrue(
            isDegenerateRing(ring),
            "Single point ring is degenerate"
        )
    }

    @Test
    fun `isDegenerateRing returns true for two points`() {
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0)
        )
        assertTrue(
            isDegenerateRing(ring),
            "Two point ring is degenerate (line, not area)"
        )
    }

    @Test
    fun `isDegenerateRing returns false for triangle`() {
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(5.0, 10.0)
        )
        assertFalse(
            isDegenerateRing(ring),
            "Triangle is valid (3 points minimum)"
        )
    }

    @Test
    fun `isDegenerateRing returns false for closed quad`() {
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0),
            Vector2(0.0, 0.0)  // Closed
        )
        assertFalse(
            isDegenerateRing(ring),
            "Closed quadrilateral is valid"
        )
    }

    // ============================================
    // isDegenerateRing - area tests
    // ============================================

    @Test
    fun `isDegenerateRing returns true for near-zero area`() {
        // Very thin triangle with area < 1e-10
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(1e-6, 0.0),
            Vector2(0.5e-6, 1e-6)
        )
        assertTrue(
            isDegenerateRing(ring),
            "Ring with near-zero area is degenerate"
        )
    }

    @Test
    fun `isDegenerateRing returns false for normal area`() {
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0)
        )
        assertFalse(
            isDegenerateRing(ring),
            "Triangle with normal area is valid"
        )
    }

    @Test
    fun `isDegenerateRing detects collinear points`() {
        // All points on same line = zero area
        val ring = listOf(
            Vector2(0.0, 0.0),
            Vector2(5.0, 0.0),
            Vector2(10.0, 0.0)
        )
        assertTrue(
            isDegenerateRing(ring),
            "Collinear points have zero area"
        )
    }

    // ============================================
    // calculateBounds tests
    // ============================================

    @Test
    fun `calculateBounds returns correct bbox for square`() {
        val ring = listOf(
            Vector2(10.0, 20.0),
            Vector2(30.0, 20.0),
            Vector2(30.0, 40.0),
            Vector2(10.0, 40.0)
        )

        val bounds = calculateBounds(ring)

        assertEquals(10.0, bounds.minX, 0.0001, "minX should be 10.0")
        assertEquals(30.0, bounds.maxX, 0.0001, "maxX should be 30.0")
        assertEquals(20.0, bounds.minY, 0.0001, "minY should be 20.0")
        assertEquals(40.0, bounds.maxY, 0.0001, "maxY should be 40.0")
    }

    @Test
    fun `calculateBounds handles single point`() {
        val ring = listOf(Vector2(5.0, 10.0))

        val bounds = calculateBounds(ring)

        assertEquals(5.0, bounds.minX, 0.0001)
        assertEquals(5.0, bounds.maxX, 0.0001)
        assertEquals(10.0, bounds.minY, 0.0001)
        assertEquals(10.0, bounds.maxY, 0.0001)
    }

    @Test
    fun `calculateBounds handles empty ring`() {
        val bounds = calculateBounds(emptyList())

        assertEquals(Double.POSITIVE_INFINITY, bounds.minX, 0.0001)
        assertEquals(Double.NEGATIVE_INFINITY, bounds.maxX, 0.0001)
        assertEquals(Double.POSITIVE_INFINITY, bounds.minY, 0.0001)
        assertEquals(Double.NEGATIVE_INFINITY, bounds.maxY, 0.0001)
    }

    @Test
    fun `calculateBounds handles negative coordinates`() {
        val ring = listOf(
            Vector2(-50.0, -30.0),
            Vector2(-20.0, -30.0),
            Vector2(-20.0, -10.0),
            Vector2(-50.0, -10.0)
        )

        val bounds = calculateBounds(ring)

        assertEquals(-50.0, bounds.minX, 0.0001)
        assertEquals(-20.0, bounds.maxX, 0.0001)
        assertEquals(-30.0, bounds.minY, 0.0001)
        assertEquals(-10.0, bounds.maxY, 0.0001)
    }

    // ============================================
    // isPointInBounds tests
    // ============================================

    @Test
    fun `isPointInBounds returns true for point inside`() {
        val bounds = BoundingBox(0.0, 0.0, 100.0, 100.0)
        val point = Vector2(50.0, 50.0)

        assertTrue(
            isPointInBounds(point, bounds),
            "Point (50,50) should be inside (0,0)-(100,100)"
        )
    }

    @Test
    fun `isPointInBounds returns true for point on boundary`() {
        val bounds = BoundingBox(0.0, 0.0, 100.0, 100.0)

        assertTrue(
            isPointInBounds(Vector2(0.0, 50.0), bounds),
            "Point on left boundary should be inside"
        )
        assertTrue(
            isPointInBounds(Vector2(100.0, 50.0), bounds),
            "Point on right boundary should be inside"
        )
        assertTrue(
            isPointInBounds(Vector2(50.0, 0.0), bounds),
            "Point on bottom boundary should be inside"
        )
        assertTrue(
            isPointInBounds(Vector2(50.0, 100.0), bounds),
            "Point on top boundary should be inside"
        )
    }

    @Test
    fun `isPointInBounds returns false for point outside`() {
        val bounds = BoundingBox(0.0, 0.0, 100.0, 100.0)

        assertFalse(
            isPointInBounds(Vector2(-1.0, 50.0), bounds),
            "Point left of bounds should be outside"
        )
        assertFalse(
            isPointInBounds(Vector2(101.0, 50.0), bounds),
            "Point right of bounds should be outside"
        )
        assertFalse(
            isPointInBounds(Vector2(50.0, -1.0), bounds),
            "Point below bounds should be outside"
        )
        assertFalse(
            isPointInBounds(Vector2(50.0, 101.0), bounds),
            "Point above bounds should be outside"
        )
    }

    // ============================================
    // validateInteriorRings tests
    // ============================================

    @Test
    fun `validateInteriorRings drops degenerate rings`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        val holes = listOf(
            // Valid hole
            listOf(
                Vector2(25.0, 25.0),
                Vector2(25.0, 75.0),
                Vector2(75.0, 75.0),
                Vector2(75.0, 25.0)
            ),
            // Degenerate hole (only 2 points)
            listOf(
                Vector2(10.0, 10.0),
                Vector2(20.0, 10.0)
            )
        )

        val validHoles = validateInteriorRings(exterior, holes, featureId = "test-1")

        assertEquals(1, validHoles.size, "Should drop degenerate hole")
        assertEquals(4, validHoles[0].size, "Should keep valid hole")
    }

    @Test
    fun `validateInteriorRings keeps holes inside bounds`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        val hole = listOf(
            Vector2(25.0, 25.0),
            Vector2(75.0, 25.0),
            Vector2(75.0, 75.0),
            Vector2(25.0, 75.0)
        )

        val validHoles = validateInteriorRings(exterior, listOf(hole), featureId = "test-2")

        assertEquals(1, validHoles.size, "Should keep hole inside bounds")
    }

    @Test
    fun `validateInteriorRings warns but keeps hole outside bounds`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        // Hole completely outside exterior bounds
        val hole = listOf(
            Vector2(200.0, 200.0),
            Vector2(250.0, 200.0),
            Vector2(250.0, 250.0),
            Vector2(200.0, 250.0)
        )

        val validHoles = validateInteriorRings(exterior, listOf(hole), featureId = "test-3")

        // Per validate-but-don't-repair: keep the hole but log warning
        assertEquals(1, validHoles.size, "Should keep hole even outside bounds (validate, don't repair)")
    }

    @Test
    fun `validateInteriorRings handles empty holes list`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0)
        )

        val validHoles = validateInteriorRings(exterior, emptyList(), featureId = "test-4")

        assertTrue(validHoles.isEmpty(), "Empty holes list should return empty")
    }

    @Test
    fun `validateInteriorRings handles all degenerate holes`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        val holes = listOf(
            listOf(Vector2(10.0, 10.0)),  // Single point
            listOf(Vector2(20.0, 20.0), Vector2(30.0, 20.0))  // Line
        )

        val validHoles = validateInteriorRings(exterior, holes, featureId = "test-5")

        assertTrue(validHoles.isEmpty(), "All degenerate holes should be dropped")
    }

    @Test
    fun `validateInteriorRings handles mixed valid and invalid`() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )

        val holes = listOf(
            // Valid hole inside
            listOf(
                Vector2(20.0, 20.0),
                Vector2(40.0, 20.0),
                Vector2(40.0, 40.0),
                Vector2(20.0, 40.0)
            ),
            // Degenerate (too few points)
            listOf(Vector2(10.0, 10.0)),
            // Valid hole but outside bounds
            listOf(
                Vector2(150.0, 150.0),
                Vector2(160.0, 150.0),
                Vector2(160.0, 160.0),
                Vector2(150.0, 160.0)
            ),
            // Degenerate (zero area)
            listOf(
                Vector2(50.0, 50.0),
                Vector2(60.0, 50.0),
                Vector2(55.0, 50.0)
            )
        )

        val validHoles = validateInteriorRings(exterior, holes, featureId = "test-6")

        assertEquals(2, validHoles.size, "Should keep 2 valid holes, drop 2 degenerate")
    }

    // ============================================
    // BoundingBox data class tests
    // ============================================

    @Test
    fun `BoundingBox contains point correctly`() {
        val bounds = BoundingBox(0.0, 0.0, 100.0, 100.0)

        assertTrue(bounds.contains(50.0, 50.0), "Center point should be contained")
        assertTrue(bounds.contains(0.0, 0.0), "Corner should be contained")
        assertTrue(bounds.contains(100.0, 100.0), "Opposite corner should be contained")
        assertFalse(bounds.contains(-1.0, 50.0), "Outside should not be contained")
        assertFalse(bounds.contains(101.0, 50.0), "Outside should not be contained")
    }

    @Test
    fun `BoundingBox width and height calculated correctly`() {
        val bounds = BoundingBox(10.0, 20.0, 50.0, 80.0)

        assertEquals(40.0, bounds.width, 0.0001, "Width should be 40")
        assertEquals(60.0, bounds.height, 0.0001, "Height should be 60")
    }
}

/**
 * Simple bounding box data class for ring validation.
 * TODO: Move to RingValidator.kt or shared geometry types.
 */
data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    val width: Double get() = maxX - minX
    val height: Double get() = maxY - minY

    fun contains(x: Double, y: Double): Boolean {
        return x >= minX && x <= maxX && y >= minY && y <= maxY
    }
}

// TODO: Implement these functions in RingValidator.kt (plan 16-01 or 16-02)
fun isDegenerateRing(ring: List<Vector2>): Boolean {
    TODO("Implement in plan 16-01: Check if ring is degenerate (vertex count or area)")
}

fun calculateBounds(ring: List<Vector2>): BoundingBox {
    TODO("Implement in plan 16-01: Calculate bounding box of ring")
}

fun isPointInBounds(point: Vector2, bounds: BoundingBox): Boolean {
    TODO("Implement in plan 16-01: Check if point is inside bounds")
}

fun validateInteriorRings(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    featureId: String? = null
): List<List<Vector2>> {
    TODO("Implement in plan 16-01: Validate interior rings (drop degenerate, warn if outside bounds)")
}
