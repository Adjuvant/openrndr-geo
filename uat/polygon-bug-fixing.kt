@file:JvmName("PolygonBugFixing")
package uat

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.*
import geo.render.*

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {
        val antimeridianData = loadGeo("data/geo/test-antimeridian-crossing.geojson")
        val p = antimeridianData.projectToFit(width, height)

        val polyHolesData = loadGeo("data/geo/polygonsWithHole.geojson")
        val holeProjection = polyHolesData.projectToFit(width, height)


        extend {
            drawer.clear(ColorRGBa(0.15, 0.15, 0.15))

            drawer.geo(antimeridianData) {
                projection = p
                fill = ColorRGBa(0.2, 0.6, 0.3, 0.6)  // Green with transparency
                stroke = ColorRGBa(0.1, 0.8, 0.1)     // Dark green stroke
                strokeWeight = 1.0
            }

            drawer.geo(polyHolesData){
                projection = holeProjection
                fill = ColorRGBa.YELLOW
            }
        }
    }
}
