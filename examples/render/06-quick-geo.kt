@file:JvmName("QuickGeo")
package examples.anim

import geo.geoSource
import geo.render.geo
import org.openrndr.application

fun main() = application {
    configure {
        width = 700
        height = 700
    }

    program {
        // Load geographic data
        val data = geoSource("examples/data/geo/populated_places.geojson")

        extend {
            // Draw geo data with the default style
            drawer.geo(data)
        }
    }
}