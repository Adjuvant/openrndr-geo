package geo.performance

import geo.*
import org.openrndr.math.Vector2
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Simulates v1.2.0 rendering behavior (before optimization) for performance comparison.
 * 
 * This simulator mimics the old code path:
 * - Per-point coordinate transformation (no batch optimization)
 * - No viewport caching (re-project every frame)
 * - Render loop transforms coordinates on every call
 * 
 * Used to measure speedup achieved by Phase 11 (batch projection) and 
 * Phase 12 (viewport caching) optimizations.
 */
object BaselineSimulator {
    
    /**
     * Simulates v1.2.0 per-point projection without any optimization.
     * 
     * This method mimics the original rendering pipeline:
     * 1. Iterate through all features in the source
     * 2. For each feature, project coordinates individually (per-point)
     * 3. No caching - projections are recalculated on every call
     * 
     * @param source The GeoSource containing features to render
     * @param projection The geographic projection to use
     * @return Duration of the simulated render operation
     */
    fun perPointProjection(source: GeoSource, projection: geo.projection.GeoProjection): Duration {
        return measureTime {
            source.features.forEach { feature ->
                projectGeometryPerPoint(feature.geometry, projection)
            }
        }
    }
    
    /**
     * Simulates rendering with per-point projection for multiple iterations.
     * 
     * Useful for measuring average performance over multiple frames,
     * simulating a typical render loop.
     * 
     * @param source The GeoSource containing features to render
     * @param projection The geographic projection to use
     * @param iterations Number of render iterations to simulate
     * @return Total duration for all iterations
     */
    fun simulateRenderLoop(
        source: GeoSource,
        projection: geo.projection.GeoProjection,
        iterations: Int
    ): Duration {
        return measureTime {
            repeat(iterations) {
                source.features.forEach { feature ->
                    projectGeometryPerPoint(feature.geometry, projection)
                }
            }
        }
    }
    
    /**
     * Simulates a pan operation with per-point projection.
     * 
     * In v1.2.0, panning required re-projection of all coordinates
     * since there was no caching of projected coordinates.
     * 
     * @param source The GeoSource containing features
     * @param projectionFactory Function to create projection for given bounds
     * @param startBounds Initial viewport bounds
     * @param endBounds Final viewport bounds after pan
     * @param steps Number of intermediate steps in the pan animation
     * @return Total duration for the pan operation
     */
    fun simulatePanOperation(
        source: GeoSource,
        projectionFactory: (Bounds) -> geo.projection.GeoProjection,
        startBounds: Bounds,
        endBounds: Bounds,
        steps: Int = 30
    ): Duration {
        return measureTime {
            // Simulate pan animation with intermediate frames
            for (step in 0..steps) {
                val t = step.toDouble() / steps.toDouble()
                val currentBounds = interpolateBounds(startBounds, endBounds, t)
                val projection = projectionFactory(currentBounds)
                
                // Re-project all features (no cache in v1.2.0)
                source.features.forEach { feature ->
                    projectGeometryPerPoint(feature.geometry, projection)
                }
            }
        }
    }
    
    /**
     * Simulates a static camera view (no movement) with per-point projection.
     * 
     * This measures the cost of re-projecting everything every frame
     * even when the viewport hasn't changed (the main optimization target).
     * 
     * @param source The GeoSource containing features
     * @param projection The geographic projection to use
     * @param frameCount Number of frames to simulate
     * @return Total duration for all frames
     */
    fun simulateStaticCamera(
        source: GeoSource,
        projection: geo.projection.GeoProjection,
        frameCount: Int
    ): Duration {
        return measureTime {
            repeat(frameCount) {
                // Even with static camera, v1.2.0 re-projects every frame
                source.features.forEach { feature ->
                    projectGeometryPerPoint(feature.geometry, projection)
                }
            }
        }
    }
    
    /**
     * Compares baseline (v1.2.0) performance against an optimized render function.
     * 
     * @param source The GeoSource to benchmark
     * @param projection The geographic projection to use
     * @param optimizedRender Function that performs the optimized rendering
     * @return Comparison result with timing and speedup ratio
     */
    fun comparePerformance(
        source: GeoSource,
        projection: geo.projection.GeoProjection,
        iterations: Int = 50,
        optimizedRender: () -> Unit
    ): PerformanceComparison {
        // Warmup
        repeat(10) {
            perPointProjection(source, projection)
            optimizedRender()
        }
        
        // Measure baseline (v1.2.0)
        val baselineTime = measureTime {
            repeat(iterations) {
                source.features.forEach { feature ->
                    projectGeometryPerPoint(feature.geometry, projection)
                }
            }
        }
        
        // Measure optimized
        val optimizedTime = measureTime {
            repeat(iterations) {
                optimizedRender()
            }
        }
        
        return PerformanceComparison(
            baselineDuration = baselineTime,
            optimizedDuration = optimizedTime,
            baselinePerIteration = baselineTime / iterations,
            optimizedPerIteration = optimizedTime / iterations,
            speedup = baselineTime.inWholeNanoseconds.toDouble() / optimizedTime.inWholeNanoseconds.toDouble()
        )
    }
    
    // ============================================================================
    // Internal projection helpers (v1.2.0 style)
    // ============================================================================
    
