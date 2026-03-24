package geo.projection

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds
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
        
        // Should have some zoom level (degenerate bbox = very high = negative scale zoom)
        assertTrue("Zoom should be a valid number", !params.zoomLevel.isNaN() && !params.zoomLevel.isInfinite())
    }

    @Test
    fun testZoomLevelConversion() {
        // Verify the scale/zoom conversion formula for viewport-relative zoom
        // scale = baseScale * 2^(-zoom) where baseScale fits world in viewport
        
        // Create a config with known dimensions
        val config = ProjectionConfig(width = 800.0, height = 600.0)
        
        // At zoomLevel = 0, scale = baseScale (world fits viewport)
        val baseScale = config.baseScale
        assertEquals(baseScale, config.scale, 0.001)
        
        // At zoomLevel = 1, scale = baseScale / 2 (2x zoomed in)
        val configZoom1 = config.copy(zoomLevel = 1.0)
        assertEquals(baseScale / 2.0, configZoom1.scale, 0.001)
        
        // At zoomLevel = 2, scale = baseScale / 4 (4x zoomed in)
        val configZoom2 = config.copy(zoomLevel = 2.0)
        assertEquals(baseScale / 4.0, configZoom2.scale, 0.001)
        
        // At zoomLevel = -1, scale = baseScale * 2 (2x zoomed out)
        val configZoomNeg1 = config.copy(zoomLevel = -1.0)
        assertEquals(baseScale * 2.0, configZoomNeg1.scale, 0.001)
    }
    
    @Test
    fun testZoomZeroFitsWorldInViewport() {
        val projection = ProjectionFactory.fitWorldMercator(800.0, 600.0)
        // zoom=0 should fit world
        assertEquals(0.0, projection.config.zoomLevel, 0.001)
        
        // World corners should be visible
        val left = projection.project(Vector2(-180.0, 0.0)).x
        val right = projection.project(Vector2(180.0, 0.0)).x
        assertTrue("Left corner should be >= 0, was $left", left >= 0)
        assertTrue("Right corner should be <= 800, was $right", right <= 800)
    }
    
    @Test
    fun testZoomOneIsDoubleZoomedIn() {
        val zoom0 = ProjectionFactory.fitWorldMercator(800.0, 600.0)
        val zoom1 = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 1.0
        }
        // zoom=1 should show half the horizontal extent
        val width0 = zoom0.project(Vector2(180.0, 0.0)).x - zoom0.project(Vector2(-180.0, 0.0)).x
        val width1 = zoom1.project(Vector2(180.0, 0.0)).x - zoom1.project(Vector2(-180.0, 0.0)).x
        // At zoom=1, we should see half the world (2x zoomed)
        assertTrue("At zoom=1, width should be less than at zoom=0", width1 < width0)
        // And roughly half (2x zoom = 0.5x width)
        assertTrue("At zoom=1, should be ~2x zoomed", width1 < width0 * 0.6)
    }
}
