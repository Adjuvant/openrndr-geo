@file:JvmName("LayerPackage")
@file:Suppress("unused")

package geo.layer

// ============================================================================
// geo.layer Package-Level Wildcard Exports
// ============================================================================
// This file provides wildcard exports for the geo.layer package.
// Import with: import geo.layer.*
//
// Exports all layer-related APIs:
// - GeoLayer compositional wrapper
// - Graticule generation utilities
// ============================================================================

// ----------------------------------------------------------------------------
// Layer Core
// ----------------------------------------------------------------------------
// Re-export GeoLayer and related functions

/**
 * Compositional wrapper for GeoSource with Style.
 *
 * GeoLayer provides a convenient way to define geo data layers for use with
 * orx-compositor's `compose { }` DSL. Each layer wraps a data source and styling
 * configuration.
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
 * @property source The geo data source for this layer
 * @property style The rendering style applied to all features in this layer
 */
public typealias GeoLayerExport = GeoLayer

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
public typealias LayerFunc = (GeoLayer.() -> Unit) -> GeoLayer

// The layer() function is automatically available via wildcard import
public val createLayerFunc = ::layer

// ----------------------------------------------------------------------------
// Graticule Generation
// ----------------------------------------------------------------------------
// Re-export Graticule utilities

/**
 * Generate graticule (latitude/longitude grid) features for reference layers.
 *
 * Creates Point features at grid line intersections for visual reference.
 * The graticule helps users orient themselves when viewing projected geo data.
 *
 * ## Usage
 * ```kotlin
 * // Generate a 5-degree graticule for the given bounds
 * val graticule = generateGraticule(5.0, bounds)
 *
 * // Use in a layer
 * val graticuleLayer = layer {
 *     source = generateGraticuleSource(5.0, bounds)
 *     style = Style {
 *         stroke = ColorRGBa.WHITE.withAlpha(0.3)
 *         strokeWeight = 0.5
 *     }
 * }
 * ```
 *
 * ## Spacing Values
 * Common spacing values for different zoom levels:
 * - **1.0**: Detailed view, shows individual degree lines
 * - **5.0**: Regional view, good balance of detail and clarity
 * - **10.0**: Continental view, clean reference without clutter
 *
 * @param spacing Grid spacing in degrees (minimum 1.0, typical: 1.0, 5.0, 10.0)
 * @param bounds The geographic bounds to cover with the graticule
 * @return List of Point features at grid intersections
 */
public val generateGraticuleFunc = ::generateGraticule

/**
 * Generate a GeoSource containing graticule features.
 *
 * Wraps [generateGraticule] output in a GeoSource for use with GeoLayer.
 *
 * ## Usage
 * ```kotlin
 * val graticuleLayer = layer {
 *     source = generateGraticuleSource(5.0, dataBounds)
 *     style = Style {
 *         stroke = ColorRGBa.GRAY
 *         strokeWeight = 0.5
 *     }
 * }
 * ```
 *
 * @param spacing Grid spacing in degrees (minimum 1.0, typical: 1.0, 5.0, 10.0)
 * @param bounds The geographic bounds to cover with the graticule
 * @return GeoSource containing Point features at grid intersections
 */
public val generateGraticuleSourceFunc = ::generateGraticuleSource

// ----------------------------------------------------------------------------
// Blend Mode References
// ----------------------------------------------------------------------------
// Documentation for blend modes (actual classes are in orx-fx library)

/**
 * Available blend modes from org.openrndr.extra.fx.blend.*
 *
 * These blend modes work well with geo visualizations:
 *
 * - **Add**: Lightens the result, good for highlights and glowing effects
 *   ```kotlin
 *   blend(Add())
 *   ```
 *
 * - **Multiply**: Darkens the result, excellent for overlaying on dark backgrounds
 *   ```kotlin
 *   blend(Multiply())
 *   ```
 *
 * - **Overlay**: Increases contrast, best for preserving detail when layering
 *   ```kotlin
 *   blend(Overlay())
 *   ```
 *
 * - **Screen**: Lightens the result, useful for light backgrounds and glows
 *   ```kotlin
 *   blend(Screen())
 *   ```
 *
 * Import from: `import org.openrndr.extra.fx.blend.*`
 */
public object BlendModes {
    /**
     * Additive blend mode - lightens the result.
     * Good for highlights and glowing effects.
     */
    const val ADD = "org.openrndr.extra.fx.blend.Add"

    /**
     * Multiplicative blend mode - darkens the result.
     * Excellent for overlaying on dark backgrounds.
     */
    const val MULTIPLY = "org.openrndr.extra.fx.blend.Multiply"

    /**
     * Overlay blend mode - increases contrast.
     * Best for preserving detail when layering.
     */
    const val OVERLAY = "org.openrndr.extra.fx.blend.Overlay"

    /**
     * Screen blend mode - lightens the result.
     * Useful for light backgrounds and glows.
     */
    const val SCREEN = "org.openrndr.extra.fx.blend.Screen"
}
