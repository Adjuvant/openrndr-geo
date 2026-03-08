package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.Winding

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

        assertNotNull("Should create a Shape from MultiPolygon", shape)
        assertEquals(
    "Shape should have 2 contours (one per polygon)",
    2,
    shape.contours.size
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
    "Contour $index should be clockwise",
    Winding.CLOCKWISE,
    contour.winding
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
    "Shape should have 2 contours (1 exterior + 1 hole)",
    2,
    shape.contours.size
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
    "Exterior contour should be clockwise",
    Winding.CLOCKWISE,
    exteriorContour.winding
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
    "Hole contour should be counter-clockwise",
    Winding.COUNTER_CLOCKWISE,
    holeContour.winding
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

        assertEquals("Should have 1 exterior + 2 holes", 3, shape.contours.size)

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
    "Exterior should be normalized to clockwise",
    Winding.CLOCKWISE,
    shape.contours[0].winding
)

        // Hole should be counter-clockwise (correct)
        assertEquals(
    "Hole should be normalized to counter-clockwise",
    Winding.COUNTER_CLOCKWISE,
    shape.contours[1].winding
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

// Helper functions for MultiPolygon rendering tests

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
    val contours = prepareMultiPolygonContours(multiPolygon, projection)
    return Shape(contours)
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
    val contours = mutableListOf<ShapeContour>()

    multiPolygon.polygons.forEach { polygon ->
        // Project and add exterior with clockwise winding
        val screenExterior = polygon.exterior.map { projection(it) }
        if (screenExterior.size >= 3) {
            val extContour = ShapeContour.fromPoints(screenExterior, closed = true).clockwise
            contours.add(extContour)
        }

        // Project and add interiors with counter-clockwise winding
        polygon.interiors.forEach { ring ->
            if (ring.size >= 3) {
                val screenRing = ring.map { projection(it) }
                val holeContour = ShapeContour.fromPoints(screenRing, closed = true).counterClockwise
                contours.add(holeContour)
            }
        }
    }

    return contours
}
