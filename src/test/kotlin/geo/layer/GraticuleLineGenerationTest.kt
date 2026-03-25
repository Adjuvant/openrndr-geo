package geo.layer

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds
import geo.core.LineString
import geo.core.Feature

/**
 * Tests for LineString-based graticule line generation.
 * 
 * Covers latitude and longitude line creation as LineString features.
 */
class GraticuleLineGenerationTest {

    @Test
    fun `generateGraticuleLines returns latitude lines as horizontal LineStrings`() {
        val bounds = Bounds(-10.0, -5.0, 10.0, 5.0)
        val result = generateGraticuleLines(bounds, 10.0)
        
        // Get latitude lines from latLines GeoSource
        val latLines = result.latLines
        
        // Should have lines at -5, 5 (since spacing is 10 and bounds.minY=-5, bounds.maxY=5)
        // floor(-5/10)*10 = -10, then -10, 0 (2 lines total)
        val latCount = latLines.countFeatures()
        assertTrue("Should have at least 2 latitude lines, got $latCount", latCount >= 2)
    }

    @Test
    fun `generateGraticuleLines returns longitude lines as vertical LineStrings`() {
        val bounds = Bounds(-10.0, -5.0, 10.0, 5.0)
        val result = generateGraticuleLines(bounds, 10.0)
        
        // Get longitude lines from lngLines GeoSource
        val lngLines = result.lngLines
        
        // Should have lines at -10, 0, 10 (3 lines total)
        val lngCount = lngLines.countFeatures()
        assertEquals("Should have 3 longitude lines", 3, lngCount.toInt())
    }

    @Test
    fun `generateGraticuleLines creates lines at correct interval positions`() {
        val bounds = Bounds(0.0, 0.0, 25.0, 25.0)  // 25x25 degree region
        val result = generateGraticuleLines(bounds, 10.0)
        
        // For 0-25 range with 10° spacing:
        // Lines at 0, 10, 20, 30 (ceil(25/10)*10 = 30)
        val latCount = result.latLines.countFeatures().toInt()
        assertEquals("Should have 4 latitude lines for 0-25 range at 10° spacing", 4, latCount)
        
        val lngCount = result.lngLines.countFeatures().toInt()
        assertEquals("Should have 4 longitude lines for 0-25 range at 10° spacing", 4, lngCount)
    }

    @Test
    fun `generateGraticuleLines returns LineString geometry`() {
        val bounds = Bounds(-10.0, -10.0, 10.0, 10.0)
        val result = generateGraticuleLines(bounds, 10.0)
        
        val latFeature: Feature = result.latLines.features.first()
        val geom = latFeature.geometry
        
        // Geometry should be a LineString (each line is a LineString)
        assertTrue("Latitude geometry should be LineString, got ${geom::class.simpleName}", 
            geom is LineString)
    }
}
