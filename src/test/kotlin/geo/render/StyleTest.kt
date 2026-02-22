package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin

class StyleTest {

    @Test
    fun testDefaultValues() {
        val style = Style()
        assertNull(style.fill)
        assertEquals(ColorRGBa.WHITE, style.stroke)
        assertEquals(1.0, style.strokeWeight, 0.0001)
        assertEquals(5.0, style.size, 0.0001)
        assertEquals(Shape.Circle, style.shape)
        assertEquals(LineCap.BUTT, style.lineCap)
        assertEquals(LineJoin.MITER, style.lineJoin)
        assertEquals(4.0, style.miterLimit, 0.0001)
    }

    @Test
    fun testDslSyntax() {
        val style = Style {
            fill = ColorRGBa.RED
            stroke = ColorRGBa.BLUE
            strokeWeight = 2.5
            size = 10.0
            shape = Shape.Square
            lineCap = LineCap.ROUND
            lineJoin = LineJoin.ROUND
            miterLimit = 8.0
        }
        assertEquals(ColorRGBa.RED, style.fill)
        assertEquals(ColorRGBa.BLUE, style.stroke)
        assertEquals(2.5, style.strokeWeight, 0.0001)
        assertEquals(10.0, style.size, 0.0001)
        assertEquals(Shape.Square, style.shape)
        assertEquals(LineCap.ROUND, style.lineCap)
        assertEquals(LineJoin.ROUND, style.lineJoin)
        assertEquals(8.0, style.miterLimit, 0.0001)
    }

    @Test
    fun testAllProperties() {
        val style = Style()
        style.fill = ColorRGBa.GREEN
        style.stroke = ColorRGBa.BLACK
        style.strokeWeight = 3.0
        style.size = 15.0
        style.shape = Shape.Triangle
        style.lineCap = LineCap.SQUARE
        style.lineJoin = LineJoin.BEVEL
        style.miterLimit = 2.0

        assertEquals(ColorRGBa.GREEN, style.fill)
        assertEquals(ColorRGBa.BLACK, style.stroke)
        assertEquals(3.0, style.strokeWeight, 0.0001)
        assertEquals(15.0, style.size, 0.0001)
        assertEquals(Shape.Triangle, style.shape)
        assertEquals(LineCap.SQUARE, style.lineCap)
        assertEquals(LineJoin.BEVEL, style.lineJoin)
        assertEquals(2.0, style.miterLimit, 0.0001)
    }

    @Test
    fun testTransparentStyle() {
        val style = Style.transparent(0.5)
        assertNotNull(style.fill)
        assertEquals(0.5, style.fill?.a ?: 0.0, 0.0001)
        assertNotNull(style.stroke)
        assertEquals(0.5, style.stroke?.a ?: 0.0, 0.0001)
    }

    @Test
    fun testDefaultTransparent() {
        val style = Style.transparent()
        assertNotNull(style.fill)
        assertEquals(0.0, style.fill?.a ?: 0.0, 0.0001)
        assertNotNull(style.stroke)
        assertEquals(0.0, style.stroke?.a ?: 0.0, 0.0001)
    }

    @Test
    fun testWithAlphaExtension() {
        val color = ColorRGBa.RED.withAlpha(0.5)
        assertEquals(ColorRGBa.RED.r, color.r, 0.0001)
        assertEquals(ColorRGBa.RED.g, color.g, 0.0001)
        assertEquals(ColorRGBa.RED.b, color.b, 0.0001)
        assertEquals(0.5, color.a, 0.0001)
    }
}
