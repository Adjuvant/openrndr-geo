package geo.core

import geo.projection.CRSTransformer
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

/**
 * Tests for geometry transformation using CRSTransformer.
 * Verifies structural integrity and coordinate transformation for all geometry types.
 */
class GeometryTransformTest {

    @Test
    fun testPointTransform() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val point = Point(530000.0, 180000.0) // London area in BNG

        val transformed = point.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a Point", transformed is Point)
        val result = transformed as Point

        // Verify coordinates changed (from BNG easting/northing to WGS84 lon/lat)
        // BNG coordinates are in range 0-700000 (easting), 0-1300000 (northing)
        // WGS84 coordinates are in range -180 to 180 (lon), -90 to 90 (lat)
        assertTrue("X should be transformed to WGS84 range", result.x > -180 && result.x < 180)
        assertTrue("Y should be transformed to WGS84 range", result.y > -90 && result.y < 90)
        // Verify values actually changed (not still in BNG range)
        assertTrue("X should no longer be BNG easting", result.x < 1000)
        assertTrue("Y should no longer be BNG northing", result.y < 1000)

        // Verify original point unchanged (immutability)
        assertEquals(530000.0, point.x, 0.0001)
        assertEquals(180000.0, point.y, 0.0001)
    }

    @Test
    fun testLineStringTransform() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val lineString = LineString(listOf(
            Vector2(530000.0, 180000.0),
            Vector2(531000.0, 181000.0),
            Vector2(532000.0, 182000.0)
        ))

        val transformed = lineString.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a LineString", transformed is LineString)
        val result = transformed as LineString

        // Verify all points were transformed
        assertEquals("Point count preserved", 3, result.points.size)

        // Verify coordinates changed to WGS84 range
        result.points.forEach { point ->
            assertTrue("X should be in WGS84 longitude range", point.x > -180 && point.x < 180)
            assertTrue("Y should be in WGS84 latitude range", point.y > -90 && point.y < 90)
            // Verify values actually changed from BNG
            assertTrue("X should no longer be BNG easting", point.x < 1000)
            assertTrue("Y should no longer be BNG northing", point.y < 1000)
        }

        // Verify original unchanged
        assertEquals(530000.0, lineString.points[0].x, 0.0001)
    }

    @Test
    fun testPolygonTransformWithHoles() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val polygon = Polygon(
            exterior = listOf(
                Vector2(530000.0, 180000.0), // Bottom-left
                Vector2(540000.0, 180000.0), // Bottom-right
                Vector2(540000.0, 190000.0), // Top-right
                Vector2(530000.0, 190000.0)  // Top-left
            ),
            interiors = listOf(
                // A hole inside the polygon
                listOf(
                    Vector2(534000.0, 184000.0),
                    Vector2(536000.0, 184000.0),
                    Vector2(536000.0, 186000.0),
                    Vector2(534000.0, 186000.0)
                )
            )
        )

        val transformed = polygon.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a Polygon", transformed is Polygon)
        val result = transformed as Polygon

        // Verify exterior ring preserved structure
        assertEquals("Exterior point count preserved", 4, result.exterior.size)

        // Verify interior rings (holes) preserved
        assertEquals("Interior ring count preserved", 1, result.interiors.size)
        assertEquals("Hole point count preserved", 4, result.interiors[0].size)

        // Verify all coordinates transformed to WGS84 range
        result.exterior.forEach { point ->
            assertTrue("Exterior X should be in WGS84 range", point.x > -180 && point.x < 180)
            assertTrue("Exterior Y should be in WGS84 range", point.y > -90 && point.y < 90)
            // Verify values actually changed from BNG
            assertTrue("Exterior X should no longer be BNG easting", point.x < 1000)
            assertTrue("Exterior Y should no longer be BNG northing", point.y < 1000)
        }

        result.interiors[0].forEach { point ->
            assertTrue("Interior X should be in WGS84 range", point.x > -180 && point.x < 180)
            assertTrue("Interior Y should be in WGS84 range", point.y > -90 && point.y < 90)
            // Verify values actually changed from BNG
            assertTrue("Interior X should no longer be BNG easting", point.x < 1000)
            assertTrue("Interior Y should no longer be BNG northing", point.y < 1000)
        }

        // Verify original unchanged
        assertEquals(530000.0, polygon.exterior[0].x, 0.0001)
        assertEquals(534000.0, polygon.interiors[0][0].x, 0.0001)
    }

    @Test
    fun testMultiPointTransform() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val multiPoint = MultiPoint(listOf(
            Point(530000.0, 180000.0),
            Point(531000.0, 181000.0),
            Point(532000.0, 182000.0)
        ))

        val transformed = multiPoint.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a MultiPoint", transformed is MultiPoint)
        val result = transformed as MultiPoint

        // Verify point count preserved
        assertEquals("Point count preserved", 3, result.points.size)

        // Verify all points are Point instances (delegation worked)
        result.points.forEach { point ->
            assertTrue("Each child should be a Point", point is Point)
            assertTrue("X should be in WGS84 range", point.x > -180 && point.x < 180)
            assertTrue("Y should be in WGS84 range", point.y > -90 && point.y < 90)
            // Verify values actually changed from BNG
            assertTrue("X should no longer be BNG easting", point.x < 1000)
            assertTrue("Y should no longer be BNG northing", point.y < 1000)
        }

        // Verify original unchanged
        assertEquals(530000.0, multiPoint.points[0].x, 0.0001)
    }

    @Test
    fun testMultiLineStringTransform() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val multiLineString = MultiLineString(listOf(
            LineString(listOf(
                Vector2(530000.0, 180000.0),
                Vector2(531000.0, 181000.0)
            )),
            LineString(listOf(
                Vector2(540000.0, 190000.0),
                Vector2(541000.0, 191000.0)
            ))
        ))

        val transformed = multiLineString.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a MultiLineString", transformed is MultiLineString)
        val result = transformed as MultiLineString

        // Verify line string count preserved
        assertEquals("LineString count preserved", 2, result.lineStrings.size)

        // Verify all line strings have correct structure
        result.lineStrings.forEach { lineString ->
            assertTrue("Each child should be a LineString", lineString is LineString)
            assertEquals("Each LineString has correct point count", 2, lineString.points.size)
            lineString.points.forEach { point ->
                assertTrue("X should be in WGS84 range", point.x > -180 && point.x < 180)
                assertTrue("Y should be in WGS84 range", point.y > -90 && point.y < 90)
                // Verify values actually changed from BNG
                assertTrue("X should no longer be BNG easting", point.x < 1000)
                assertTrue("Y should no longer be BNG northing", point.y < 1000)
            }
        }

        // Verify original unchanged
        assertEquals(530000.0, multiLineString.lineStrings[0].points[0].x, 0.0001)
    }

    @Test
    fun testMultiPolygonTransform() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val multiPolygon = MultiPolygon(listOf(
            Polygon(
                exterior = listOf(
                    Vector2(530000.0, 180000.0),
                    Vector2(540000.0, 180000.0),
                    Vector2(540000.0, 190000.0)
                )
            ),
            Polygon(
                exterior = listOf(
                    Vector2(550000.0, 200000.0),
                    Vector2(560000.0, 200000.0),
                    Vector2(560000.0, 210000.0)
                ),
                interiors = listOf(
                    listOf(
                        Vector2(554000.0, 204000.0),
                        Vector2(556000.0, 204000.0),
                        Vector2(556000.0, 206000.0)
                    )
                )
            )
        ))

        val transformed = multiPolygon.transform(transformer)

        // Verify type is preserved
        assertTrue("Should return a MultiPolygon", transformed is MultiPolygon)
        val result = transformed as MultiPolygon

        // Verify polygon count preserved
        assertEquals("Polygon count preserved", 2, result.polygons.size)

        // Verify first polygon has no holes
        assertEquals("First polygon has no holes", 0, result.polygons[0].interiors.size)

        // Verify second polygon preserves holes
        assertEquals("Second polygon preserves holes", 1, result.polygons[1].interiors.size)
        assertEquals("Hole point count preserved", 3, result.polygons[1].interiors[0].size)

        // Verify all coordinates transformed to WGS84 range
        result.polygons.forEach { polygon ->
            polygon.exterior.forEach { point ->
                assertTrue("Exterior X should be in WGS84 range", point.x > -180 && point.x < 180)
                assertTrue("Exterior Y should be in WGS84 range", point.y > -90 && point.y < 90)
                // Verify values actually changed from BNG
                assertTrue("Exterior X should no longer be BNG easting", point.x < 1000)
                assertTrue("Exterior Y should no longer be BNG northing", point.y < 1000)
            }
        }

        // Verify original unchanged
        assertEquals(530000.0, multiPolygon.polygons[0].exterior[0].x, 0.0001)
        assertEquals(554000.0, multiPolygon.polygons[1].interiors[0][0].x, 0.0001)
    }

    @Test
    fun testAllGeometryTypesHandled() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")

        // Create all 6 geometry types
        val geometries: List<Geometry> = listOf(
            Point(530000.0, 180000.0),
            LineString(listOf(Vector2(530000.0, 180000.0), Vector2(531000.0, 181000.0))),
            Polygon(listOf(
                Vector2(530000.0, 180000.0),
                Vector2(540000.0, 180000.0),
                Vector2(540000.0, 190000.0)
            )),
            MultiPoint(listOf(Point(530000.0, 180000.0))),
            MultiLineString(listOf(LineString(listOf(
                Vector2(530000.0, 180000.0),
                Vector2(531000.0, 181000.0)
            )))),
            MultiPolygon(listOf(Polygon(listOf(
                Vector2(530000.0, 180000.0),
                Vector2(540000.0, 180000.0),
                Vector2(540000.0, 190000.0)
            ))))
        )

        // Transform each geometry - should not throw
        val results = geometries.map { it.transform(transformer) }

        // Verify all 6 returned correct types
        assertTrue("1st should be Point", results[0] is Point)
        assertTrue("2nd should be LineString", results[1] is LineString)
        assertTrue("3rd should be Polygon", results[2] is Polygon)
        assertTrue("4th should be MultiPoint", results[3] is MultiPoint)
        assertTrue("5th should be MultiLineString", results[4] is MultiLineString)
        assertTrue("6th should be MultiPolygon", results[5] is MultiPolygon)

        // Verify all coordinates were transformed (no longer in BNG range)
        results.forEach { geometry ->
            when (geometry) {
                is Point -> {
                    assertTrue("Point should be transformed", geometry.x < 1000)
                }
                is LineString -> {
                    assertTrue("LineString should be transformed", geometry.points[0].x < 1000)
                }
                is Polygon -> {
                    assertTrue("Polygon should be transformed", geometry.exterior[0].x < 1000)
                }
                is MultiPoint -> {
                    assertTrue("MultiPoint should be transformed", geometry.points[0].x < 1000)
                }
                is MultiLineString -> {
                    assertTrue("MultiLineString should be transformed", geometry.lineStrings[0].points[0].x < 1000)
                }
                is MultiPolygon -> {
                    assertTrue("MultiPolygon should be transformed", geometry.polygons[0].exterior[0].x < 1000)
                }
            }
        }
    }
}
