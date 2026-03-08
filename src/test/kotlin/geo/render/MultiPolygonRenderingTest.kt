package geo.render

import org.junit.Test
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.Winding
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD scaffold for MultiPolygon rendering with combined Shape approach.
 *
 * These tests define expected behavior for rendering MultiPolygons as a single Shape
 * with all contours combined, using non-zero winding rule for proper hole handling.
 * Tests will initially fail (RED phase) until MultiPolygon rendering is updated
 * in plans 16-01 and 16-02.
 *
 * Per 16-CONTEXT.md:
 * - Exterior rings → clockwise winding (positive fill)
 * - Interior rings (holes) → counter-clockwise winding (negative fill)
 * - Combined Shape eliminates overdraw and seams at shared boundaries
 *
 * @see MultiRenderer
 */
class MultiPolygonRenderingTest {

    // ============================================
    // Simple MultiPolygon (no holes) tests
    // ============================================

    @Test
    fun `simple multipolygon renders as single shape with multiple contours`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            )),
            geo.Polygon(listOf(
                Vector2(60.0, 0.0),
                Vector2(110.0, 0.0),
                Vector2(110.0, 50.0),
                Vector2(60.0, 50.0)
            ))
        ))

        // TODO: Implement createMultiPolygonShape helper
        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertNotNull(shape, "Should create a Shape from MultiPolygon")
        assertEquals(
            2,
            shape.contours.size,
            "Shape should have 2 contours (one per polygon)"
        )
    }

    @Test
    fun `simple multipolygon contours have clockwise winding`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            )),
            geo.Polygon(listOf(
                Vector2(60.0, 0.0),
                Vector2(110.0, 0.0),
                Vector2(110.0, 50.0),
                Vector2(60.0, 50.0)
            ))
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        // All contours should be clockwise (exterior winding)
        shape.contours.forEachIndexed { index, contour ->
            assertEquals(
                Winding.CLOCKWISE,
                contour.winding,
                "Contour $index should be clockwise"
            )
        }
    }

    // ============================================
    // MultiPolygon with holes tests
    // ============================================

    @Test
    fun `multipolygon with holes has correct number of contours`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(100.0, 0.0),
                    Vector2(100.0, 100.0),
                    Vector2(0.0, 100.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(25.0, 25.0),
                        Vector2(25.0, 75.0),
                        Vector2(75.0, 75.0),
                        Vector2(75.0, 25.0)
                    )
                )
            )
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertEquals(
            2,
            shape.contours.size,
            "Shape should have 2 contours (1 exterior + 1 hole)"
        )
    }

    @Test
    fun `multipolygon exterior has clockwise winding`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(100.0, 0.0),
                    Vector2(100.0, 100.0),
                    Vector2(0.0, 100.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(25.0, 25.0),
                        Vector2(75.0, 25.0),
                        Vector2(75.0, 75.0),
                        Vector2(25.0, 75.0)
                    )
                )
            )
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        val exteriorContour = shape.contours[0]
        assertEquals(
            Winding.CLOCKWISE,
            exteriorContour.winding,
            "Exterior contour should be clockwise"
        )
    }

    @Test
    fun `multipolygon holes have counterClockwise winding`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(100.0, 0.0),
                    Vector2(100.0, 100.0),
                    Vector2(0.0, 100.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(25.0, 25.0),
                        Vector2(75.0, 25.0),
                        Vector2(75.0, 75.0),
                        Vector2(25.0, 75.0)
                    )
                )
            )
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        val holeContour = shape.contours[1]
        assertEquals(
            Winding.COUNTER_CLOCKWISE,
            holeContour.winding,
            "Hole contour should be counter-clockwise"
        )
    }

    @Test
    fun `multipolygon with multiple holes maintains correct winding`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(100.0, 0.0),
                    Vector2(100.0, 100.0),
                    Vector2(0.0, 100.0)
                ),
                interiors = listOf(
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
            )
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertEquals(3, shape.contours.size, "Should have 1 exterior + 2 holes")

        // First contour is exterior
        assertEquals(Winding.CLOCKWISE, shape.contours[0].winding)

        // Remaining contours are holes
        assertEquals(Winding.COUNTER_CLOCKWISE, shape.contours[1].winding)
        assertEquals(Winding.COUNTER_CLOCKWISE, shape.contours[2].winding)
    }

    // ============================================
    // Multiple polygons with mixed holes tests
    // ============================================

    @Test
    fun `multipolygon with multiple polygons and mixed holes combines correctly`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            // First polygon with hole
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(50.0, 0.0),
                    Vector2(50.0, 50.0),
                    Vector2(0.0, 50.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(10.0, 10.0),
                        Vector2(40.0, 10.0),
                        Vector2(40.0, 40.0),
                        Vector2(10.0, 40.0)
                    )
                )
            ),
            // Second polygon without hole
            geo.Polygon(listOf(
                Vector2(60.0, 0.0),
                Vector2(110.0, 0.0),
                Vector2(110.0, 50.0),
                Vector2(60.0, 50.0)
            ))
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        // 1 exterior (poly1) + 1 hole (poly1) + 1 exterior (poly2) = 3 contours
        assertEquals(3, shape.contours.size)
    }

    // ============================================
    // Adjacent polygon seam tests
    // ============================================

    @Test
    fun `adjacent polygons combine without error`() {
        // Two polygons sharing an edge at x=50
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            )),
            geo.Polygon(listOf(
                Vector2(50.0, 0.0),
                Vector2(100.0, 0.0),
                Vector2(100.0, 50.0),
                Vector2(50.0, 50.0)
            ))
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertEquals(2, shape.contours.size)
        assertTrue(shape.contours.all { it.winding == Winding.CLOCKWISE })
    }

    // ============================================
    // Shape construction error handling tests
    // ============================================

    @Test
    fun `empty multipolygon creates empty shape`() {
        val multiPolygon = geo.MultiPolygon(emptyList())

        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertEquals(0, shape.contours.size, "Empty MultiPolygon should create empty Shape")
    }

    @Test
    fun `single polygon multipolygon creates shape with one contour`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            ))
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        assertEquals(1, shape.contours.size)
        assertEquals(Winding.CLOCKWISE, shape.contours[0].winding)
    }

    @Test
    fun `multipolygon with degenerate polygon skips degenerate`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            )),
            // Degenerate polygon (line, not area)
            geo.Polygon(listOf(
                Vector2(100.0, 0.0),
                Vector2(150.0, 0.0)
            ))
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        // Should only have 1 contour (degenerate polygon skipped)
        assertEquals(1, shape.contours.size)
    }

    // ============================================
    // Winding normalization with projection tests
    // ============================================

    @Test
    fun `multipolygon with projection preserves winding rules`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(100.0, 0.0),
                    Vector2(100.0, 100.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(25.0, 25.0),
                        Vector2(75.0, 25.0),
                        Vector2(50.0, 75.0)
                    )
                )
            )
        ))

        // Apply a simple projection transformation
        val projection: (Vector2) -> Vector2 = { v ->
            Vector2(v.x * 2.0, v.y * 2.0)  // Scale by 2
        }

        val shape = createMultiPolygonShape(multiPolygon, projection)

        assertEquals(2, shape.contours.size)

        // After projection and normalization:
        // Exterior should still be clockwise
        assertEquals(Winding.CLOCKWISE, shape.contours[0].winding)

        // Hole should still be counter-clockwise
        assertEquals(Winding.COUNTER_CLOCKWISE, shape.contours[1].winding)

        // Verify projection was applied
        val projectedPoint = shape.contours[0].segments[0].start
        assertEquals(0.0, projectedPoint.x, 0.0001)
        assertEquals(0.0, projectedPoint.y, 0.0001)
    }

    @Test
    fun `multipolygon with incorrect winding gets normalized`() {
        // Polygon with counter-clockwise exterior (wrong for screen space)
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(0.0, 100.0),
                    Vector2(100.0, 100.0),
                    Vector2(100.0, 0.0)
                ),
                interiors = listOf(
                    // Hole with clockwise winding (wrong for screen space)
                    listOf(
                        Vector2(25.0, 25.0),
                        Vector2(75.0, 25.0),
                        Vector2(75.0, 75.0),
                        Vector2(25.0, 75.0)
                    )
                )
            )
        ))

        val shape = createMultiPolygonShape(multiPolygon) { it }

        // After normalization:
        // Exterior should be clockwise (correct)
        assertEquals(
            Winding.CLOCKWISE,
            shape.contours[0].winding,
            "Exterior should be normalized to clockwise"
        )

        // Hole should be counter-clockwise (correct)
        assertEquals(
            Winding.COUNTER_CLOCKWISE,
            shape.contours[1].winding,
            "Hole should be normalized to counter-clockwise"
        )
    }

    // ============================================
    // Integration with geometry normalization
    // ============================================

    @Test
    fun `render multipolygon uses combined shape approach`() {
        val multiPolygon = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(0.0, 50.0)
            )),
            geo.Polygon(listOf(
                Vector2(60.0, 0.0),
                Vector2(110.0, 0.0),
                Vector2(110.0, 50.0),
                Vector2(60.0, 50.0)
            ))
        ))

        // TODO: Implement renderMultiPolygonShape helper
        val contours = prepareMultiPolygonContours(multiPolygon) { it }

        // Should produce a flat list of all contours
        assertEquals(2, contours.size)

        // All contours should be clockwise
        contours.forEach { contour ->
            assertEquals(Winding.CLOCKWISE, contour.winding)
        }
    }
}

// TODO: Implement these functions in MultiRenderer.kt or geometry utilities (plans 16-01, 16-02)

/**
 * Creates a Shape from a MultiPolygon with proper winding normalization.
 *
 * @param multiPolygon The MultiPolygon to convert
 * @param projection Optional projection function to transform coordinates
 * @return A Shape with all contours combined (exteriors clockwise, holes counter-clockwise)
 */
fun createMultiPolygonShape(
    multiPolygon: geo.MultiPolygon,
    projection: (Vector2) -> Vector2 = { it }
): Shape {
    TODO("Implement in plan 16-02: Create Shape from MultiPolygon with proper winding")
}

/**
 * Prepares contours for MultiPolygon rendering.
 *
 * @param multiPolygon The MultiPolygon to prepare
 * @param projection Optional projection function
 * @return List of normalized ShapeContours ready for Shape creation
 */
fun prepareMultiPolygonContours(
    multiPolygon: geo.MultiPolygon,
    projection: (Vector2) -> Vector2 = { it }
): List<ShapeContour> {
    TODO("Implement in plan 16-02: Prepare contours for combined Shape rendering")
}
