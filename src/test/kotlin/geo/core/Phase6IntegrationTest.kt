package geo.core

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

/**
 * Integration tests for Phase 6 fixes: fitBounds, MultiPolygon clamping, API tiers, and CRS.
 */
class Phase6IntegrationTest {

    // Test data - simple geometries
    private val worldBounds = Bounds(-180.0, -85.0, 180.0, 85.0)
    
    private val ukBounds = Bounds(-8.0, 49.0, 2.0, 61.0)
    
    private val testPoint = Point(0.0, 51.0)  // London
    
    private val testLineString = LineString(listOf(
        Vector2(-1.0, 51.0),
        Vector2(0.0, 51.0),
        Vector2(1.0, 52.0)
    ))
    
    private val testPolygon = Polygon(listOf(
        Vector2(-1.0, 51.0),
        Vector2(1.0, 51.0),
        Vector2(0.0, 52.0)
    ))

    // ========================================================================
    // Test: fitBounds calculation
    // ========================================================================
    
    @Test
    fun testFitBoundsMercator() {
        val projection = geo.projection.ProjectionFactory.fitBounds(
            bounds = ukBounds,
            width = 800.0,
            height = 600.0,
            padding = 0.9,
            projection = geo.projection.ProjectionType.MERCATOR
        )
        
        // Verify projection was created
        assertTrue(projection is geo.projection.ProjectionMercator)
    }
    
    @Test
    fun testFitBoundsEquirectangular() {
        val projection = geo.projection.ProjectionFactory.fitBounds(
            bounds = ukBounds,
            width = 800.0,
            height = 600.0,
            padding = 0.9,
            projection = geo.projection.ProjectionType.EQUIRECTANGULAR
        )
        
        assertTrue(projection is geo.projection.ProjectionEquirectangular)
    }
    
    @Test
    fun testFitBoundsWorldBounds() {
        // Test with whole-world bounds
        val projection = geo.projection.ProjectionFactory.fitBounds(
            bounds = worldBounds,
            width = 800.0,
            height = 600.0,
            padding = 0.9,
            projection = geo.projection.ProjectionType.MERCATOR
        )
        
        assertTrue(projection is geo.projection.ProjectionMercator)
    }

    // ========================================================================
    // Test: MultiPolygon clamping for ocean/whole-world data
    // ========================================================================
    
    @Test
    fun testValidateMercatorBounds() {
        // Valid coordinates
        val validPoint = Point(0.0, 51.0)
        assertTrue(validPoint.validateMercatorBounds())
        
        // Invalid - beyond north pole
        val invalidPoint = Point(0.0, 91.0)
        assertFalse(invalidPoint.validateMercatorBounds())
        
        // Invalid - beyond south pole
        val invalidPoint2 = Point(0.0, -91.0)
        assertFalse(invalidPoint2.validateMercatorBounds())
    }
    
    @Test
    fun testGeometryClampToMercator() {
        // Point beyond pole
        val beyondPole = Point(0.0, 91.0)
        val clamped = beyondPole.clampToMercator() as Point
        
        // Verify clamping
        assertTrue(clamped.y <= 85.0511287798066)
    }
    
    @Test
    fun testNormalizeLongitude() {
        // Test dateline crossing
        val result1 = normalizeLongitude(181.0)
        assertTrue(result1 in -180.0..180.0)
        
        val result2 = normalizeLongitude(-181.0)
        assertTrue(result2 in -180.0..180.0)
        
        val result3 = normalizeLongitude(360.0)
        assertEquals(0.0, result3, 0.001)
    }

    // ========================================================================
    // Test: Three-tier API
    // ========================================================================
    
    @Test
    fun testGeoSourceConvenience() {
        // Create GeoSource from features
        val features = listOf(
            Feature(testPoint, mapOf("name" to "London")),
            Feature(testLineString, mapOf("name" to "Thames"))
        )
        
        val source = geoSourceFromFeatures(features.asSequence())
        
        assertFalse(source.isEmpty())
        assertEquals(2, source.countFeatures())
    }
    
    @Test
    fun testGeoSourceMaterialize() {
        // Create GeoSource from features
        val features = listOf(
            Feature(testPoint, mapOf("name" to "London")),
            Feature(testLineString, mapOf("name" to "Thames"))
        )
        
        val source = geoSourceFromFeatures(features.asSequence())
        val materialized = source.materialize()
        
        // Verify materialized source has same features
        assertFalse(materialized.isEmpty())
    }
    
