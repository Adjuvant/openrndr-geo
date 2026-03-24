@file:JvmName("SyntheticDataGen")
package examples.tools

import geo.core.geoSource
import geo.render.geo
import geo.tools.SyntheticData
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import kotlin.random.Random

fun main() = application {
    configure {
        width = 700
        height = 700
    }

    program {
        // Base map (static)
        val coastline = geoSource("examples/data/geo/coastline.geojson")

        // Mutable layer slots — each regenerated on its own 5s cycle
        var scatter = SyntheticData.Presets.scatterPlot(count = 150, seed = 1L)
        var grid    = SyntheticData.Presets.grid(cols = 12, rows = 8, seed = 2L)
        var rings   = SyntheticData.Presets.concentricRings(rings = 6, seed = 3L)
        var burst   = SyntheticData.Presets.radialBurst(rays = 20, seed = 4L)

        // Glitch state
        val rng = Random.Default
        var lastGlitchFrame = 0
        var glitchIndex = 0  // which layer to hit next (round-robin)

        extend {
            val fps = if (seconds > 0.0) (frameCount / seconds).toInt() else 0
            val glitchInterval = (60 * .5) // ~5 seconds at 60fps

            // --- Regenerate one layer every ~0.5 seconds ---
            if (frameCount - lastGlitchFrame >= glitchInterval) {
                lastGlitchFrame = frameCount
                val seed = rng.nextLong()

                when (glitchIndex % 4) {
                    0 -> {
                        val count = rng.nextInt(50, 300)
                        scatter = SyntheticData.Presets.scatterPlot(count = count, seed = seed)
                    }
                    1 -> {
                        val cols = rng.nextInt(4, 20)
                        val rows = rng.nextInt(4, 16)
                        grid = SyntheticData.Presets.grid(cols = cols, rows = rows, seed = seed)
                    }
                    2 -> {
                        val n = rng.nextInt(3, 10)
                        val cx = rng.nextDouble(-120.0, 120.0)
                        val cy = rng.nextDouble(-60.0, 60.0)
                        rings = SyntheticData.Presets.concentricRings(
                            rings = n,
                            center = Vector2(cx, cy),
                            seed = seed
                        )
                    }
                    3 -> {
                        val rays = rng.nextInt(8, 40)
                        val cx = rng.nextDouble(-120.0, 120.0)
                        val cy = rng.nextDouble(-60.0, 60.0)
                        burst = SyntheticData.Presets.radialBurst(
                            rays = rays,
                            center = Vector2(cx, cy),
                            seed = seed
                        )
                    }
                }
                glitchIndex++
            }

            // --- Render ---
            drawer.clear(ColorRGBa(0.05, 0.05, 0.1))

            // Base: coastline
            drawer.geo(coastline) {
                fill = ColorRGBa(0.1, 0.12, 0.18)
                stroke = ColorRGBa.WHITE.opacify(0.25)
                strokeWeight = 0.5
            }

            // Layer 1: scatter
            drawer.geo(scatter) {
                fill = ColorRGBa.PINK.opacify(0.6)
                stroke = null
            }

            // Layer 2: grid
            drawer.geo(grid) {
                fill = ColorRGBa.YELLOW.opacify(0.35)
                stroke = null
            }

            // Layer 3: concentric rings
            drawer.geo(rings) {
                fill = null
                stroke = ColorRGBa(0.3, 0.8, 1.0, 0.5)
                strokeWeight = 1.2
            }

            // Layer 4: radial burst
            drawer.geo(burst) {
                fill = null
                stroke = ColorRGBa(1.0, 0.6, 0.2, 0.7)
                strokeWeight = 0.8
            }

            // HUD
            drawer.fill = ColorRGBa.WHITE
            drawer.text("$fps FPS", 20.0, 20.0)
        }
    }
}