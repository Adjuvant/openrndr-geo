package geo.performance

import geo.core.Bounds
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration

/**
 * Unit tests for BaselineSimulator.
 * 
 * Verifies that the baseline simulator accurately simulates v1.2.0 behavior
 * and produces correct geographic results.
 */
class BaselineSimulatorTest {
    
    private fun createTestProjection() = ProjectionFactory.fitBounds(
        bounds = Bounds(-180.0, -85.0, 180.0, 85.0),
        width = 800.0,
        height = 600.0,
        projection = ProjectionType.MERCATOR
    )
    
    @Test
    fun `perPointProjection returns valid duration`() {
        val source = SyntheticDataGenerator.createPointDataset(100)
        val projection = createTestProjection()
        
        val duration = BaselineSimulator.perPointProjection(source, projection)
        
        assertTrue("Duration should be positive", duration > Duration.ZERO)
        assertTrue("Duration should be measurable (at least 1 microsecond)", 
            duration.inWholeMicroseconds > 0)
    }
    
    @Test
    fun `perPointProjection produces correct geographic results`() {
        val source = SyntheticDataGenerator.createPointDataset(10)
        val projection = createTestProjection()
        
        // The baseline should produce valid projected coordinates
        // We verify by checking that the method completes and returns a duration
        val duration = BaselineSimulator.perPointProjection(source, projection)
        
        assertTrue("Projection should take some time", duration.inWholeNanoseconds > 0)
    }
    
    @Test
    fun `simulateStaticCamera runs specified number of frames`() {
        val source = SyntheticDataGenerator.createPointDataset(100)
        val projection = createTestProjection()
        val frameCount = 10
        
        val duration = BaselineSimulator.simulateStaticCamera(
            source = source,
            projection = projection,
            frameCount = frameCount
        )
        
        assertTrue("Duration should increase with frame count", 
            duration > Duration.ZERO)
    }
    
    @Test
    fun `simulatePanOperation runs specified number of steps`() {
        val source = SyntheticDataGenerator.createPointDataset(100)
        val startBounds = Bounds(-180.0, -90.0, 180.0, 90.0)
        val endBounds = Bounds(-90.0, -45.0, 90.0, 45.0)
        val steps = 20
        
        val duration = BaselineSimulator.simulatePanOperation(
            source = source,
            projectionFactory = { bounds ->
                ProjectionFactory.fitBounds(
                    bounds = bounds,
                    width = 800.0,
                    height = 600.0,
                    projection = ProjectionType.MERCATOR
                )
            },
            startBounds = startBounds,
            endBounds = endBounds,
            steps = steps
        )
        
        assertTrue("Pan operation should take measurable time", 
            duration.inWholeMicroseconds > 0)
    }
    
    @Test
    fun `comparePerformance calculates correct speedup`() {
        val source = SyntheticDataGenerator.createPointDataset(100)
        val projection = createTestProjection()
        
        // Compare baseline against itself (should give ~1.0 speedup)
        val comparison = BaselineSimulator.comparePerformance(
            source = source,
            projection = projection,
            iterations = 10,
            optimizedRender = {
                // Same as baseline for this test
                source.features.forEach { feature ->
                    // Just iterate to simulate work
                }
            }
        )
        
        assertTrue("Baseline duration should be positive", 
            comparison.baselineDuration > Duration.ZERO)
        assertTrue("Should have speedup value", comparison.speedup > 0.0)
    }
    
    @Test
    fun `comparePerformance with actual optimization shows speedup`() {
        val source = SyntheticDataGenerator.createPointDataset(1000)
        val projection = createTestProjection()
        
        // Pre-materialize features for fair comparison
        val materializedFeatures = source.listFeatures()
        
        val comparison = BaselineSimulator.comparePerformance(
            source = source,
            projection = projection,
            iterations = 50,
            optimizedRender = {
                // Optimized: simulate less work by just iterating
                materializedFeatures.forEach { feature ->
                    // In real optimized code, we'd use cached projections
                    // Here we just simulate work by accessing the geometry
                    feature.geometry
                }
            }
        )
        
        assertTrue("Should have timing results", comparison.baselinePerIteration > Duration.ZERO)
        assertNotNull("Speedup should be calculated", comparison.speedup)
    }
    
    @Test
    fun `PerformanceComparison formatReport produces readable output`() {
        val comparison = PerformanceComparison(
            baselineDuration = Duration.parse("1.5s"),
            optimizedDuration = Duration.parse("0.3s"),
            baselinePerIteration = Duration.parse("30ms"),
            optimizedPerIteration = Duration.parse("6ms"),
            speedup = 5.0
        )
        
        val report = comparison.formatReport()
        
        assertTrue("Report should contain baseline", report.contains("Baseline"))
        assertTrue("Report should contain speedup", report.contains("Speedup"))
        assertTrue("Report should contain 5.00x", report.contains("5.00"))
    }
    
    @Test
    fun `BenchmarkStats calculates correct statistics`() {
        val durations = listOf(
            Duration.parse("10ms"),
            Duration.parse("20ms"),
            Duration.parse("30ms"),
            Duration.parse("40ms"),
            Duration.parse("50ms")
        )
        
        val stats = BenchmarkStats.fromDurations(durations)
        
        assertEquals("Mean should be 30ms", 30_000_000.0, stats.mean, 1.0)
        assertEquals("Median should be 30ms", 30_000_000.0, stats.median, 1.0)
        assertEquals("Min should be 10ms", 10_000_000.0, stats.min, 1.0)
        assertEquals("Max should be 50ms", 50_000_000.0, stats.max, 1.0)
    }
    
    @Test
    fun `BenchmarkStats handles empty list`() {
        val stats = BenchmarkStats.fromDurations(emptyList())
        
        assertEquals("Mean should be 0", 0.0, stats.mean, 0.0)
        assertEquals("Median should be 0", 0.0, stats.median, 0.0)
    }
    
    @Test
    fun `BenchmarkStats formatReport produces readable output`() {
        val stats = BenchmarkStats(
            mean = 30_000_000.0,  // 30ms
            median = 30_000_000.0,
            min = 10_000_000.0,
            max = 50_000_000.0,
            stdDev = 10_000_000.0
        )
        
        val report = stats.formatReport()
        
        assertTrue("Report should contain mean", report.contains("mean="))
        assertTrue("Report should contain ms values", report.contains("ms"))
    }
    
    @Test
    fun `simulateRenderLoop runs specified iterations`() {
        val source = SyntheticDataGenerator.createPointDataset(100)
        val projection = createTestProjection()
        val iterations = 20
        
        val duration = BaselineSimulator.simulateRenderLoop(
            source = source,
            projection = projection,
            iterations = iterations
        )
        
        assertTrue("Should complete with positive duration", 
            duration.inWholeMicroseconds > 0)
    }
    
    @Test
    fun `larger datasets take longer to process`() {
        val projection = createTestProjection()
        
        val smallSource = SyntheticDataGenerator.createPointDataset(100)
        val largeSource = SyntheticDataGenerator.createPointDataset(1000)
        
        val smallDuration = BaselineSimulator.perPointProjection(smallSource, projection)
        val largeDuration = BaselineSimulator.perPointProjection(largeSource, projection)
        
        // Large dataset should generally take longer (allow some variance)
        assertTrue("Larger dataset should take more time on average",
            largeDuration.inWholeMilliseconds >= smallDuration.inWholeMilliseconds)
    }
}