    @Test
    fun testGeoSourceBoundingBox() {
        val features = listOf(
            Feature(testPoint, emptyMap()),
            Feature(testPolygon, emptyMap())
        )
        
        val source = geoSourceFromFeatures(features.asSequence())
        val bounds = source.totalBoundingBox()
        
        assertTrue(bounds.width > 0)
        assertTrue(bounds.height > 0)
    }

    // ========================================================================
    // Test: CRS enum and transformation
    // ========================================================================
    
    @Test
    fun testCRSEnumParsing() {
        // Test parsing from different formats
        assertEquals(geo.crs.CRS.WGS84, geo.crs.CRS.fromString("EPSG:4326"))
        assertEquals(geo.crs.CRS.WGS84, geo.crs.CRS.fromString("4326"))
        assertEquals(geo.crs.CRS.WGS84, geo.crs.CRS.fromString("WGS84"))
        
        assertEquals(geo.crs.CRS.WebMercator, geo.crs.CRS.fromString("EPSG:3857"))
        assertEquals(geo.crs.CRS.WebMercator, geo.crs.CRS.fromString("3857"))
        assertEquals(geo.crs.CRS.WebMercator, geo.crs.CRS.fromString("WebMercator"))
        
        assertEquals(geo.crs.CRS.BritishNationalGrid, geo.crs.CRS.fromString("EPSG:27700"))
        assertEquals(geo.crs.CRS.BritishNationalGrid, geo.crs.CRS.fromString("27700"))
        assertEquals(geo.crs.CRS.BritishNationalGrid, geo.crs.CRS.fromString("BNG"))
    }
    
    @Test
    fun testCRSFromEPSG() {
        assertEquals(geo.crs.CRS.WGS84, geo.crs.CRS.fromEPSG(4326))
        assertEquals(geo.crs.CRS.WebMercator, geo.crs.CRS.fromEPSG(3857))
        assertEquals(geo.crs.CRS.BritishNationalGrid, geo.crs.CRS.fromEPSG(27700))
    }
    
    @Test
    fun testCRSIsUnknown() {
        assertTrue(geo.crs.CRS.UNKNOWN.isUnknown())
        assertFalse(geo.crs.CRS.WGS84.isUnknown())
        assertFalse(geo.crs.CRS.WebMercator.isUnknown())
    }
    
    @Test
    fun testGeoSourceTransformToCRS() {
        // Create source in WGS84
        val source = geoSourceFromFeatures(
            sequenceOf(Feature(testPoint, emptyMap())),
            crs = geo.crs.CRS.WGS84.code
        )
        
        // Transform to same CRS (identity path)
        val transformed = source.transform(to = geo.crs.CRS.WGS84)
        
        // Should return same CRS
        assertEquals(geo.crs.CRS.WGS84.code, transformed.crs)
    }

    // ========================================================================
    // Test: GeoStack multi-dataset
    // ========================================================================
    
    @Test
    fun testGeoStackCreation() {
        val source1 = geoSourceFromFeatures(sequenceOf(Feature(testPoint, emptyMap())))
        val source2 = geoSourceFromFeatures(sequenceOf(Feature(testLineString, emptyMap())))
        
        val stack = geoStack(source1, source2)
        
        assertEquals(2, stack.sourceCount())
    }
    
    @Test
    fun testGeoStackSingleSource() {
        val source = geoSourceFromFeatures(sequenceOf(Feature(testPoint, emptyMap())))
        
        val stack = geoStack(source)
        
        assertEquals(1, stack.sourceCount())
    }
    
    @Test
    fun testGeoStackBounds() {
        val source1 = geoSourceFromFeatures(sequenceOf(Feature(testPoint, emptyMap())))
        val source2 = geoSourceFromFeatures(sequenceOf(Feature(testPolygon, emptyMap())))
        
        val stack = geoStack(source1, source2)
        val bounds = stack.totalBoundingBox()
        
        assertTrue(bounds.width > 0)
        assertTrue(bounds.height > 0)
    }
    
    @Test
    fun testGeoStackCRS() {
        val source1 = geoSourceFromFeatures(sequenceOf(Feature(testPoint, emptyMap())), "EPSG:4326")
        val source2 = geoSourceFromFeatures(sequenceOf(Feature(testLineString, emptyMap())), "EPSG:4326")
        
        val stack = geoStack(source1, source2)
        
        assertEquals("EPSG:4326", stack.crs)
    }
    
    @Test
    fun testGeoStackCompanion() {
        val source1 = geoSourceFromFeatures(sequenceOf(Feature(testPoint, emptyMap())))
        val source2 = geoSourceFromFeatures(sequenceOf(Feature(testLineString, emptyMap())))
        
        // Test using companion object
        val stack = GeoStack.create(source1, source2)
        
        assertEquals(2, stack.sourceCount())
    }
}
