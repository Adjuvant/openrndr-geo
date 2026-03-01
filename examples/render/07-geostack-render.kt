@file:JvmName("GeoStackRender")
package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.KEY_F
import org.openrndr.KEY_I
import org.openrndr.KEY_O
import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.KEY_ARROW_UP
import org.openrndr.KEY_ARROW_DOWN
import org.openrndr.KEY_W
import org.openrndr.KEY_A
import org.openrndr.KEY_S
import org.openrndr.KEY_D
import geo.GeoJSON
import geo.geoStack

/**
 * ## 07 - GeoStack Rendering with Zoom/Pan
 *
 * Demonstrates rendering multiple geographic datasets through a unified GeoStack
 * with automatic CRS unification, auto-fitting projection, and interactive
 * zoom/pan controls for iterative map exploration.
 *
 * ### Concepts
 * - `geoStack()` for multi-dataset composition
 * - Automatic CRS unification across sources
 * - Multi-dataset overlay rendering
 * - Auto-fit projection with stack.render()
 * - Interactive view manipulation (zoom, pan, reset)
 *
 * ### Keyboard Controls
 * - `F` - Reset to full view (show all data)
 * - `I` - Zoom in by 1.5x
 * - `O` - Zoom out by 0.75x
 * - Arrow Keys or `W`/`A`/`S`/`D` - Pan view in that direction
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.render.GeoStackRenderKt
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load multiple GeoJSON datasets with different geometry types
        println("Loading datasets...")
        val coastline = GeoJSON.load("examples/data/geo/coastline.geojson")
        val cities = GeoJSON.load("examples/data/geo/populated_places.geojson")
        val riversLakes = GeoJSON.load("examples/data/geo/rivers_lakes.geojson")

        // Create a GeoStack combining all sources
        // Automatically unifies CRS across all datasets
        println("Creating GeoStack with ${listOf(coastline, cities, riversLakes).size} sources...")
        val stack = geoStack(coastline, cities, riversLakes)

        println("GeoStack ready: ${stack.sourceCount()} sources, unified CRS: ${stack.crs}")
        println("\nInteractive controls:")
        println("  F - Reset to full view")
        println("  I - Zoom in")
        println("  O - Zoom out")
        println("  Arrow keys or WASD - Pan view")

        extend {
            // Clear with black background
            drawer.clear(ColorRGBa.BLACK)

            // Render all features with current view
            // Projection automatically uses current view bounds
            stack.render(drawer)
        }

        // Key bindings for iterative exploration
        keyboard.keyDown.listen {
            when (it.key) {
                // Reset to full view
                KEY_F -> {
                    stack.reset()
                    println("Reset to full view")
                }
                // Zoom in
                KEY_I -> {
                    stack.zoom(1.5)
                    println("Zoomed in 1.5x")
                }
                // Zoom out
                KEY_O -> {
                    stack.zoom(0.75)
                    println("Zoomed out 0.75x")
                }
                // Pan directions (arrow keys or WASD)
                KEY_ARROW_LEFT, KEY_A -> {
                    stack.pan(-stack.getCurrentViewBounds().width * 0.2, 0.0)
                    println("Panned left")
                }
                KEY_ARROW_RIGHT, KEY_D -> {
                    stack.pan(stack.getCurrentViewBounds().width * 0.2, 0.0)
                    println("Panned right")
                }
                KEY_ARROW_UP, KEY_W -> {
                    stack.pan(0.0, stack.getCurrentViewBounds().height * 0.2)
                    println("Panned up")
                }
                KEY_ARROW_DOWN, KEY_S -> {
                    stack.pan(0.0, -stack.getCurrentViewBounds().height * 0.2)
                    println("Panned down")
                }
                else -> {}
            }
        }
    }
}
