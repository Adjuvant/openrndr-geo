@file:JvmName("BatchOptimization")
package examples.core

import geo.geoSource
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.Style
import geo.render.geo
import org.openrndr.application
import org.openrndr.color.ColorRGBa

/**
 * ## 05 - Batch Projection Optimization
 *
 * Demonstrates the batch projection optimization feature for improved rendering performance.
 *
 * ### Concepts
 * - Loading data with `optimize=true` for batch projection
 * - Side-by-side comparison of standard vs optimized rendering
 * - Visual proof that optimized sources render identically
 * - Using styleByType to color different geometry types
 *
 * ### Key Benefits
 * - Reduced GC pressure from fewer object allocations
 * - Better cache locality with DoubleArray storage
 * - Measurable performance improvements on large datasets
 * - 100% backward compatible - opt-in optimization
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

        // 2. Optimized loading (new feature) - RIGHT SIDE
        val optimizedSource = geoSource("examples/data/geo/catchment-topo.geojson", optimize = true)
        println("✓ Optimized source loaded: ${optimizedSource.countFeatures()} features")
        println("  Bounds: ${optimizedSource.boundingBox()}")

        // Create projections for left and right halves of the screen
        val leftProjection = ProjectionFactory.fitBounds(
            bounds = standardSource.boundingBox(),
            width = width / 2.0,
            height = height.toDouble(),
            padding = 50.0,
            projection = ProjectionType.MERCATOR
        )

        val rightProjection = ProjectionFactory.fitBounds(
            bounds = optimizedSource.boundingBox(),
            width = width / 2.0,
            height = height.toDouble(),
            padding = 50.0,
            projection = ProjectionType.MERCATOR
        )

        // Define styles - Standard in BLUE, Optimized in RED
        val standardStyle = Style { stroke = ColorRGBa.BLUE; strokeWeight = 1.5 }
        val optimizedStyle = Style { stroke = ColorRGBa.RED; strokeWeight = 1.5 }

        extend {
            drawer.clear(ColorRGBa.BLACK)

            // LEFT SIDE: Standard source in BLUE
            drawer.geo(standardSource) {
                projection = leftProjection
                style = standardStyle
            }

            // RIGHT SIDE: Optimized source in RED
            // Shift to right half of screen by translating the drawer
            val t = frameCount % 60.0 / 60.0
            drawer.translate((width / 2.0) * t, 0.0)
            drawer.geo(optimizedSource) {
                projection = rightProjection
                style = optimizedStyle
            }
            // Reset translation
            drawer.translate(-width / 2.0, 0.0)

            // Draw dividing line between the two views
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0
            drawer.lineSegment(width / 2.0, 0.0, width / 2.0, height.toDouble())
        }
    }
}
