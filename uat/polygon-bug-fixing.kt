@file:JvmName("PolygonBugFixing")
package uat

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.*
import geo.projection.ProjectionFactory
import geo.render.*
import org.openrndr.internal.colorBufferLoader

fun main() = application {
    configure {
        width = 1000
        height = 500
    }

    program {
        val antimeridianData = loadGeo("data/geo/test-antimeridian-crossing.geojson")
        val p = antimeridianData.projectToFit(width, height)

        val ocean = geoSource("data/geo/ocean.geojson")
        val coastline = geoSource("data/geo/coastline.geojson")  // WGS84
        val cities = geoSource("data/geo/populated_places.geojson")       // WGS84
        val map = geoStack( coastline, cities) // ocean,

        val oceanProjection = ocean.projectToFit(width,height)

        extend {
            drawer.clear(ColorRGBa(0.15, 0.15, 0.15))

//            map.render(drawer)
            drawer.geo(ocean){
                projection = oceanProjection
                stroke = ColorRGBa.PINK
                fill = ColorRGBa.GREEN
            }
            drawer.geo(antimeridianData) {
                projection = p
                fill = ColorRGBa.YELLOW
                stroke = ColorRGBa.BLUE
                strokeWeight = 1.0
            }
        }
    }
}
