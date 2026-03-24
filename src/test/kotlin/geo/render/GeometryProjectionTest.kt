package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import geo.projection.*

class GeometryProjectionTest {

    @Test
    fun testPointToScreenMercator() {
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        
        // London coordinates
        val point = geo.core.Point(-0.1, 51.5)
        val screen = point.toScreen(projection)
        
        // Verify result is Vector2 with positive coordinates
        assertTrue("Screen x should be positive", screen.x > 0)
        assertTrue("Screen y should be positive", screen.y > 0)
        assertTrue("Screen x should be within bounds", screen.x <= 800.0)
        assertTrue("Screen y should be within bounds", screen.y <= 600.0)
    }

    @Test
    fun testPointToScreenEquirectangular() {
        val projection = ProjectionEquirectangular {
            width = 800.0
            height = 600.0
        }
        
        // Equator/Prime Meridian
        val point = geo.core.Point(0.0, 0.0)
        val screen = point.toScreen(projection)
        
        // Should be near center
        assertTrue("Center lon should map to center x", screen.x in 350.0..450.0)
        assertTrue("Center lat should map to center y", screen.y in 250.0..350.0)
    }

    @Test
    fun testPointToScreenReturnsVector2() {
        val projection = ProjectionFactory.mercator(800.0, 600.0)
        val point = geo.core.Point(-0.1, 51.5)
        val screen = point.toScreen(projection)
        
        // Verify the result is OpenRNDR Vector2 type
        assertNotNull(screen)
        assertTrue("Result should be Vector2", screen is Vector2)
        assertTrue("x should be finite", screen.x.isFinite())
        assertTrue("y should be finite", screen.y.isFinite())
    }

    @Test
    fun testPointToScreenWithBNG() {
        val projection = ProjectionBNG {
            width = 800.0
            height = 600.0
        }
        
        // London UK coordinates
        val point = geo.core.Point(-0.1, 51.5)
        val screen = point.toScreen(projection)
        
        // Verify valid screen coordinates for UK
        assertTrue("Screen x should be positive", screen.x > 0)
        assertTrue("Screen y should be positive", screen.y > 0)
        assertTrue("Screen x should be within bounds", screen.x <= 800.0)
        assertTrue("Screen y should be within bounds", screen.y <= 600.0)
    }

    @Test
    fun testPointToScreenWithDifferentProjections() {
        val point = geo.core.Point(-0.1, 51.5)
        
        // Test with default zoomLevel - projections should still work
        val mercator = ProjectionFactory.mercator(800.0, 600.0)
        val equirectangular = ProjectionFactory.equirectangular(800.0, 600.0)
        
        val mercatorScreen = point.toScreen(mercator)
        val equirectScreen = point.toScreen(equirectangular)
        
        // Verify projections work and return Vector2
        assertNotNull(mercatorScreen)
        assertNotNull(equirectScreen)
        
        // Different projections should give different results (that's the point)
        assertTrue(mercatorScreen != equirectScreen)
    }

    @Test
    fun testPointCoordinatesPreserved() {
        val point = geo.core.Point(123.456, 789.012)
        
        assertEquals(123.456, point.x, 0.0001)
        assertEquals(789.012, point.y, 0.0001)
    }

    @Test
    fun testPointToVector2() {
        val point = geo.core.Point(10.0, 20.0)
        val vec = point.toVector2()
        
        assertEquals(10.0, vec.x, 0.0001)
        assertEquals(20.0, vec.y, 0.0001)
    }

    @Test
    fun testPointFromVector2() {
        val vec = Vector2(10.0, 20.0)
        val point = geo.core.Point.fromVector2(vec)
        
        assertEquals(10.0, point.x, 0.0001)
        assertEquals(20.0, point.y, 0.0001)
    }
}
