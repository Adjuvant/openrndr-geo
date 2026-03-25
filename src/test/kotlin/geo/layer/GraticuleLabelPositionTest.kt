package geo.layer

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds
import geo.projection.ProjectionMercator
import org.openrndr.math.Vector2

/**
 * Tests for label placement at viewport edges.
 * 
 * Covers proper positioning of labels at left edge (lat) and bottom edge (lng).
 */
class GraticuleLabelPositionTest {

    @Test
    fun `GraticuleLabels contains latitudeLabels and longitudeLabels lists`() {
        val labels = GraticuleLabels(emptyList(), emptyList())
        assertNotNull(labels.latitudeLabels)
        assertNotNull(labels.longitudeLabels)
    }

    @Test
    fun `LabelPosition has text coordinate and projected position`() {
        val label = LabelPosition(
            text = "45°N",
            longitude = 45.0,
            latitude = 0.0,
            projectedX = 100.0,
            projectedY = 200.0
        )
        assertEquals("45°N", label.text)
        assertEquals(45.0, label.longitude, 0.001)
        assertEquals(0.0, label.latitude, 0.001)
        assertEquals(100.0, label.projectedX, 0.001)
        assertEquals(200.0, label.projectedY, 0.001)
    }

    @Test
    fun `GraticuleLabels groups lat and lng labels separately`() {
        val latLabels = listOf(
            LabelPosition("45°N", 0.0, 45.0, 100.0, 200.0)
        )
        val lngLabels = listOf(
            LabelPosition("90°E", 90.0, 0.0, 300.0, 400.0)
        )
        val labels = GraticuleLabels(latLabels, lngLabels)
        
        assertEquals(1, labels.latitudeLabels.size)
        assertEquals(1, labels.longitudeLabels.size)
        assertEquals("45°N", labels.latitudeLabels[0].text)
        assertEquals("90°E", labels.longitudeLabels[0].text)
    }

    @Test
    fun `generateGraticuleLabels creates latitude labels at left edge`() {
        // Use bounds within valid Mercator range
        val bounds = Bounds(-10.0, 40.0, 10.0, 60.0)
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val labels = generateGraticuleLabels(bounds, projection, 10.0)
        
        // Latitude labels should be at left edge (x = projected position of bounds.minX)
        assertTrue("Should have latitude labels", labels.latitudeLabels.isNotEmpty())
        val leftEdgeX = projection.project(Vector2(bounds.minX, 0.0)).x
        for (label in labels.latitudeLabels) {
            // All latitude labels should have same x (at left edge)
            assertEquals("Latitude labels should be at left edge", leftEdgeX, label.projectedX, 5.0)
        }
    }

    @Test
    fun `generateGraticuleLabels creates longitude labels at bottom edge`() {
        // Use bounds within valid Mercator range
        val bounds = Bounds(-10.0, 40.0, 10.0, 60.0)
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val labels = generateGraticuleLabels(bounds, projection, 10.0)
        
        // Longitude labels should be at bottom edge (y = projected position of bounds.minY)
        assertTrue("Should have longitude labels", labels.longitudeLabels.isNotEmpty())
        val bottomEdgeY = projection.project(Vector2(0.0, bounds.minY)).y
        for (label in labels.longitudeLabels) {
            // All longitude labels should have same y (at bottom edge)
            assertEquals("Longitude labels should be at bottom edge", bottomEdgeY, label.projectedY, 5.0)
        }
    }
}
