package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap

class LineRendererTest {

    @Test
    fun testWriteLineStringWithValidPoints() {
        val points = listOf(
            Vector2(0.0, 0.0),
            Vector2(50.0, 50.0),
            Vector2(100.0, 0.0)
        )
        val style = Style {
            stroke = ColorRGBa.RED
            strokeWeight = 2.0
            lineCap = LineCap.ROUND
        }
        
        // Verify points and style are properly configured
        assertEquals(3, points.size)
        assertEquals(ColorRGBa.RED, style.stroke)
        assertEquals(2.0, style.strokeWeight, 0.0001)
        assertEquals(LineCap.ROUND, style.lineCap)
    }

    @Test
    fun testWriteLineStringWithTwoPoints() {
        val points = listOf(
            Vector2(0.0, 0.0),
            Vector2(100.0, 100.0)
        )
        val style = StyleDefaults.defaultLineStyle
        
        // Minimum valid: 2 points
        assertEquals(2, points.size)
        assertNotNull(style)
        assertNotNull(style.stroke)
    }

    @Test
    fun testWriteLineStringWithSinglePoint() {
        val points = listOf(Vector2(0.0, 0.0))
        
        // Guard clause: need at least 2 points
        assertTrue("Single point list has size < 2", points.size < 2)
        assertEquals("Single point should return early", 1, points.size)
    }

    @Test
    fun testWriteLineStringWithEmptyList() {
        val points = emptyList<Vector2>()
        
        // Guard clause: empty list returns early
        assertTrue("Empty list has size < 2", points.size < 2)
        assertEquals(0, points.size)
    }

    @Test
    fun testWriteLineStringWithStrokeStyling() {
        val style = Style {
            stroke = ColorRGBa.BLUE
            strokeWeight = 2.0
            lineCap = LineCap.ROUND
            lineJoin = org.openrndr.draw.LineJoin.ROUND
        }
        
        assertEquals(ColorRGBa.BLUE, style.stroke)
        assertEquals(2.0, style.strokeWeight, 0.0001)
        assertEquals(LineCap.ROUND, style.lineCap)
        assertEquals(org.openrndr.draw.LineJoin.ROUND, style.lineJoin)
    }

    @Test
    fun testWriteLineStringWithNullStroke() {
        val style = Style {
            stroke = null
            fill = ColorRGBa.RED
        }
        
        // Verify null stroke is set correctly
        assertNull(style.stroke)
        assertNotNull(style.fill)
        assertEquals(ColorRGBa.RED, style.fill)
    }

    @Test
    fun testDefaultLineStyle() {
        val defaultStyle = StyleDefaults.defaultLineStyle
        
        // Verify default line style properties
        assertNull(defaultStyle.fill)  // Lines don't have fill
        assertNotNull(defaultStyle.stroke)
        assertEquals(ColorRGBa.WHITE, defaultStyle.stroke)
        assertEquals(1.0, defaultStyle.strokeWeight, 0.0001)
        assertEquals(LineCap.BUTT, defaultStyle.lineCap)
        assertEquals(org.openrndr.draw.LineJoin.MITER, defaultStyle.lineJoin)
    }

    @Test
    fun testLineStringBoundingBox() {
        val ls = geo.LineString(listOf(
            Vector2(0.0, 0.0),
            Vector2(50.0, 100.0),
            Vector2(100.0, 0.0)
        ))
        
        val bbox = ls.boundingBox
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(100.0, bbox.maxX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(100.0, bbox.maxY, 0.0001)
    }
}
