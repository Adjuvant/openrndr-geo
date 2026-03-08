/**
 * Template Program - OPENRNDR GEO Starter
 * 
 * This is a demonstration template showing the basic usage of the openrndr-geo library.
 * It demonstrates:
 * - Loading multiple GeoJSON datasets
 * - Creating a geoStack to combine sources
 * - Using ProjectionFactory for viewport fitting
 * - Chain operations: filter().map().withProjection()
 * - Rendering with custom styles
 * 
 * Note: This file serves as both a runnable example and a starting point for new projects.
 */

import geo.GeoPackage
import geo.GeoSource
import geo.animation.animator
import geo.forEachWithProjection
import geo.geoSource
import geo.geoStack
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.projection.toWGS84
import geo.render.Style
import geo.render.geo
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 800
        height = 800
    }



    program {
        val animator = animator()
        animator.apply{
            ::x.animate(100.0, 2000, Easing.QuartInOut)
        }

        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/default.otf", 64.0)

        // Load multiple datasets with potentially different CRS
        val ocean = geoSource("data/geo/ocean.geojson")
        val coastline = geoSource("data/geo/coastline.geojson")  // WGS84
        val cities = geoSource("data/geo/populated_places.geojson")       // WGS84
        val rivers = geoSource("data/geo/rivers_lakes.geojson")        // Could be different

        // Create a stack - automatically unifies to first source's CRS
        val map = geoStack( ocean,coastline, cities, rivers) // ocean,

        // Without the toWGS84 it ends up off the screen.
        var ness = GeoPackage.load("data/geo/ness-vectors.gpkg").toWGS84()
        // ness.printSummary()
        val nessProjection = ProjectionFactory.fitBounds(ness.totalBoundingBox(),
            width.toDouble(), height.toDouble(),
            padding = 0.0, projection = ProjectionType.MERCATOR)

        ness.withProjection(projection = nessProjection)
            .take(1).forEachWithProjection { feature, geometry ->
                println("==== FEATURES ====")
                println(feature)
                println("==== GEOMETRY ====")
                println(geometry)
        }

        val sampleData = geoSource("data/sample.geojson")
        val projection = ProjectionFactory.fitBounds(
            sampleData.totalBoundingBox(),
            width.toDouble(), height.toDouble(),
            padding = 20.0,
            projection = ProjectionType.MERCATOR
        )

        // Test: filter().map().withProjection() chain
        // Chain order: filter features -> transform -> apply projection -> iterate with geometry
        sampleData
            .filter { it.propertyKeys().contains("population") }
            .map { it }  // Identity transform
            .withProjection(projection)
            .take(5)
            .forEachWithProjection { feature, geometry ->
                println("Feature: ${feature.propertyKeys()}")
                println("Name: ${feature.property("name")}")
                println("Projected: $geometry")
            }

        val topo = geoSource("data/geo/catchment-topo.geojson")

        val topoProjection = ProjectionFactory.fitBounds(
            topo.totalBoundingBox(),
            width.toDouble(), height.toDouble(),
            padding = 50.0
        )
        extend {
            animator.updateAnimation()
            drawer.clear(ColorRGBa.BLACK)
            drawer.points {
                repeat(20000) {
                    fill = rgb((it * 0.01 - seconds) % 1)
                    point((it * it * 0.013) % width, (it * 4.011) % height)
                }
            }
//            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
//            drawer.image(image)

            drawer.fill = ColorRGBa.PINK
            drawer.circle(cos(seconds) * width / 2.0 + width / 2.0, sin(0.5 * seconds) * height / 2.0 + height / 2.0, 140.0)

            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text("OPENRNDR GEO", width / 4.0+animator.x, height / 2.0)

            // Fit to view and render
            map.render(drawer)
//            ness.render(drawer, nessProjection)
//            drawer.geo(topo){ this.projection = topoProjection
//                styleByFeature = { feature ->
//                    val height = feature.doubleProperty("property_value") ?: 100.0
//                    Style(
//                        stroke = ColorRGBa(
//                            r = (height / 300.0).coerceIn(0.0, 1.0),
//                            g = 0.5,
//                            b = 1.0 - (height / 300.0).coerceIn(0.0, 1.0)
//                        ),
//                        strokeWeight = 1.0
//                    )
//                }
//            }

            // Use explicit type to avoid overload ambiguity
            drawer.geo(topo, block = fun geo.render.GeoRenderConfig.() {
                this.projection = topoProjection
                styleByType = mapOf(
                    "LineString" to Style(stroke = ColorRGBa.BLUE, strokeWeight = 1.5)
                )
            })
        }
    }
}
