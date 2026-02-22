package geo.projection

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import geo.exception.CRSTransformationException
import kotlin.math.abs
import kotlin.math.sqrt

class CRSTransformerTest {

    @Test
    fun testLondonBNGToWGS84() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val easting = 530000.0  // London area
        val northing = 180000.0  // London area

        val wgsResult = transformer.transform(easting, northing)

        // Helmert transformation from BNG to WGS84
        // Expected: London area coordinates (approximately -0.13, 51.48)
        // Tolerance: 0.05 degrees (~5.5km at this latitude) to account for Helmert accuracy
        println("London BNG($easting, $northing) → WGS84(${wgsResult.x}, ${wgsResult.y})")
        assertTrue("Longitude should be ≈ -0.13, got ${wgsResult.x}", 
            abs(wgsResult.x + 0.13) < 0.05)
        assertTrue("Latitude should be ≈ 51.48, got ${wgsResult.y}", 
            abs(wgsResult.y - 51.48) < 0.05)
    }

    @Test
    fun testEdinburghBNGToWGS84() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val easting = 326700.0  // Edinburgh area
        val northing = 673400.0  // Edinburgh area

        val wgsResult = transformer.transform(easting, northing)

        // Helmert transformation from BNG to WGS84
        // Expected: Edinburgh area coordinates (approximately -3.18, 55.95)
        // Tolerance: 0.05 degrees (~5.5km at this latitude) to account for Helmert accuracy
        println("Edinburgh BNG($easting, $northing) → WGS84(${wgsResult.x}, ${wgsResult.y})")
        assertTrue("Longitude should be ≈ -3.18, got ${wgsResult.x}", 
            abs(wgsResult.x + 3.18) < 0.05)
        assertTrue("Latitude should be ≈ 55.95, got ${wgsResult.y}", 
            abs(wgsResult.y - 55.95) < 0.05)
    }

    @Test
    fun testBNGRoundTrip() {
        val toWGS = CRSTransformer("EPSG:27700", "EPSG:4326")
        val toBNG = CRSTransformer("EPSG:4326", "EPSG:27700")

        val original = Vector2(530000.0, 180000.0)
        val wgs84 = toWGS.transform(original.x, original.y)
        val backToBNG = toBNG.transform(wgs84.x, wgs84.y)

        // Round-trip within ~5 meters (Helmert transformation accuracy)
        val error = sqrt(
            (original.x - backToBNG.x) * (original.x - backToBNG.x) +
            (original.y - backToBNG.y) * (original.y - backToBNG.y)
        )
        assertTrue("Round-trip error should be < 5 meters, got $error meters", error < 5.0)
    }

    @Test(expected = CRSTransformationException::class)
    fun testInvalidCRSThrows() {
        CRSTransformer("EPSG:99999", "EPSG:4326")
    }

    @Test
    fun testInvalidTargetCRSThrows() {
        try {
            CRSTransformer("EPSG:4326", "EPSG:99999")
            fail("Should have thrown CRSTransformationException")
        } catch (e: CRSTransformationException) {
            assertTrue("Exception message should mention target CRS", 
                e.message?.contains("target CRS") ?: false)
        }
    }

    @Test
    fun testIdentityTransform() {
        val transformer = CRSTransformer("EPSG:4326", "EPSG:4326")
        val lon = -0.1
        val lat = 51.5

        val result = transformer.transform(lon, lat)

        // Identity transformation should return same coordinates (within floating point tolerance)
        assertEquals("Longitude should be unchanged", lon, result.x, 0.0001)
        assertEquals("Latitude should be unchanged", lat, result.y, 0.0001)
    }

    @Test
    fun testBatchPerformance() {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val batchSize = 10000

        val start = System.currentTimeMillis()
        repeat(batchSize) { i ->
            val x = (i % 700000).toDouble()
            val y = ((i * 13) % 1300000).toDouble()
            transformer.transform(x, y)
        }
        val elapsed = System.currentTimeMillis() - start

        println("Transformed $batchSize coordinates in ${elapsed}ms")
        println("Average: ${(batchSize.toDouble() / elapsed) * 1000} coords/sec")

        // Should be < 10 seconds for 10,000 coordinates (fairly generous threshold)
        assertTrue("Performance too slow: ${elapsed}ms for $batchSize coordinates", 
            elapsed < 10000)
        // Actually should be much faster - at least 1000 coords/second
        assertTrue("Should process at least 1000 coordinates/second", 
            (batchSize.toDouble() / elapsed) * 1000 > 1000)
    }

    @Test
    fun testCaseInsensitiveCRS() {
        // Test that CRS codes are case-insensitive (proj4j behavior)
        val lowerCase = CRSTransformer("epsg:27700", "epsg:4326")
        val upperCase = CRSTransformer("EPSG:27700", "EPSG:4326")

        val easting = 530000.0
        val northing = 180000.0

        val lowerResult = lowerCase.transform(easting, northing)
        val upperResult = upperCase.transform(easting, northing)

        assertEquals("Case should not affect result (longitude)", lowerResult.x, upperResult.x, 0.0001)
        assertEquals("Case should not affect result (latitude)", lowerResult.y, upperResult.y, 0.0001)
    }
}
