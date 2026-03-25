package geo.layer

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds

/**
 * Tests for adaptive spacing logic in graticule generation.
 * 
 * Covers spacing selection based on viewport size using power-of-10 grid.
 */
class GraticuleSpacingTest {

    @Test
    fun `calculateAdaptiveSpacing returns 1 degree for very zoomed in view`() {
        // Bounds with 1 degree span should use 1 degree spacing
        val bounds = Bounds(-0.5, -0.5, 0.5, 0.5)
        assertEquals(1.0, calculateAdaptiveSpacing(bounds), 0.001)
    }

    @Test
    fun `calculateAdaptiveSpacing returns 10 degrees for regional view`() {
        // Bounds with ~10 degree span should use 10 degree spacing
        val bounds = Bounds(-10.0, -5.0, 5.0, 5.0)  // width = 15
        assertEquals(10.0, calculateAdaptiveSpacing(bounds), 0.001)
    }

    @Test
    fun `calculateAdaptiveSpacing returns 30 degrees for continental view`() {
        // Bounds with ~45 degree span should use 30 degree spacing  
        val bounds = Bounds(-50.0, -30.0, 0.0, 20.0)  // width = 50, height = 50, max = 50 < 60
        assertEquals(30.0, calculateAdaptiveSpacing(bounds), 0.001)
    }

    @Test
    fun `calculateAdaptiveSpacing returns 90 degrees for global view`() {
        // Bounds spanning nearly the whole world should use 90 degree spacing
        val bounds = Bounds(-180.0, -60.0, 180.0, 60.0)  // width = 360, height = 120
        assertEquals(90.0, calculateAdaptiveSpacing(bounds), 0.001)
    }

    @Test
    fun `calculateAdaptiveSpacing has minimum floor of 1 degree`() {
        // Very small bounds should still return 1.0
        val bounds = Bounds(0.0, 0.0, 0.1, 0.1)  // 0.1 degree span
        assertEquals(1.0, calculateAdaptiveSpacing(bounds), 0.001)
    }
}
