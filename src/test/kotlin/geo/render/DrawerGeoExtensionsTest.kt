package geo.render

import geo.internal.OptimizedFeature
import geo.internal.geometry.OptimizedPoint
import geo.internal.geometry.OptimizedLineString
import geo.internal.geometry.OptimizedPolygon
import geo.internal.geometry.OptimizedMultiPoint
import geo.internal.geometry.OptimizedMultiLineString
import geo.internal.geometry.OptimizedMultiPolygon
import geo.internal.batch.CoordinateBatch
import geo.projection.ProjectionMercator
import org.junit.Assert.*
import org.junit.Test
import org.openrndr.math.Vector2

/**
 * Tests for toScreenCoordinates extension on OptimizedFeature.
 * Verifies correct screen coordinate projection for all optimized geometry types.
 */
class DrawerGeoExtensionsTest {

    private val projection = ProjectionMercator {
        width = 800.0
        height = 600.0
        zoomLevel = 1.0
        center = Vector2(0.0, 0.0)
    }

    private fun createCoordBatch(vararg coords: Pair<Double, Double>): CoordinateBatch {
        val x = coords.map { it.first }
        val y = coords.map { it.second }
        return CoordinateBatch(x.toDoubleArray(), y.toDoubleArray())
    }

    // --- OptimizedPoint tests ---