    /**
     * Projects a geometry using true per-point projection (no batching).
     * 
     * This mimics the original implementation where each coordinate
     * is transformed individually in a loop.
     */
    private fun projectGeometryPerPoint(
        geometry: Geometry,
        projection: geo.projection.GeoProjection
    ): List<Vector2> {
        return when (geometry) {
            is Point -> {
                // Per-point: single coordinate transformation
                listOf(projection.project(Vector2(geometry.x, geometry.y)))
            }
            is LineString -> {
                // Per-point: map each point individually
                geometry.points.map { pt -> projection.project(pt) }
            }
            is Polygon -> {
                // Per-point: map each exterior point individually
                geometry.exterior.map { pt -> projection.project(pt) }
            }
            is MultiPoint -> {
                // Per-point: nested iteration with individual projections
                geometry.points.map { pt -> projection.project(Vector2(pt.x, pt.y)) }
            }
            is MultiLineString -> {
                // Per-point: nested loops with individual projections
                geometry.lineStrings.flatMap { line ->
                    line.points.map { pt -> projection.project(pt) }
                }
            }
            is MultiPolygon -> {
                // Per-point: nested loops with individual projections
                geometry.polygons.flatMap { poly ->
                    poly.exterior.map { pt -> projection.project(pt) }
                }
            }
        }
    }
    
    /**
     * Interpolates between two bounding boxes for pan simulation.
     */
    private fun interpolateBounds(start: Bounds, end: Bounds, t: Double): Bounds {
        return Bounds(
            minX = start.minX + (end.minX - start.minX) * t,
            minY = start.minY + (end.minY - start.minY) * t,
            maxX = start.maxX + (end.maxX - start.maxX) * t,
            maxY = start.maxY + (end.maxY - start.maxY) * t
        )
    }
}

/**
 * Result of a performance comparison between baseline and optimized rendering.
 * 
 * @property baselineDuration Total time for baseline (v1.2.0) rendering
 * @property optimizedDuration Total time for optimized rendering
 * @property baselinePerIteration Average time per iteration (baseline)
 * @property optimizedPerIteration Average time per iteration (optimized)
 * @property speedup Speedup ratio (baseline / optimized), higher is better
 */
data class PerformanceComparison(
    val baselineDuration: Duration,
    val optimizedDuration: Duration,
    val baselinePerIteration: Duration,
    val optimizedPerIteration: Duration,
    val speedup: Double
) {
    /**
     * Formats the comparison as a readable string.
     */
    fun formatReport(): String {
        return buildString {
            appendLine("Performance Comparison Results:")
            appendLine("  Baseline (v1.2.0): ${formatDuration(baselineDuration)} total")
            appendLine("  Optimized:         ${formatDuration(optimizedDuration)} total")
            appendLine("  Baseline/iter:     ${formatDuration(baselinePerIteration)}")
            appendLine("  Optimized/iter:    ${formatDuration(optimizedPerIteration)}")
            appendLine("  Speedup:           ${String.format("%.2f", speedup)}x")
            appendLine("  Status:            ${if (speedup >= 1.0) "✓ FASTER" else "✗ SLOWER (unexpected)"}")
        }
    }
    
    private fun formatDuration(duration: Duration): String {
        return when {
            duration.inWholeSeconds > 0 -> "${duration.inWholeSeconds}s ${duration.inWholeMilliseconds % 1000}ms"
            duration.inWholeMilliseconds > 0 -> "${duration.inWholeMilliseconds}ms"
            else -> "${duration.inWholeMicroseconds}µs"
        }
    }
}

/**
 * Statistics calculated from multiple benchmark runs.
 * 
 * @property mean Average value
 * @property median Middle value
 * @property min Minimum value
 * @property max Maximum value
 * @property stdDev Standard deviation
 */
data class BenchmarkStats(
    val mean: Double,
    val median: Double,
    val min: Double,
    val max: Double,
    val stdDev: Double
) {
    companion object {
        /**
         * Calculates statistics from a list of duration measurements.
         */
        fun fromDurations(durations: List<Duration>): BenchmarkStats {
            val values = durations.map { it.inWholeNanoseconds.toDouble() }
            return fromValues(values)
        }
        
        /**
         * Calculates statistics from a list of double values (in nanoseconds).
         */
        fun fromValues(values: List<Double>): BenchmarkStats {
            if (values.isEmpty()) {
                return BenchmarkStats(0.0, 0.0, 0.0, 0.0, 0.0)
            }
            
            val sorted = values.sorted()
            val mean = values.average()
            val median = if (sorted.size % 2 == 0) {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            } else {
                sorted[sorted.size / 2]
            }
            val min = sorted.first()
            val max = sorted.last()
            
            // Calculate standard deviation
            val variance = values.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            
            return BenchmarkStats(mean, median, min, max, stdDev)
        }
    }
    
    /**
     * Formats the statistics as a readable string.
     */
    fun formatReport(): String {
        return "mean=${formatNanos(mean)}, median=${formatNanos(median)}, min=${formatNanos(min)}, max=${formatNanos(max)}, stdDev=${formatNanos(stdDev)}"
    }
    
    private fun formatNanos(nanos: Double): String {
        return when {
            nanos >= 1_000_000_000 -> String.format("%.2fs", nanos / 1_000_000_000)
            nanos >= 1_000_000 -> String.format("%.2fms", nanos / 1_000_000)
            nanos >= 1_000 -> String.format("%.2fµs", nanos / 1_000)
            else -> String.format("%.0fns", nanos)
        }
    }
}
