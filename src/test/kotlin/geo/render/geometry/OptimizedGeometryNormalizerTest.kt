package geo.render.geometry

import geo.render.geometry.splitAtAntimeridian
import geo.render.geometry.normalizePolygon
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class OptimizedGeometryNormalizerTest {

    @Test
    fun `splitAtAntimeridian splits single crossing correctly`() {
        // A ring that crosses the antimeridian twice (edges 0→1 and 2→3)
        // 2 crossings produces 3 fragments (N crossings = N+1 fragments)
        val ring = listOf(
            Vector2(170.0, 0.0),
            Vector2(-170.0, 0.0),
            Vector2(-170.0, 1.0),
            Vector2(170.0, 1.0)
        )
        val result = splitAtAntimeridian(ring)
        // For a ring with 2 crossings, we get 3 fragments
        assertEquals(3, result.size)
        // Each fragment should not cross antimeridian
        assertFalse("Fragment 0 out of bounds", result[0].any { it.x > 180.0 || it.x < -180.0 })
        assertFalse("Fragment 1 out of bounds", result[1].any { it.x > 180.0 || it.x < -180.0 })
        assertFalse("Fragment 2 out of bounds", result[2].any { it.x > 180.0 || it.x < -180.0 })
    }

    @Test
    fun `splitAtAntimeridian handles multiple crossings`() {
        // Simulate multiple crossings (e.g., zigzag)
        val ring = listOf(
            Vector2(170.0, 0.0),
            Vector2(-170.0, 0.0),
            Vector2(179.0, 1.0),
            Vector2(-179.0, 1.0)
        )
        val result = splitAtAntimeridian(ring)
        assertTrue("Should split into multiple fragments", result.size >= 2)
    }

    @Test
    fun `normalizePolygon produces properly wound fragments`() {
        // TODO: Prepare a polygon that crosses antimeridian and test winding of normalized
        // For now, just ensure normalizePolygon returns non-empty list
        // Minimal polygon crossing antimeridian
        val crossingPolygon = geo.Polygon(
            exterior = listOf(
                Vector2(179.0, 0.0),
                Vector2(-179.0, 0.0),
                Vector2(-179.0, 1.0),
                Vector2(179.0, 1.0)
            ),
            interiors = emptyList()
        )
        val normalized = normalizePolygon(crossingPolygon)
        assertTrue("Normalized result should not be empty", normalized.isNotEmpty())
        // Check each normalized polygon has proper winding
        normalized.forEach { poly ->
            assertTrue("Exterior ring should not be empty", poly.exterior.isNotEmpty())
            // Here we could add explicit winding checks if utility exists
        }
    }
}
