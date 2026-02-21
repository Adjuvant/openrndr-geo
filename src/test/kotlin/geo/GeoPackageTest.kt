package geo

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileNotFoundException

class GeoPackageTest {

    @Test
    fun `convenience features should return same features as load`() {
        val gpkgPath = "data/geo/ness-vectors.gpkg"
        val file = File(gpkgPath)
        
        // Skip test if GeoPackage file doesn't exist
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        val source = GeoPackage.load(gpkgPath)
        val sourceFeatures = source.listFeatures()
        
        val convenienceFeatures = GeoPackage.features(gpkgPath).toList()

        assertEquals(sourceFeatures.size.toInt(), convenienceFeatures.size)
        assertFalse(convenienceFeatures.isEmpty())
    }

    @Test
    fun `convenience features with maxFeatures should limit feature count`() {
        val gpkgPath = "data/geo/ness-vectors.gpkg"
        val file = File(gpkgPath)
        
        // Skip test if GeoPackage file doesn't exist
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Load limited features using convenience function
        val limitedFeatures = GeoPackage.features(gpkgPath, maxFeatures = 5).toList()

        assertTrue("Should have at most 5 features", limitedFeatures.size <= 5)
    }

    @Test
    fun `convenience features should allow normal iteration`() {
        val gpkgPath = "data/geo/ness-vectors.gpkg"
        val file = File(gpkgPath)
        
        // Skip test if GeoPackage file doesn't exist
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        val features = GeoPackage.features(gpkgPath).toList()
        
        // Verify we can iterate and access properties
        for (feature in features.take(5)) {
            assertNotNull(feature.geometry)
            // Geometry should have a valid bounding box
            val bounds = feature.geometry.boundingBox
            assertNotNull(bounds)
        }
    }

    @Test(expected = FileNotFoundException::class)
    fun `convenience features should throw FileNotFoundException for missing file`() {
        GeoPackage.features("/nonexistent/path/file.gpkg").toList()
    }

    @Test
    fun `convenience features should return consistent results across multiple calls`() {
        val gpkgPath = "data/geo/ness-vectors.gpkg"
        val file = File(gpkgPath)
        
        // Skip test if GeoPackage file doesn't exist
        if (!file.exists()) {
            println("Skipping test: GeoPackage file not found at $gpkgPath")
            return
        }

        // Call convenience function twice
        val features1 = GeoPackage.features(gpkgPath).toList()
        val features2 = GeoPackage.features(gpkgPath).toList()

        assertEquals(features1.size, features2.size)
    }
}
