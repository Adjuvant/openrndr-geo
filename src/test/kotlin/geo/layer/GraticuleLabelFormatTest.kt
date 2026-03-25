package geo.layer

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for degree notation formatting (N/S/E/W).
 * 
 * Covers standard cartographic notation for degree labels.
 */
class GraticuleLabelFormatTest {

    @Test
    fun `formatLatitude produces N suffix for positive values`() {
        assertEquals("45°N", formatLatitude(45.0))
    }

    @Test
    fun `formatLatitude produces S suffix for negative values`() {
        assertEquals("30.5°S", formatLatitude(-30.5))
    }

    @Test
    fun `formatLatitude handles zero correctly`() {
        assertEquals("0°", formatLatitude(0.0))
    }

    @Test
    fun `formatLongitude produces E suffix for positive values`() {
        assertEquals("120°E", formatLongitude(120.0))
    }

    @Test
    fun `formatLongitude produces W suffix for negative values`() {
        assertEquals("90°W", formatLongitude(-90.0))
    }

    @Test
    fun `formatLongitude handles zero correctly`() {
        assertEquals("0°", formatLongitude(0.0))
    }

    @Test
    fun `formatLongitude handles 180 correctly`() {
        assertEquals("180°", formatLongitude(180.0))
    }
}
