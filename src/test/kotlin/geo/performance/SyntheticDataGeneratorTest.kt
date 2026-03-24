package geo.performance

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SyntheticDataGenerator.
 * 
 * Verifies that synthetic datasets are generated with correct feature counts
 * and valid geographic properties.
 */
class SyntheticDataGeneratorTest {
    
    @Test
    fun `createPointDataset generates correct feature count`() {
        val testSizes = listOf(10_000, 50_000, 100_000, 250_000)
        
        testSizes.forEach { count ->
            val source = SyntheticDataGenerator.createPointDataset(count)
            val actualCount = source.countFeatures()
            
            assertEquals(
                "Point dataset should have exactly $count features",
                count.toLong(),
                actualCount
            )
        }
    }
    
    @Test
    fun `createLineStringDataset generates correct feature count`() {
        val testSizes = listOf(10_000, 50_000, 100_000, 250_000)
        
        testSizes.forEach { count ->
            val source = SyntheticDataGenerator.createLineStringDataset(count, pointsPerLine = 10)
            val actualCount = source.countFeatures()
            
            assertEquals(
                "LineString dataset should have exactly $count features",
                count.toLong(),
                actualCount
            )
        }
    }
    
    @Test
    fun `createPolygonDataset generates correct feature count`() {
        val testSizes = listOf(10_000, 50_000, 100_000, 250_000)
        
        testSizes.forEach { count ->
            val source = SyntheticDataGenerator.createPolygonDataset(count, pointsPerPolygon = 20)
            val actualCount = source.countFeatures()
            
            assertEquals(
                "Polygon dataset should have exactly $count features",
                count.toLong(),
                actualCount
            )
        }
    }
    
    @Test
    fun `createPointDataset generates points within world bounds`() {
        val source = SyntheticDataGenerator.createPointDataset(1_000)
        val bounds = SyntheticDataGenerator.calculateBounds(source)
        
        assertTrue("Min X should be >= -180", bounds.minX >= -180.0)
        assertTrue("Max X should be <= 180", bounds.maxX <= 180.0)
        assertTrue("Min Y should be >= -90", bounds.minY >= -90.0)
        assertTrue("Max Y should be <= 90", bounds.maxY <= 90.0)
    }
    
    @Test
    fun `createLineStringDataset generates lines with correct point count`() {
        val pointsPerLine = 15
        val source = SyntheticDataGenerator.createLineStringDataset(100, pointsPerLine = pointsPerLine)
        
        source.features.take(10).forEach { feature ->
            val lineString = feature.geometry as geo.core.LineString
            assertEquals(
                "Each LineString should have $pointsPerLine points",
                pointsPerLine,
                lineString.points.size
            )
        }
    }
    
    @Test
    fun `createPolygonDataset generates polygons with correct vertex count`() {
        val pointsPerPolygon = 25
        val source = SyntheticDataGenerator.createPolygonDataset(100, pointsPerPolygon = pointsPerPolygon)
        
        source.features.take(10).forEach { feature ->
            val polygon = feature.geometry as geo.core.Polygon
            assertEquals(
                "Each Polygon should have $pointsPerPolygon exterior points",
                pointsPerPolygon,
                polygon.exterior.size
            )
        }
    }
    
    @Test
    fun `reproducibility - same seed produces same data`() {
        val count = 1000
        val seed = 12345L
        
        val source1 = SyntheticDataGenerator.createPointDataset(count, seed = seed)
        val source2 = SyntheticDataGenerator.createPointDataset(count, seed = seed)
        
        val points1 = source1.features.map { it.geometry as geo.core.Point }.toList()
        val points2 = source2.features.map { it.geometry as geo.core.Point }.toList()
        
        assertEquals("Same seed should produce same number of features", points1.size, points2.size)
        
        points1.zip(points2).forEach { (p1, p2) ->
            assertEquals("X coordinates should match", p1.x, p2.x, 0.0001)
            assertEquals("Y coordinates should match", p1.y, p2.y, 0.0001)
        }
    }
    
    @Test
    fun `different seeds produce different data`() {
        val count = 100
        
        val source1 = SyntheticDataGenerator.createPointDataset(count, seed = 1L)
        val source2 = SyntheticDataGenerator.createPointDataset(count, seed = 2L)
        
        val points1 = source1.features.map { it.geometry as geo.core.Point }.toList()
        val points2 = source2.features.map { it.geometry as geo.core.Point }.toList()
        
        // At least some points should be different
        val allSame = points1.zip(points2).all { (p1, p2) ->
            kotlin.math.abs(p1.x - p2.x) < 0.0001 && kotlin.math.abs(p1.y - p2.y) < 0.0001
        }
        
        assertFalse("Different seeds should produce different data", allSame)
    }
    
    @Test
    fun `createMixedDataset generates all geometry types`() {
        val count = 1000
        val source = SyntheticDataGenerator.createMixedDataset(count)
        
        val featureList = source.listFeatures()
        val geometryTypes = featureList.map { it.geometry::class.simpleName }.distinct()
        
        assertTrue("Should have Point features", geometryTypes.contains("Point"))
        assertTrue("Should have LineString features", geometryTypes.contains("LineString"))
        assertTrue("Should have Polygon features", geometryTypes.contains("Polygon"))
        
        assertEquals("Total feature count should match", count.toLong(), featureList.size.toLong())
    }
    
    @Test
    fun `coordinate count calculation is correct`() {
        val pointCount = 100
        val source = SyntheticDataGenerator.createPointDataset(pointCount)
        
        val coordCount = SyntheticDataGenerator.countCoordinates(source)
        
        assertEquals(
            "Point dataset should have 1 coordinate per feature",
            pointCount.toLong(),
            coordCount
        )
    }
    
    @Test
    fun `LineString coordinate count calculation`() {
        val featureCount = 10
        val pointsPerLine = 20
        val source = SyntheticDataGenerator.createLineStringDataset(featureCount, pointsPerLine = pointsPerLine)
        
        val coordCount = SyntheticDataGenerator.countCoordinates(source)
        val expected = featureCount * pointsPerLine
        
        assertEquals(
            "LineString dataset should have features * pointsPerLine coordinates",
            expected.toLong(),
            coordCount
        )
    }
}
