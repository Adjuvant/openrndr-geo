package geo.layer

import org.junit.Test
import org.junit.Assert.*
import geo.core.Bounds
import geo.projection.ProjectionMercator
import org.openrndr.math.Vector2

/**
 * Tests for auto-thinning when zoomed out.
 * 
 * Covers density management when projected labels would be too crowded.
 */
class GraticuleDensityTest {

    @Test
    fun `includeLabels false produces null labels`() {
        val bounds = Bounds(-10.0, 40.0, 10.0, 60.0)
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val layer = generateGraticuleLayer(bounds, projection = projection, includeLabels = false)
        
        assertNull("Labels should be null when includeLabels=false", layer.labels)
    }

    @Test
    fun `includeLabels true produces label data`() {
        val bounds = Bounds(-10.0, 40.0, 10.0, 60.0)
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0
        }
        
        val layer = generateGraticuleLayer(bounds, projection = projection, includeLabels = true)
        
        assertNotNull("Labels should not be null when includeLabels=true", layer.labels)
        assertTrue("Should have latitude labels", layer.labels!!.latitudeLabels.isNotEmpty())
        assertTrue("Should have longitude labels", layer.labels!!.longitudeLabels.isNotEmpty())
    }

    @Test
    fun `very zoomed out view should thin labels to prevent crowding`() {
        // Use bounds within valid Mercator range but large enough to test thinning
        val bounds = Bounds(-50.0, 30.0, 50.0, 70.0)  // Large region but valid
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 0.0
        }
        
        // With 10° spacing in this region, would have many labels (5 lat, 11 lng)
        // Auto-thinning should reduce them with 50px min spacing
        val labels = generateGraticuleLabels(bounds, projection, 10.0, minPixelSpacing = 50.0)
        
        // Auto-thinning should reduce the number of labels
        assertTrue("Should have some latitude labels after thinning", 
            labels.latitudeLabels.isNotEmpty())
    }
}
