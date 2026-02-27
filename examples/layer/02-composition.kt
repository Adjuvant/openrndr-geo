@file:JvmName("Composition")
package examples.layer

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.LineString
import geo.Point
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.projection.toScreen
import geo.layer.generateGraticuleSource
import geo.render.Style
import geo.render.drawLineString
import geo.render.drawPoint
import geo.render.Shape
import geo.render.withAlpha
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.fx.blend.Add
import org.openrndr.extra.fx.blend.Multiply

/**
 * ## 02 - Layer Composition
 *
 * Demonstrates composing multiple layers with different data sources, blend modes,
 * and z-ordering using the orx-compositor library.
 *
 * ### Concepts
 * - Layer stacking with orx-compositor
 * - Blend modes (Add, Multiply) for layer effects
 * - Z-ordering: later layers draw on top
 * - Combining graticule with geographic data
 * - Background and foreground layer organization
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.layer.CompositionKt
 * ```
 */
fun main() = application {
    configure {
        width = 1000
        height = 700
    }

    program {
        // Load multiple data sources
        val coastline = try {
            GeoJSON.load("examples/data/geo/coastline.geojson")
        } catch (e: Exception) {
            println("Could not load coastline: ${e.message}")
            null
        }

        val rivers = try {
            GeoJSON.load("examples/data/geo/rivers_lakes.geojson")
        } catch (e: Exception) {
            println("Could not load rivers: ${e.message}")
            null
        }

        val places = try {
            GeoJSON.load("examples/data/geo/populated_places.geojson")
        } catch (e: Exception) {
            println("Could not load places: ${e.message}")
            null
        }

        // Create projection
        val projection = ProjectionFactory.fitBounds(
            geo.Bounds(-180.0, -90.0, 180.0, 90.0),
            width.toDouble(),
            height.toDouble(),
            padding = 20.0,
            projection = ProjectionType.EQUIRECTANGULAR
        )

        // Generate graticule for reference
        val graticule = generateGraticuleSource(30.0, geo.Bounds(-180.0, -90.0, 180.0, 90.0))

        // Create composite with multiple layers
        val composite = compose {
            // Layer 1: Background (dark ocean)
            layer {
                draw {
                    drawer.clear(ColorRGBa(0.02, 0.05, 0.15))
                }
            }

            // Layer 2: Graticule (reference grid)
            layer {
                blend(Multiply())
                draw {
                    drawer.stroke = ColorRGBa.WHITE.withAlpha(0.15)
                    drawer.strokeWeight = 1.0

                    // Draw latitude lines
                    for (lat in -90..90 step 30) {
                        val left = projection.toScreen(-180.0, lat.toDouble())
                        val right = projection.toScreen(180.0, lat.toDouble())
                        drawer.lineSegment(left.x, left.y, right.x, right.y)
                    }

                    // Draw longitude lines
                    for (lng in -180..180 step 30) {
                        val top = projection.toScreen(lng.toDouble(), 90.0)
                        val bottom = projection.toScreen(lng.toDouble(), -90.0)
                        drawer.lineSegment(top.x, top.y, bottom.x, bottom.y)
                    }
                }
            }

            // Layer 3: Coastline (base layer)
            layer {
                draw {
                    coastline?.features?.forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is LineString -> {
                                val screenPoints = geometry.points.map { pt ->
                                    projection.toScreen(pt.x, pt.y)
                                }
                                drawLineString(drawer, screenPoints, Style {
                                    fill = null
                                    stroke = ColorRGBa.CORNFLOWER_BLUE
                                    strokeWeight = 2.0
                                })
                            }
                        }
                    }
                }
            }

            // Layer 4: Rivers (additive blend for glow effect)
            layer {
                blend(Add())
                draw {
                    rivers?.features?.take(200)?.forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is LineString -> {
                                val screenPoints = geometry.points.map { pt ->
                                    projection.toScreen(pt.x, pt.y)
                                }
                                drawLineString(drawer, screenPoints, Style {
                                    fill = null
                                    stroke = ColorRGBa.CYAN.withAlpha(0.6)
                                    strokeWeight = 2.5
                                })
                            }
                        }
                    }
                }
            }

            // Layer 5: Populated places (highlighted points)
            layer {
                draw {
                    places?.features?.take(100)?.forEach { feature ->
                        when (val geometry = feature.geometry) {
                            is Point -> {
                                val screen = projection.toScreen(geometry.x, geometry.y)
                                drawPoint(drawer, screen, Style {
                                    fill = ColorRGBa.ORANGE
                                    stroke = ColorRGBa.WHITE
                                    strokeWeight = 1.0
                                    size = 6.0
                                    shape = Shape.Circle
                                })
                            }
                        }
                    }
                }
            }

            // Layer 6: Title overlay
            layer {
                draw {
                    drawer.fill = ColorRGBa.WHITE
                    drawer.text("Layer Composition", 20.0, 30.0)
                    drawer.text("Background -> Graticule -> Coastline -> Rivers (Add) -> Places", 20.0, 50.0)

                    // Legend
                    drawer.fill = ColorRGBa.CORNFLOWER_BLUE
                    drawer.text("Coastline", 750.0, 30.0)
                    drawer.fill = ColorRGBa.CYAN
                    drawer.text("Rivers (Add blend)", 750.0, 50.0)
                    drawer.fill = ColorRGBa.ORANGE
                    drawer.text("Populated Places", 750.0, 70.0)
                }
            }
        }

        extend {
            composite.draw(drawer)
        }
    }
}
