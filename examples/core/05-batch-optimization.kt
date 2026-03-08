@file:JvmName("BatchOptimization")
package examples.core

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.*
import geo.render.*

/**
 * ## 05 - Batch Projection Optimization
 *
 * Demonstrates the batch projection optimization feature for improved rendering performance.
 *
 * ### Concepts
 * - Loading data with optimize=true for batch projection
 * - Using loadGeo(optimize=true) for automatic optimization
 * - Visual proof that optimized sources render identically
 * - Three-line workflow with optimized data
 *
 * ### Key Benefits
 * - Reduced GC pressure from fewer object allocations
 * - Better cache locality with DoubleArray storage
 * - Measurable performance improvements on large datasets
 * - Automatic viewport caching
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.core.BatchOptimizationKt
 * ```
 */
fun main() = application {
    configure {
        width = 1200
        height = 600
    }

    program {
        // Load the same dataset two ways for comparison
        println("=== Batch Projection Optimization Demo ===\n")

        // 1. Standard loading (backward compatible) - LEFT SIDE
        val standardSource = geoSource("examples/data/geo/catchment-topo.geojson")
        println("✓ Standard source loaded: ${standardSource.countFeatures()} features")

        // 2. Optimized loading using loadGeo with caching
        val optimizedData = loadGeo("examples/data/geo/catchment-topo.geojson")
        println("✓ Optimized source loaded: ${optimizedData.countFeatures()} features")
        println("  Bounds: ${optimizedData.boundingBox()}")

        // Create projections for left and right halves
        val leftProjection = standardSource.projectToFit(width / 2, height)
        val rightProjection = optimizedData.projectToFit(width / 2, height)

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.text("${frameCount} :: ${(frameCount/seconds).toInt()} FPS", 20.0, 20.0)
            
            // LEFT SIDE: Standard source in BLUE
            drawer.geo(standardSource) {
                projection = leftProjection
                stroke = ColorRGBa.BLUE
                strokeWeight = 1.5
                fill = null
            }

            // RIGHT SIDE: Optimized source in RED
            drawer.translate((width / 2.0), 0.0)
            drawer.geo(optimizedData) {
                projection = rightProjection
                stroke = ColorRGBa.RED
                strokeWeight = 1.5
                fill = null
            }
            
            // Reset translation
            drawer.translate(-width / 2.0, 0.0)

            // Draw dividing line
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.lineSegment(width / 2.0, 0.0, width / 2.0, height.toDouble())
            
            // Labels
            drawer.fill = ColorRGBa.WHITE
            drawer.text("Standard (geoSource)", 20.0, 550.0)
            drawer.text("Cached (loadGeo)", width / 2.0 + 20.0, 550.0)
        }
    }
}
