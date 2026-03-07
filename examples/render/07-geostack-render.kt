@file:JvmName("GeoStackRender")

package examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.KEY_ARROW_UP
import org.openrndr.KEY_ARROW_DOWN
import geo.GeoJSON
import geo.GeoStack
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
        val coastline = GeoJSON.load("examples/data/geo/coastline.geojson",optimize = true)
        val cities = GeoJSON.load("examples/data/geo/populated_places.geojson",optimize = true)
        val riversLakes = GeoJSON.load("examples/data/geo/rivers_lakes.geojson",optimize = true)

        // Create a GeoStack combining all sources
        // Automatically unifies CRS across all datasets
        println("Creating GeoStack with ${listOf(coastline, cities, riversLakes).size} sources...")
        val stack = geoStack(coastline, cities, riversLakes)

        println("GeoStack ready: ${stack.sourceCount()} sources, unified CRS: ${stack.crs}")
        println("\nInteractive controls:")
        println("  f - Reset to full view")
        println("  i - Zoom in")
        println("  o - Zoom out")
        println("  Arrow keys or WASD - Pan view")
        // Key bindings for iterative exploration
        keyboard.keyDown.listen {
            when (it.key) {

                // Pan directions (arrow keys or WASD)
                KEY_ARROW_LEFT -> {
                    panLeft(stack)
                }

                KEY_ARROW_RIGHT -> {
                    panRight(stack)
                }

                KEY_ARROW_UP -> {
                    panUp(stack)
                }

                KEY_ARROW_DOWN -> {
                    panDown(stack)
                }

                else -> {}
            }
            when (it.name) {
                // Reset to full view
                "f" -> {
                    stack.reset()
                    println("Reset to full view")
                }
                // Zoom in
                "i" -> {
                    stack.zoom(1.5)
                    println("Zoomed in 1.5x")
                }
                // Zoom out
                "o" -> {
                    stack.zoom(0.75)
                    println("Zoomed out 0.75x")
                }
                // Pan directions (arrow keys or WASD)
                "a" -> {
                    panLeft(stack)
                }

                "d" -> {
                    panRight(stack)
                }

                "w" -> {
                    panUp(stack)
                }

                "s" -> {
                    panDown(stack)
                }

                else -> {}
            }
        }

        extend {
            // Clear with black background
            drawer.clear(ColorRGBa.BLACK)
            drawer.text("${frameCount} :: ${frameCount/seconds} FPS", 20.0, 20.0)
            // Render all features with current view
            // Projection automatically uses current view bounds
            stack.render(drawer)
        }
    }
}

private fun panUp(stack: GeoStack) {
    stack.pan(0.0, stack.getCurrentViewBounds().height * 0.2)
    println("Panned up")
}

private fun panLeft(stack: GeoStack) {
    stack.pan(-stack.getCurrentViewBounds().width * 0.2, 0.0)
    println("Panned left")
}

private fun panDown(stack: GeoStack) {
    stack.pan(0.0, -stack.getCurrentViewBounds().height * 0.2)
    println("Panned down")
}

private fun panRight(stack: GeoStack) {
    stack.pan(stack.getCurrentViewBounds().width * 0.2, 0.0)
    println("Panned right")
}
