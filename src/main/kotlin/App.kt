/**
 * Canonical Entry Point for openrndr-geo
 *
 * This is the primary entry point for the openrndr-geo library.
 * It demonstrates the essential three-line workflow:
 * 1. loadGeo() - auto-magic loading with caching
 * 2. projectToFit() - automatic projection fitting
 * 3. drawer.geo() - render with inline style DSL
 *
 * For a more comprehensive example with animations, multiple datasets,
 * and advanced features, see TemplateProgram.kt.
 */

import geo.core.*
import geo.crs.CRS
import geo.render.*
import geo.projection.*
import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        // Three-line workflow with auto-magic loading
        val topo = loadGeo("data/geo/catchment-topo.geojson")

        // Create projections that fit the data to the viewport
        val topoProj = ProjectionFactory.fitBounds(
            topo.totalBoundingBox(),
            width.toDouble(), height.toDouble(), 135.0,
            PROJECTION_MERCATOR
        )

//        val sortedFeatures = data.features.sortedBy { it.boundingBox.center }

        // Render topo with bucketed per-segment gradient
        val propValues = topo.features.mapNotNull { it.doubleProperty("property_value") }
        val minProp = propValues.minOrNull() ?: 0.0
        val maxProp = propValues.maxOrNull() ?: 1.0
        val propRange = maxProp - minProp

        val buckets = Array(20) { mutableListOf<LineSegment>() }

        topo.features.forEach { feature ->
            if (feature.geometry is LineString) {
                val geom = feature.geometry
                val normProp = if (propRange > 0) {
                    ((feature.doubleProperty("property_value") ?: 0.0) - minProp) / propRange
                } else 0.5

                val pts = geom.toScreen(topoProj)
                val segCount = pts.size - 1

                pts.zipWithNext().forEachIndexed { i, (a, b) ->
                    val t = i.toDouble() / segCount.coerceAtLeast(1)
                    val combined = t * normProp
                    val bucket = (combined * 20).toInt().coerceIn(0, 19)
                    buckets[bucket] += LineSegment(a, b)
                }
            }
        }

        extend {
            // Clear background
            drawer.clear(ColorRGBa.BLACK)

            val pingPong = 1.0 - kotlin.math.abs((seconds % 10.0) / 5.0 - 1.0)

            drawer.translate(80.0, -60.0)
            drawer.strokeWeight = 1.0
            buckets.forEachIndexed { bucket, segments ->
                if (segments.isNotEmpty()) {
                    drawer.stroke = ColorHSVa(bucket * 18.0, 0.8, 0.9, 1.0).toRGBa()
                    drawer.translate(pingPong * -bucket *.7, pingPong * bucket * 0.5)
                    drawer.rotate(pingPong*(.05*bucket))
                    drawer.lineSegments(segments)
                }
            }
        }
    }
}
