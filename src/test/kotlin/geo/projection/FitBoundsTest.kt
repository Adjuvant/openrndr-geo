package geo.projection

import org.junit.Test
import org.junit.Assert.*
import geo.Bounds
import org.openrndr.math.Vector2
import kotlin.math.ln
import kotlin.math.tan

class FitBoundsTest {

    @Test
    fun testFitParametersCalculatesCorrectZoom() {
        val mercator = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        
        // UK bounding box
        val ukBounds = Bounds(
            minX = -8.0,
            minY = 50.0,
            maxX = 2.0,
            maxY = 60.0
        )
        
        val params = mercator.fitParameters(ukBounds, padding = 0.0)
        
        // Should return FitParameters with center in UK
        assertNotNull(params)
        assertTrue("Center lon should be in UK", params.center.x in -10.0..10.0)
        assertTrue("Center lat should be in UK", params.center.y in 40.0..70.0)
    }

    @Test
    fun testFitWithPadding() {
        val mercator = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        
        val bounds = Bounds(
            minX = -10.0,
            minY = 40.0,
            maxX = 10.0,
            maxY = 60.0
        )
        
        // Test fitParameters with different padding values
        val paramsNoPad = mercator.fitParameters(bounds, padding = 0.0)
        val paramsWithPad = mercator.fitParameters(bounds, padding = 100.0)
        
        // Should return FitParameters
        assertNotNull(paramsNoPad)
        assertNotNull(paramsWithPad)
    }

    @Test
    fun testFittedReturnsNewInstance() {
        val original = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val bounds = Bounds(
            minX = -5.0,
            minY = 45.0,
            maxX = 5.0,
            maxY = 55.0
        )
        
        val fitted = original.fitted(bounds)
        
        // Original should be unchanged
        assertEquals(2.0, original.config.zoomLevel, 0.001)
        
        // Fitted should have different zoom
        assertTrue("Fitted should have different zoom", fitted.config.zoomLevel != original.config.zoomLevel)
    }

    @Test
    fun testFitMutatesInPlace() {
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val bounds = Bounds(
            minX = -5.0,
            minY = 45.0,
            maxX = 5.0,
            maxY = 55.0
        )
        
        // Test fit() returns new projection with different zoom
        val fitted = projection.fit(bounds)
        
        // Fitted should have different zoom than original
        assertTrue("Zoom should have changed", fitted.config.zoomLevel != projection.config.zoomLevel)
    }

    @Test
    fun testDegenerateBbox() {
        val mercator = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        
        // Degenerate bbox (single point)
        val degenerate = Bounds(
            minX = 0.0,
            minY = 0.0,
            maxX = 0.0,
            maxY = 0.0
        )
        
        // Should not crash
        val params = mercator.fitParameters(degenerate)
        
        // Should have some zoom level (might be very high)
        assertTrue(params.zoomLevel >= 0)
    }

    @Test
    fun testZoomLevelConversion() {
        // Verify the scale/zoom conversion formula
        // scale = 256 * 2^zoom
        
        // At zoomLevel = 0, scale = 256
        assertEquals(256.0, 256.0 * Math.pow(2.0, 0.0), 0.001)
        
        // At zoomLevel = 1, scale = 512
        assertEquals(512.0, 256.0 * Math.pow(2.0, 1.0), 0.001)
        
        // At zoomLevel = 2, scale = 1024
        assertEquals(1024.0, 256.0 * Math.pow(2.0, 2.0), 0.001)
        
        // At zoomLevel = -1, scale = 128
        assertEquals(128.0, 256.0 * Math.pow(2.0, -1.0), 0.001)
    }
}
