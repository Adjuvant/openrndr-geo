package geo

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class GeometryTest {

    @Test
    fun testPointCreation() {
        val pt = Point(1.0, 2.0)
        assertEquals(1.0, pt.x, 0.0001)
        assertEquals(2.0, pt.y, 0.0001)
    }

    @Test
    fun testPointBoundingBox() {
        val pt = Point(5.0, 10.0)
        val bbox = pt.boundingBox
        assertEquals(5.0, bbox.minX, 0.0001)
        assertEquals(5.0, bbox.maxX, 0.0001)
        assertEquals(10.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testPointToVector2() {
        val pt = Point(3.0, 4.0)
        val vec = pt.toVector2()
        assertEquals(3.0, vec.x, 0.0001)
        assertEquals(4.0, vec.y, 0.0001)
    }

    @Test
    fun testPointFromVector2() {
        val vec = Vector2(3.0, 4.0)
        val pt = Point.fromVector2(vec)
        assertEquals(3.0, pt.x, 0.0001)
        assertEquals(4.0, pt.y, 0.0001)
    }

    @Test
    fun testLineStringCreation() {
        val ls = LineString(listOf(Vector2.ZERO, Vector2.ONE))
        assertEquals(2, ls.points.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testLineStringRequiresAtLeastTwoPoints() {
        LineString(listOf(Vector2.ZERO))
    }

    @Test
    fun testLineStringBoundingBox() {
        val ls = LineString(listOf(
            Vector2(0.0, 0.0),
            Vector2(5.0, 10.0),
            Vector2(10.0, 0.0)
        ))
        val bbox = ls.boundingBox
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(10.0, bbox.maxX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testPolygonCreation() {
        val poly = Polygon(listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(5.0, 10.0)
        ))
        assertFalse(poly.hasHoles())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPolygonRequiresAtLeastThreePoints() {
        Polygon(listOf(Vector2.ZERO, Vector2.ONE))
    }

    @Test
    fun testPolygonWithHoles() {
        val poly = Polygon(
            exterior = listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 0.0),
                Vector2(10.0, 10.0),
                Vector2(0.0, 10.0)
            ),
            interiors = listOf(
                listOf(
                    Vector2(2.0, 2.0),
                    Vector2(8.0, 2.0),
                    Vector2(8.0, 8.0),
                    Vector2(2.0, 8.0)
                )
            )
        )
        assertTrue(poly.hasHoles())
    }

    @Test
    fun testMultiPointCreation() {
        val mp = MultiPoint(listOf(
            Point(0.0, 0.0),
            Point(10.0, 10.0)
        ))
        assertEquals(2, mp.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMultiPointRequiresAtLeastOnePoint() {
        MultiPoint(emptyList())
    }

    @Test
    fun testMultiPointBoundingBox() {
        val mp = MultiPoint(listOf(
            Point(0.0, 0.0),
            Point(10.0, 5.0),
            Point(5.0, 10.0)
        ))
        val bbox = mp.boundingBox
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(10.0, bbox.maxX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testMultiLineStringCreation() {
        val mls = MultiLineString(listOf(
            LineString(listOf(Vector2.ZERO, Vector2.ONE)),
            LineString(listOf(Vector2.ONE, Vector2(2.0, 2.0)))
        ))
        assertEquals(2, mls.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMultiLineStringRequiresAtLeastOneLineString() {
        MultiLineString(emptyList())
    }

    @Test
    fun testMultiPolygonCreation() {
        val mp = MultiPolygon(listOf(
            Polygon(listOf(
                Vector2(0.0, 0.0),
                Vector2(10.0, 0.0),
                Vector2(5.0, 10.0)
            )),
            Polygon(listOf(
                Vector2(20.0, 20.0),
                Vector2(30.0, 20.0),
                Vector2(25.0, 30.0)
            ))
        ))
        assertEquals(2, mp.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMultiPolygonRequiresAtLeastOnePolygon() {
        MultiPolygon(emptyList())
    }

    @Test
    fun testSealedClassExhaustiveWhen() {
        // This test verifies the sealed class allows exhaustive when expressions
        val geometries = listOf(
            Point(0.0, 0.0),
            LineString(listOf(Vector2.ZERO, Vector2.ONE)),
            Polygon(listOf(Vector2.ZERO, Vector2.ONE, Vector2(0.0, 1.0))),
            MultiPoint(listOf(Point(0.0, 0.0))),
            MultiLineString(listOf(LineString(listOf(Vector2.ZERO, Vector2.ONE)))),
            MultiPolygon(listOf(Polygon(listOf(Vector2.ZERO, Vector2.ONE, Vector2(0.0, 1.0)))))
        )

        for (geom in geometries) {
            val result = when (geom) {
                is Point -> "point"
                is LineString -> "linestring"
                is Polygon -> "polygon"
                is MultiPoint -> "multipoint"
                is MultiLineString -> "multilinestring"
                is MultiPolygon -> "multipolygon"
            }
            assertNotNull(result)
        }
    }
}
