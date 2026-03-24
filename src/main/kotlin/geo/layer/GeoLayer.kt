package geo.layer

import geo.core.Bounds
import geo.core.GeoSource
import geo.render.Style

/**
 * Compositional wrapper for GeoSource with Style.
 *
 * GeoLayer provides a convenient way to define geo data layers for use with
 * orx-compositor's `compose { }` DSL. Each layer wraps a data source and styling
 * configuration. Blend modes are applied directly in the compositor using the
 * blend() function from orx-fx.
 *
 * ## Usage
 * ```kotlin
 * val graticuleLayer = GeoLayer {
 *     source = generateGraticuleSource(5.0, bounds)
 *     style = Style {
 *         stroke = ColorRGBa.WHITE
 *         strokeWeight = 1.0
 *     }
 * }
 *
 * val dataLayer = GeoLayer {
 *     source = loadGeoPackage("data.geo")
 *     style = Style {
 *         fill = ColorRGBa.BLUE.withAlpha(0.5)
 *         stroke = ColorRGBa.BLACK
 *     }
 * }
 *
 * // Use in compositor
 * val composite = compose {
 *     layer {
 *         draw {
 *             // Draw graticule
 *         }
 *     }
 *     layer {
 *         blend(Multiply())  // Apply blend mode
 *         draw {
 *             // Draw data layer
 *         }
 *     }
 * }
 * ```
 *
 * ## Blend Modes
 * The following blend modes from orx-fx work well with geo visualizations:
 *
 * - **Add**: Lightens the result, good for highlights and glowing effects
 * - **Multiply**: Darkens the result, excellent for overlaying on dark backgrounds
 * - **Overlay**: Increases contrast, best for preserving detail when layering
 * - **Screen**: Lightens the result, useful for light backgrounds and glows
 *
 * Import blend modes from `org.openrndr.extra.fx.blend.*`
 *
 * ## Layer Ordering
 * In orx-compositor, layer order matters - layers defined later are drawn on top
 * of earlier layers. The last layer in the `compose { }` block appears as the
 * topmost visual element.
 *
 * @property source The geo data source for this layer
 * @property style The rendering style applied to all features in this layer
 *
 * @see geo.layer.generateGraticule Generate graticule features for reference layers
 * @see geo.layer.generateGraticuleSource Generate a GeoSource from graticule features
 * @see org.openrndr.extra.fx.blend.Add Additive blend mode
 * @see org.openrndr.extra.fx.blend.Multiply Multiplicative blend mode
 * @see org.openrndr.extra.fx.blend.Overlay Overlay blend mode
 * @see org.openrndr.extra.fx.blend.Screen Screen blend mode
 */
data class GeoLayer(
    var source: GeoSource? = null,
    var style: Style? = null
) {
    companion object {
        /**
         * Create a GeoLayer using DSL syntax.
         *
         * Enables type-safe builder pattern:
         * ```kotlin
         * val layer = GeoLayer {
         *     source = loadGeoJSON("data.json")
         *     style = Style { fill = ColorRGBa.RED }
         * }
         * ```
         *
         * @param block Configuration lambda applied to new GeoLayer instance
         * @return Configured GeoLayer instance
         */
        operator fun invoke(block: GeoLayer.() -> Unit): GeoLayer {
            val layer = GeoLayer()
            layer.block()
            return layer
        }
    }
}

/**
 * Convenience function for creating a GeoLayer with DSL syntax.
 *
 * Alternative to `GeoLayer { }` that provides a more concise syntax:
 * ```kotlin
 * val layer = layer {
 *     source = loadGeoPackage("data.gpkg")
 *     style = Style { stroke = ColorRGBa.BLUE }
 * }
 * ```
 *
 * @param block Configuration lambda applied to new GeoLayer instance
 * @return Configured GeoLayer instance
 */
fun layer(block: GeoLayer.() -> Unit): GeoLayer {
    return GeoLayer(block)
}
