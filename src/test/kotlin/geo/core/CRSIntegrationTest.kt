package geo.core

import geo.projection.toWGS84
import geo.projection.toWebMercator
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Integration tests for CRS transformation with real GeoPackage data.
 * Verifies end-to-end workflow: load → transform → verify.
 */
class CRSIntegrationTest {

    private val gpkgPath = "data/geo/ness-vectors.gpkg"

    @Test
    fun testGeoPackageBNGToWGS84() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load GeoPackage with BNG data (EPSG:27700)
        val source = GeoPackage.load(gpkgPath)

        // Verify CRS detection works (should be BNG for ness-vectors.gpkg)
        val originalCRS = source.crs
        assertNotNull("CRS should be detected", originalCRS)

        // Transform to WGS84
        val wgs84Source = source.autoTransformTo("EPSG:4326")

        // Verify CRS changed
        assertEquals("EPSG:4326", wgs84Source.crs)
        assertNotEquals("Source and transformed should have different CRS", source.crs, wgs84Source.crs)

        // Get some features and verify coordinates are in valid WGS84 range
        val features = wgs84Source.listFeatures().take(5)
        assertTrue("Should have features", features.isNotEmpty())

        features.forEach { feature ->
            when (val geom = feature.geometry) {
                is Point -> {
                    assertTrue("Longitude should be in valid range", geom.x > -180 && geom.x < 180)
                    assertTrue("Latitude should be in valid range", geom.y > -90 && geom.y < 90)
                }
                is LineString -> {
                    geom.points.forEach { pt ->
                        assertTrue("Longitude should be in valid range", pt.x > -180 && pt.x < 180)
                        assertTrue("Latitude should be in valid range", pt.y > -90 && pt.y < 90)
                    }
                }
                is Polygon -> {
                    geom.exterior.forEach { pt ->
                        assertTrue("Longitude should be in valid range", pt.x > -180 && pt.x < 180)
                        assertTrue("Latitude should be in valid range", pt.y > -90 && pt.y < 90)
                    }
                }
                else -> {
                    // For other types, just verify they exist
                    assertNotNull(geom)
                }
            }
        }
    }

    @Test
    fun testMaterializeBehavior() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load and transform to WGS84
        val source = GeoPackage.load(gpkgPath).autoTransformTo("EPSG:4326")
        val originalCRS = source.crs
        val originalCount = source.countFeatures()

        // Materialize the features
        val materializedSource = source.materialize()

        // Verify CRS is preserved
        assertEquals("CRS should be preserved after materialization", originalCRS, materializedSource.crs)

        // Verify feature count is preserved
        assertEquals("Feature count should be preserved", originalCount, materializedSource.countFeatures())

        // Verify features are accessible (materialized means eager evaluation)
        val features1 = materializedSource.listFeatures()
        val features2 = materializedSource.listFeatures()
        assertEquals("Should return same features on multiple accesses", features1.size, features2.size)
        assertTrue("Should have features", features1.isNotEmpty())
    }

    @Test
    fun testIdentityOptimization() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load GeoPackage
        val source = GeoPackage.load(gpkgPath)

        // Transform to WGS84 (if not already)
        val wgs84Source = source.autoTransformTo("EPSG:4326")

        // Transform WGS84 to WGS84 (should return same instance - identity optimization)
        val sameSource = wgs84Source.autoTransformTo("EPSG:4326")

        // With identity optimization, this should be the same object
        assertSame("Identity optimization should return same instance", wgs84Source, sameSource)
    }

    @Test
    fun testToWGS84Extension() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load GeoPackage
        val source = GeoPackage.load(gpkgPath)

        // Use convenience extension to transform to WGS84
        val wgs84Source = source.toWGS84()

        // Verify CRS is now WGS84
        assertEquals("EPSG:4326", wgs84Source.crs)

        // Verify features are accessible
        val features = wgs84Source.listFeatures().take(5)
        assertTrue("Should have features after toWGS84()", features.isNotEmpty())

        // Verify coordinates are in valid WGS84 range
        features.forEach { feature ->
            when (val geom = feature.geometry) {
                is Point -> {
                    assertTrue("Longitude should be valid", geom.x > -180 && geom.x < 180)
                    assertTrue("Latitude should be valid", geom.y > -90 && geom.y < 90)
                }
                is LineString -> {
                    geom.points.forEach { pt ->
                        assertTrue("Longitude should be valid", pt.x > -180 && pt.x < 180)
                        assertTrue("Latitude should be valid", pt.y > -90 && pt.y < 90)
                    }
                }
                else -> { /* Other geometry types */ }
            }
        }
    }

    @Test
    fun testToWebMercatorExtension() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load GeoPackage
        val source = GeoPackage.load(gpkgPath)

        // Use convenience extension to transform to Web Mercator
        val webMercatorSource = source.toWebMercator()

        // Verify CRS is now Web Mercator
        assertEquals("EPSG:3857", webMercatorSource.crs)

        // Verify features are accessible
        val features = webMercatorSource.listFeatures().take(5)
        assertTrue("Should have features after toWebMercator()", features.isNotEmpty())

        // Web Mercator coordinates are in meters, roughly:
        // X range: -20037508 to 20037508
        // Y range: -20037508 to 20037508
        features.forEach { feature ->
            when (val geom = feature.geometry) {
                is Point -> {
                    assertTrue("X should be in Web Mercator range", geom.x > -21000000 && geom.x < 21000000)
                    assertTrue("Y should be in Web Mercator range", geom.y > -21000000 && geom.y < 21000000)
                }
                is LineString -> {
                    geom.points.forEach { pt ->
                        assertTrue("X should be in Web Mercator range", pt.x > -21000000 && pt.x < 21000000)
                        assertTrue("Y should be in Web Mercator range", pt.y > -21000000 && pt.y < 21000000)
                    }
                }
                else -> { /* Other geometry types */ }
            }
        }
    }

    @Test
    fun testChainedTransformation() {
        val file = File(gpkgPath)
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Test the full chain: load → toWGS84 → materialize
        val materializedWGS84 = GeoPackage.load(gpkgPath)
            .toWGS84()
            .materialize()

        // Verify final state
        assertEquals("EPSG:4326", materializedWGS84.crs)
        assertTrue("Should have features", materializedWGS84.countFeatures() > 0)

        // Verify features are in WGS84 range
        val sampleFeatures = materializedWGS84.listFeatures().take(3)
        sampleFeatures.forEach { feature ->
            when (val geom = feature.geometry) {
                is Point -> {
                    assertTrue("Should be in WGS84 range", geom.x > -180 && geom.x < 180)
                    assertTrue("Should be in WGS84 range", geom.y > -90 && geom.y < 90)
                }
                else -> { /* Other types */ }
            }
        }
    }
}
