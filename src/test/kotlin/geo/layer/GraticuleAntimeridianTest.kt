package geo.layer

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds
import geo.core.Feature
import geo.core.LineString

/**
 * Tests for antimeridian handling in graticule generation.
 * 
 * Covers handling of graticule lines when bounds span the ±180° boundary.
 */
class GraticuleAntimeridianTest {

    @Test
    fun `antimeridian crossing bounds produce lines on both sides`() {
        // Bounds spanning the antimeridian (e.g., 170° to -170°)
        val bounds = Bounds(170.0, -10.0, -170.0, 10.0)
        val result = generateGraticuleLines(bounds, 30.0)
        
        val lngLines = result.lngLines.features.toList()
        
        // With 170 to -170 range crossing antimeridian:
        // Should have lines on both positive side (150, 180) and negative side (-180, -150)
        assertTrue("Should have longitude lines when crossing antimeridian", lngLines.isNotEmpty())
        
        // Check that we have lines on both sides of antimeridian
        val xValues = lngLines.map { 
            val geom = it.geometry as LineString
            geom.points.map { it.x }.average()
        }
        val hasPositive = xValues.any { it > 0 }
        val hasNegative = xValues.any { it < 0 }
        assertTrue("Should have lines on positive side", hasPositive)
        assertTrue("Should have lines on negative side", hasNegative)
    }

    @Test
    fun `longitude lines entirely on one side are not split`() {
        // Bounds entirely on one side of antimeridian
        val bounds = Bounds(-50.0, -10.0, 50.0, 10.0)
        val result = generateGraticuleLines(bounds, 30.0)
        
        val lngLines = result.lngLines.features.toList()
        
        // Each line should be a simple 2-point LineString (not split)
        for (feature in lngLines) {
            val geom = feature.geometry as LineString
            assertEquals("Non-split line should have exactly 2 points", 2, geom.points.size)
        }
    }

    @Test
    fun `split lines have correct coordinate continuity`() {
        // Bounds spanning antimeridian
        val bounds = Bounds(170.0, -10.0, -170.0, 10.0)
        val result = generateGraticuleLines(bounds, 30.0)
        
        val lngLines = result.lngLines.features.toList()
        
        // After proper handling, no line segment should have a huge gap
        // (which would indicate improper antimeridian handling)
        for (feature in lngLines) {
            val geom = feature.geometry as LineString
            val points = geom.points
            for (i in 0 until points.size - 1) {
                val diff = kotlin.math.abs(points[i + 1].x - points[i].x)
                // A diff > 180 indicates crossing without proper handling
                // But diff == 180 could be legitimate (line at boundary)
                assertTrue("No segment should have gap > 180° indicating improper handling", diff <= 180.0)
            }
        }
    }

    @Test
    fun `bounds not crossing antimeridian produce unsplit lines`() {
        // Bounds entirely in the Pacific (but not crossing antimeridian)
        val bounds = Bounds(100.0, -10.0, 150.0, 10.0)
        val result = generateGraticuleLines(bounds, 30.0)
        
        val lngLines = result.lngLines.features.toList()
        
        // Each line should be a simple 2-point LineString
        assertTrue("Should have longitude lines", lngLines.isNotEmpty())
        for (feature in lngLines) {
            val geom = feature.geometry as LineString
            assertEquals("Each line should have exactly 2 points for non-crossing case", 2, geom.points.size)
        }
    }
}
