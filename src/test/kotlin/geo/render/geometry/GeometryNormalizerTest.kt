package geo.render.geometry

import geo.core.Polygon
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class GeometryNormalizerTest {

    @Test
    fun testNormalizePolygonReturnsValidPolygon() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 0.0),
            Vector2(100.0, 100.0),
            Vector2(0.0, 100.0)
        )
        val polygon = Polygon(exterior)

        val normalized = normalizePolygon(polygon)

        assertNotNull(normalized)
        assertEquals("Should return single polygon when no antimeridian crossing", 1, normalized.size)
        assertTrue(normalized[0].exterior.size >= 3)
    }

    @Test
    fun testNormalizePolygonWithHolesDropsDegenerateHoles() {
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

        val polygon = Polygon(exterior, listOf(validHole, degenerateHole))
        val normalized = normalizePolygon(polygon)

        assertEquals(1, normalized.size)
        assertEquals(1, normalized[0].interiors.size)
    }

    @Test
    fun testNormalizePolygonExtensionFunction() {
        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(10.0, 10.0),
            Vector2(0.0, 10.0)
        )
        val polygon = Polygon(exterior)

        val normalized = polygon.normalized()

        assertNotNull(normalized)
        assertEquals(1, normalized.size)
    }

    @Test
    fun testNormalizePolygonWithAntimeridianCrossing() {
        // Ring crossing antimeridian - needs at least 3 points per split part
        val exterior = listOf(
            Vector2(170.0, 20.0),
            Vector2(175.0, 25.0),  // Additional point
            Vector2(-175.0, 25.0), // Additional point
            Vector2(-170.0, 20.0),
            Vector2(-170.0, 30.0),
            Vector2(-175.0, 35.0), // Additional point
            Vector2(175.0, 35.0),  // Additional point
            Vector2(170.0, 30.0),
            Vector2(170.0, 20.0)
        )
        val polygon = Polygon(exterior)

        val normalized = normalizePolygon(polygon)

        assertNotNull(normalized)
        // When crossing antimeridian, polygon may be split into multiple parts
        assertTrue("Should return at least one polygon", normalized.size >= 1)
        // Each polygon should have valid exterior
        normalized.forEach { part ->
            assertTrue("Each part should have valid exterior", part.exterior.size >= 3)
        }
    }
}