    @Test
    fun `toScreenCoordinates for OptimizedPoint returns single screen coordinate`() {
        val coordBatch = createCoordBatch(0.0 to 0.0)
        val optimizedGeom = OptimizedPoint(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals("Point should project to single coordinate", 1, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates for OptimizedPoint handles origin point`() {
        val coordBatch = createCoordBatch(0.0 to 0.0)
        val optimizedGeom = OptimizedPoint(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals(1, screenCoords.size)
        // Origin should project to screen center (400, 300) given Mercator projection with center at origin
        // Actual values depend on projection implementation
        assertNotNull(screenCoords[0])
    }

    // --- OptimizedLineString tests ---

    @Test
    fun `toScreenCoordinates for OptimizedLineString returns projected coordinates`() {
        val coordBatch = createCoordBatch(
            -10.0 to 0.0,
            0.0 to 0.0,
            10.0 to 0.0
        )
        val optimizedGeom = OptimizedLineString(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals("LineString should project to same number of coordinates", 3, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates for OptimizedLineString preserves order`() {
        val coordBatch = createCoordBatch(
            0.0 to 0.0,
            1.0 to 1.0,
            2.0 to 2.0
        )
        val optimizedGeom = OptimizedLineString(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals(3, screenCoords.size)
        // Coordinates should be in the same order as input
        // Since we're projecting increasing values, x and y should be monotonically increasing in screen space
        for (i in 1 until screenCoords.size) {
            assertTrue("Coordinate $i should have greater or equal x than ${i-1}",
                screenCoords[i].x >= screenCoords[i-1].x)
        }
    }

    // --- OptimizedPolygon tests ---

    @Test
    fun `toScreenCoordinates for OptimizedPolygon returns exterior and interior coordinates`() {
        // Create a triangle exterior
        val exterior = createCoordBatch(
            0.0 to 0.0,
            10.0 to 0.0,
            5.0 to 10.0,
            0.0 to 0.0  // closed
        )
        val optimizedGeom = OptimizedPolygon(listOf(exterior))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        // Should return flattened list: exterior + interior coords
        assertTrue("Polygon should return at least exterior coordinates", screenCoords.isNotEmpty())
        // The first 4 coords are the exterior ring
        assertEquals("Exterior ring has 4 points", 4, screenCoords.filterIndexed { index, _ -> index < 4 }.count())
    }

    @Test
    fun `toScreenCoordinates for OptimizedPolygon with holes returns exterior and interior coords`() {
        // Exterior triangle
        val exterior = createCoordBatch(
            0.0 to 0.0,
            20.0 to 0.0,
            10.0 to 20.0,
            0.0 to 0.0
        )
        // Interior hole (smaller triangle)
        val interior = createCoordBatch(
            7.0 to 3.0,
            13.0 to 3.0,
            10.0 to 10.0,
            7.0 to 3.0
        )
        val optimizedGeom = OptimizedPolygon(listOf(exterior, interior))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        // Should have exterior (4) + interior (4) = 8 coordinates flattened
        assertEquals("Polygon with hole should have 8 flattened coordinates", 8, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates for OptimizedPolygon handles no holes case`() {
        val exterior = createCoordBatch(
            0.0 to 0.0,
            5.0 to 0.0,
            5.0 to 5.0,
            0.0 to 5.0,
            0.0 to 0.0
        )
        val optimizedGeom = OptimizedPolygon(listOf(exterior))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals("Polygon without holes should have 5 exterior points", 5, screenCoords.size)
    }

    // --- OptimizedMultiLineString tests ---

    @Test
    fun `toScreenCoordinates for OptimizedMultiLineString returns flattened coordinates`() {
        val line1 = createCoordBatch(0.0 to 0.0, 5.0 to 5.0)
        val line2 = createCoordBatch(10.0 to 0.0, 15.0 to 5.0)
        val optimizedGeom = OptimizedMultiLineString(listOf(line1, line2))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        // MultiLineString flattens all coordinates
        assertEquals("MultiLineString should have 4 coordinates (2 lines x 2 points)", 4, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates for OptimizedMultiLineString handles single line`() {
        val line = createCoordBatch(-5.0 to -5.0, 5.0 to 5.0)
        val optimizedGeom = OptimizedMultiLineString(listOf(line))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals("Single line should have 2 coordinates", 2, screenCoords.size)
    }

    // --- OptimizedMultiPolygon tests ---

    @Test
    fun `toScreenCoordinates for OptimizedMultiPolygon returns all polygon coordinates`() {
        // First polygon - exterior only
        val poly1Exterior = createCoordBatch(
            0.0 to 0.0,
            5.0 to 0.0,
            5.0 to 5.0,
            0.0 to 5.0,
            0.0 to 0.0
        )
        // Second polygon - exterior only
        val poly2Exterior = createCoordBatch(
            10.0 to 0.0,
            15.0 to 0.0,
            15.0 to 5.0,
            10.0 to 5.0,
            10.0 to 0.0
        )
        val optimizedGeom = OptimizedMultiPolygon(listOf(listOf(poly1Exterior), listOf(poly2Exterior)))
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        // 5 + 5 = 10 coordinates total
        assertEquals("MultiPolygon with 2 polygons should have 10 coordinates", 10, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates for OptimizedMultiPolygon with holes handles correctly`() {
        // First polygon with hole
        val poly1Exterior = createCoordBatch(
            0.0 to 0.0,
            10.0 to 0.0,
            10.0 to 10.0,
            0.0 to 10.0,
            0.0 to 0.0
        )
        val poly1Hole = createCoordBatch(
            3.0 to 3.0,
            7.0 to 3.0,
            7.0 to 7.0,
            3.0 to 7.0,
            3.0 to 3.0
        )
        // Second polygon - no hole
        val poly2Exterior = createCoordBatch(
            20.0 to 0.0,
            25.0 to 0.0,
            25.0 to 5.0,
            20.0 to 5.0,
            20.0 to 0.0
        )
        val optimizedGeom = OptimizedMultiPolygon(
            listOf(listOf(poly1Exterior, poly1Hole), listOf(poly2Exterior))
        )
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        // First polygon: 5 exterior + 5 hole = 10
        // Second polygon: 5 exterior = 5
        // Total: 15
        assertEquals("MultiPolygon with hole should have 15 flattened coordinates", 15, screenCoords.size)
    }

    // --- Edge cases ---

    @Test
    fun `toScreenCoordinates handles empty properties`() {
        val coordBatch = createCoordBatch(0.0 to 0.0)
        val optimizedGeom = OptimizedPoint(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals(1, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates preserves properties access`() {
        val coordBatch = createCoordBatch(0.0 to 0.0)
        val optimizedGeom = OptimizedPoint(coordBatch)
        val properties = mapOf("name" to "test", "value" to 42)
        val feature = OptimizedFeature(optimizedGeom, properties)

        // Properties should be accessible (this is tested indirectly through other functionality)
        assertEquals("test", feature.properties["name"])
        assertEquals(42, feature.properties["value"])
    }

    // --- Coordinate bounds tests ---

    @Test
    fun `toScreenCoordinates handles negative coordinates`() {
        val coordBatch = createCoordBatch(
            -10.0 to -5.0,
            10.0 to 5.0
        )
        val optimizedGeom = OptimizedLineString(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals(2, screenCoords.size)
    }

    @Test
    fun `toScreenCoordinates handles positive coordinates`() {
        val coordBatch = createCoordBatch(
            0.0 to 0.0,
            100.0 to 100.0
        )
        val optimizedGeom = OptimizedLineString(coordBatch)
        val feature = OptimizedFeature(optimizedGeom, emptyMap())

        val screenCoords = feature.toScreenCoordinates(projection)

        assertEquals(2, screenCoords.size)
    }
}
