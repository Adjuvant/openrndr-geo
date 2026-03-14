package geo.render

import org.openrndr.shape.ShapeContour
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

class DrawerGeoExtensionsCacheTest {

    @Test
    fun `test LineString contour is open`() {
        val points = listOf(
            org.openrndr.math.Vector2(0.0, 0.0),
            org.openrndr.math.Vector2(1.0, 1.0)
        )
        val openContour = ShapeContour.fromPoints(points, closed = false)
        val closedContour = ShapeContour.fromPoints(points, closed = true)

        // Length of open contour should be less than closed contour due to missing closing segment
        assertTrue(openContour.length < closedContour.length)
    }

    @Test
    fun `test Point contour size`() {
        val points = listOf(org.openrndr.math.Vector2(2.0, 3.0))
        val contour = ShapeContour.fromPoints(points, closed = false)

        // For single point contour, length should be zero
        assertEquals(0.0, contour.length, 0.0001)
    }
}
