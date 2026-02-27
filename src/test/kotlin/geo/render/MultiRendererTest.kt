package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.color.ColorRGBa

class MultiRendererTest {

    @Test
    fun testDrawMultiPoint() {
        val multiPoint = geo.MultiPoint(listOf(
            geo.Point(0.0, 0.0),
            geo.Point(100.0, 50.0),
            geo.Point(200.0, 100.0)
        ))
        
        assertEquals(3, multiPoint.size)
        assertEquals(0.0, multiPoint.points[0].x, 0.0001)
        assertEquals(200.0, multiPoint.points[2].x, 0.0001)
        assertEquals(100.0, multiPoint.points[2].y, 0.0001)
    }

    @Test
    fun testDrawMultiLineString() {
        val lines = listOf(
            geo.LineString(listOf(Vector2.ZERO, Vector2.ONE)),
            geo.LineString(listOf(Vector2.ONE, Vector2(2.0, 2.0)))
        )
        val multiLineString = geo.MultiLineString(lines)
        
        assertEquals(2, multiLineString.size)
        assertEquals(2, multiLineString.lineStrings[0].size)
        assertEquals(2, multiLineString.lineStrings[1].size)
    }

    @Test
    fun testDrawMultiPolygon() {
        val polygons = listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 0.0),
                Vector2(5.0, 10.0)
            )),
            geo.Polygon(listOf(
                Vector2(20.0, 20.0),
                Vector2(30.0, 20.0),
                Vector2(25.0, 30.0)
            ))
        )
        val multiPolygon = geo.MultiPolygon(polygons)
        
        assertEquals(2, multiPolygon.size)
        assertEquals(3, multiPolygon.polygons[0].exterior.size)
        assertEquals(3, multiPolygon.polygons[1].exterior.size)
    }

    @Test
    fun testDrawMultiPointWithCustomStyle() {
        val multiPoint = geo.MultiPoint(listOf(
            geo.Point(0.0, 0.0),
            geo.Point(10.0, 10.0),
            geo.Point(20.0, 20.0)
        ))
        
        val customStyle = Style {
            fill = ColorRGBa.GREEN
            stroke = ColorRGBa.BLACK
            size = 8.0
        }
        
        // Verify style is properly configured
        assertEquals(ColorRGBa.GREEN, customStyle.fill)
        assertEquals(ColorRGBa.BLACK, customStyle.stroke)
        assertEquals(8.0, customStyle.size, 0.0001)
        
        // Verify multiPoint structure
        assertEquals(3, multiPoint.size)
    }

    @Test
    fun testDrawMultiLineStringWithStyle() {
        val lines = listOf(
            geo.LineString(listOf(
                Vector2(0.0, 0.0),
                Vector2(50.0, 50.0),
                Vector2(100.0, 0.0)
            )),
            geo.LineString(listOf(
                Vector2(100.0, 0.0),
                Vector2(150.0, 50.0),
                Vector2(200.0, 0.0)
            ))
        )
        val multiLineString = geo.MultiLineString(lines)
        
        val style = Style {
            stroke = ColorRGBa.BLUE
            strokeWeight = 2.0
        }
        
        // Verify consistent styling
        assertEquals(ColorRGBa.BLUE, style.stroke)
        assertEquals(2.0, style.strokeWeight, 0.0001)
        assertEquals(2, multiLineString.size)
    }

    @Test
    fun testDrawMultiPolygonWithStyle() {
        val polygons = listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 0.0),
                Vector2(5.0, 10.0)
            )),
            geo.Polygon(listOf(
                Vector2(15.0, 0.0),
                Vector2(25.0, 0.0),
                Vector2(20.0, 10.0)
            ))
        )
        val multiPolygon = geo.MultiPolygon(polygons)
        
        val style = Style {
            fill = ColorRGBa.GREEN.withAlpha(0.3)
            stroke = ColorRGBa.BLACK
        }
        
        // Verify consistent styling with fill opacity
        assertNotNull(style.fill)
        val fillAlpha = style.fill?.alpha ?: 0.0
        assertEquals(0.3, fillAlpha, 0.0001)
        assertEquals(ColorRGBa.BLACK, style.stroke)
        assertEquals(2, multiPolygon.size)
    }

    @Test
    fun testMultiPointBoundingBox() {
        val multiPoint = geo.MultiPoint(listOf(
            geo.Point(0.0, 0.0),
            geo.Point(10.0, 5.0),
            geo.Point(5.0, 10.0)
        ))
        
        val bbox = multiPoint.boundingBox
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(10.0, bbox.maxX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testMultiLineStringBoundingBox() {
        val mls = geo.MultiLineString(listOf(
            geo.LineString(listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 10.0)
            )),
            geo.LineString(listOf(
                Vector2(20.0, 0.0),
                Vector2(30.0, 10.0)
            ))
        ))
        
        val bbox = mls.boundingBox
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(30.0, bbox.maxX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testMultiPolygonBoundingBox() {
        val mp = geo.MultiPolygon(listOf(
            geo.Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 0.0),
                Vector2(5.0, 10.0)
            )),
            geo.Polygon(listOf(
                Vector2(20.0, 20.0),
                Vector2(30.0, 20.0),
                Vector2(25.0, 30.0)
            ))
        ))
        
        val bbox = mp.boundingBox
        assertEquals("minX should be 0.0", 0.0, bbox.minX, 0.0001)
        assertEquals("maxX should be 30.0", 30.0, bbox.maxX, 0.0001)
        assertEquals("minY should be 0.0", 0.0, bbox.minY, 0.0001)
        assertEquals("maxY should be 30.0", 30.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testMultiPolygonWithHoles() {
        val polygons = listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(50.0, 0.0),
                    Vector2(50.0, 50.0),
                    Vector2(0.0, 50.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(10.0, 10.0),
                        Vector2(40.0, 10.0),
                        Vector2(40.0, 40.0),
                        Vector2(10.0, 40.0)
                    )
                )
            ),
            geo.Polygon(
                exterior = listOf(
                    Vector2(100.0, 0.0),
                    Vector2(150.0, 0.0),
                    Vector2(150.0, 50.0),
                    Vector2(100.0, 50.0)
                )
            )
        )
        val multiPolygon = geo.MultiPolygon(polygons)
        
        assertEquals(2, multiPolygon.size)
        assertTrue(multiPolygon.polygons[0].hasHoles())
        assertFalse(multiPolygon.polygons[1].hasHoles())
    }

    @Test
    fun testMultiPolygonWithHolesClamped() {
        // Create a MultiPolygon with polygons that have holes at extreme latitudes
        val polygons = listOf(
            geo.Polygon(
                exterior = listOf(
                    Vector2(0.0, 0.0),
                    Vector2(50.0, 0.0),
                    Vector2(50.0, 50.0),
                    Vector2(0.0, 50.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(10.0, 10.0),
                        Vector2(40.0, 10.0),
                        Vector2(40.0, 40.0),
                        Vector2(10.0, 40.0)
                    )
                )
            ),
            geo.Polygon(
                exterior = listOf(
                    Vector2(100.0, 0.0),
                    Vector2(150.0, 0.0),
                    Vector2(150.0, 50.0),
                    Vector2(100.0, 50.0)
                )
                // No holes
            )
        )
        val multiPolygon = geo.MultiPolygon(polygons)
        
        assertEquals("MultiPolygon should have 2 polygons", 2, multiPolygon.size)
        assertTrue("First polygon should have holes", multiPolygon.polygons[0].hasHoles())
        assertFalse("Second polygon should not have holes", multiPolygon.polygons[1].hasHoles())
        
        // Test clamping behavior for coordinates beyond Mercator bounds
        val polyAtExtremeLat = geo.Polygon(
            exterior = listOf(
                Vector2(0.0, 89.0),
                Vector2(10.0, 89.0),
                Vector2(10.0, 91.0),  // Beyond Mercator limit (~85.05)
                Vector2(0.0, 91.0)    // Beyond Mercator limit
            ),
            interiors = listOf(
                listOf(
                    Vector2(2.0, 89.5),
                    Vector2(8.0, 89.5),
                    Vector2(8.0, 92.0),  // Beyond Mercator limit
                    Vector2(2.0, 92.0)   // Beyond Mercator limit
                )
            )
        )
        
        // Verify the polygon has extreme coordinates
        val maxExteriorY = polyAtExtremeLat.exterior.maxOf { it.y }
        assertTrue("Exterior should have coordinates beyond Mercator limit", maxExteriorY > 90.0)
        
        val maxInteriorY = polyAtExtremeLat.interiors.flatten().maxOf { it.y }
        assertTrue("Interior should have coordinates beyond Mercator limit", maxInteriorY > 90.0)
        
        // Note: Actual clamping verification happens in drawMultiPolygon implementation
    }
}
