package geo.projection

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import geo.exception.AccuracyWarningException

class ProjectionTest {

    @Test
    fun testMercatorProjectionTransformsLatLngToScreen() {
        val mercator = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        val london = mercator.project(Vector2(-0.1, 51.5))
        assertTrue("London x should be near center, got ${london.x}", london.x in 350.0..450.0)
        assertTrue("London y should be in upper half, got ${london.y}", london.y in 100.0..300.0)
    }

    @Test
    fun testEquirectangularProjectionWithDslSyntax() {
        val equirect = ProjectionEquirectangular {
            width = 800.0
            height = 600.0
        }
        val result = equirect.project(Vector2(0.0, 0.0))
        assertTrue("Center lon should map to center x", result.x in 350.0..450.0)
        assertTrue("Center lat should map to center y", result.y in 250.0..350.0)
    }

    @Test
    fun testBngCoordinateTransformation() {
        val bng = ProjectionBNG {
            width = 800.0
            height = 600.0
        }
        val latLng = Vector2(-0.1, 51.5)
        val screen = bng.project(latLng)
        assertTrue("London should map to screen x > 0", screen.x > 0)
        assertTrue("London should map to screen y > 0", screen.y > 0)
        
        val bngCoords = ProjectionBNG.latLngToBNG(latLng)
        assertTrue("Easting should be ~530000, got ${bngCoords.x}", bngCoords.x in 525000.0..535000.0)
        assertTrue("Northing should be ~180000, got ${bngCoords.y}", bngCoords.y in 175000.0..185000.0)
    }

    @Test(expected = AccuracyWarningException::class)
    fun testBngBoundsValidationThrowsForOutsideUk() {
        val bng = ProjectionBNG {
            width = 800.0
            height = 600.0
        }
        val paris = Vector2(2.35, 48.85)
        bng.project(paris)
    }

    @Test
    fun testProjectionFactoryPresets() {
        val mercator = ProjectionFactory.mercator(800.0, 600.0)
        assertNotNull(mercator)
        val result = mercator.project(Vector2(0.0, 0.0))
        assertTrue(result.x >= 0)
    }

    @Test
    fun testFitWorldConfiguration() {
        val mercator = ProjectionFactory.fitWorldMercator(800.0, 600.0)
        val left = mercator.project(Vector2(-180.0, 0.0))
        val right = mercator.project(Vector2(180.0, 0.0))
        assertTrue("Left edge should be less than right edge", left.x < right.x)
    }

    @Test
    fun testToScreenProceduralStyle() {
        val projection = ProjectionFactory.mercator(800.0, 600.0)
        val screen = toScreen(51.5, -0.1, projection)
        assertTrue(screen.x >= 0 && screen.y >= 0)
    }

    @Test
    fun testToScreenExtensionStyle() {
        val projection = ProjectionFactory.mercator(800.0, 600.0)
        val latLng = Vector2(-0.1, 51.5)
        val screen = latLng.toScreen(projection)
        assertTrue(screen.x >= 0 && screen.y >= 0)
    }

    @Test
    fun testFromScreenInverseTransformRoundTrips() {
        val projection = ProjectionFactory.mercator(800.0, 600.0)
        val original = Vector2(-0.1, 51.5)
        val screen = original.toScreen(projection)
        val back = fromScreen(screen.x, screen.y, projection)
        assertEquals("Longitude should round trip", original.x, back.x, 0.01)
        assertEquals("Latitude should round trip", original.y, back.y, 0.01)
    }

    @Test
    fun testBatchCoordinateTransformation() {
        val projection = ProjectionFactory.mercator(800.0, 600.0)
        val points = sequenceOf(
            Vector2(-0.1, 51.5),
            Vector2(-73.9, 40.7),
            Vector2(139.6, 35.6)
        )
        val screens = toScreen(points, projection)
        assertEquals(3, screens.count())
    }

    @Test
    fun testLatitudeClamping() {
        val clamped = clampLatitude(89.0)
        assertEquals("Should clamp to Web Mercator limit", 85.05112878, clamped, 0.0001)
    }

    @Test
    fun testLongitudeNormalization() {
        val normalized = normalizeLongitude(370.0)
        assertEquals("370° should normalize to 10°", 10.0, normalized, 0.001)
    }

    @Test
    fun testScreenVisibilityCheck() {
        val point = Vector2(100.0, 200.0)
        val bounds = Rectangle(0.0, 0.0, 800.0, 600.0)
        assertTrue(isOnScreen(point, bounds))
        assertFalse(isOnScreen(Vector2(-10.0, 200.0), bounds))
    }

    @Test
    fun testCoordinateValidation() {
        assertFalse("Invalid lat should return false", isValidCoordinate(Vector2(-100.0, 200.0)))
        assertTrue("Valid coordinates should return true", isValidCoordinate(Vector2(-0.1, 51.5)))
    }
}
