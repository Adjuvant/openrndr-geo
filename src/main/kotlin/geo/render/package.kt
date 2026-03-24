@file:JvmName("RenderPackage")
@file:Suppress("unused")

package geo.render

// ============================================================================
// geo.render Package-Level Wildcard Exports
// ============================================================================
// This file provides wildcard exports for the geo.render package.
// Import with: import geo.render.*
//
// Exports all rendering-related APIs:
// - Drawer extension functions (drawer.geo(), drawer.geoJSON(), etc.)
// - Style configuration and defaults
// - Individual geometry renderers (Point, Line, Polygon)
// - Multi-geometry renderers
// - Configuration classes
// ============================================================================

// ----------------------------------------------------------------------------
// Drawer Extensions - Primary Rendering API
// ----------------------------------------------------------------------------
// Re-export Drawer.geo() extension functions from DrawerGeoExtensions.kt
// These are the main entry points for rendering geo data

/**
 * Extension function for Drawer providing simplified GeoJSON rendering.
 *
 * ## Tier 1: One-line rendering
 * ```kotlin
 * extend {
 *     drawer.geoJSON("world.json")  // Auto-load, auto-fit, auto-render
 * }
 * ```
 *
 * ## Tier 2: Load once, draw many
 * ```kotlin
 * val source = geoSource("data.json")
 * extend {
 *     source.render(drawer)
 * }
 * ```
 *
 * ## Tier 3: Full control
 * ```kotlin
 * val features = GeoJSON.load("data.json")
 * val projection = ProjectionMercator { width = 800; height = 600 }
 * extend {
 *     features.forEach { drawer.draw(it.geometry, projection) }
 * }
 * ```
 */
public typealias DrawerGeoExtensions = Unit  // Marker for documentation

// Drawer.geo() extension functions are automatically available via wildcard import
// - Drawer.geo(geometry: Geometry, ...)
// - Drawer.geo(source: GeoSource, ...)
// - Drawer.geo(source: GeoSource, block: GeoRenderConfig.() -> Unit)
// - Drawer.geoJSON(path: String, ...)
// - Drawer.geoFeatures(features: Sequence<Feature>, ...)

// ----------------------------------------------------------------------------
// Style Configuration
// ----------------------------------------------------------------------------
// Re-export Style and related classes from Style.kt and StyleDefaults.kt

/**
 * Style configuration for rendering geo primitives.
 *
 * Mutable data class optimized for zero-allocation performance in real-time animation.
 *
 * ## DSL Usage
 * ```kotlin
 * val myStyle = Style {
 *     fill = ColorRGBa.RED
 *     stroke = ColorRGBa.BLACK
 *     strokeWeight = 2.0
 *     size = 10.0
 *     shape = Shape.Circle
 * }
 * ```
 */
public typealias StyleExport = Style

/**
 * Default style configurations for different geometry types.
 *
 * Provides sensible defaults per geometry type that users can override.
 */
public typealias StyleDefaultsExport = StyleDefaults

/**
 * Shape types for point rendering.
 *
 * - Circle: Circular point marker (default)
 * - Square: Square point marker
 * - Triangle: Triangle point marker (equilateral)
 */
public typealias ShapeExport = Shape

// ----------------------------------------------------------------------------
// Rendering Functions - Point, Line, Polygon
// ----------------------------------------------------------------------------
// Re-export rendering functions from PointRenderer.kt, LineRenderer.kt, PolygonRenderer.kt

// Drawing functions are automatically available via wildcard import:
// - drawPoint(drawer, x, y, style)
// - drawPoint(drawer, point, style)
// - drawLineString(drawer, points, style)
// - drawPolygon(drawer, points, style)
// - drawPolygon(drawer, polygon, projection, style)
// - writeLineString(drawer, points, style)
// - writePolygon(drawer, points, style)
// - writePolygonWithHoles(drawer, exterior, interiors, style)

// ----------------------------------------------------------------------------
// Multi-Geometry Renderers
// ----------------------------------------------------------------------------
// Multi-geometry rendering functions are available via wildcard import:
// - drawMultiPoint(drawer, multiPoint, projection, style)
// - drawMultiLineString(drawer, multiLineString, projection, style)
// - drawMultiPolygon(drawer, multiPolygon, projection, style, clampToMercatorBounds)

// ----------------------------------------------------------------------------
// Configuration Classes
// ----------------------------------------------------------------------------
// Re-export GeoRenderConfig and resolveStyle from GeoRenderConfig.kt

/**
 * Configuration for geo rendering with DSL builder support.
 *
 * ## Usage
 * ```kotlin
 * drawer.geo(source) {
 *     projection = ProjectionMercator { width = 800; height = 600 }
 *     style = Style { stroke = ColorRGBa.WHITE }
 *     styleByType = mapOf("Polygon" to Style { fill = ColorRGBa.RED })
 * }
 * ```
 */
public typealias GeoRenderConfigExport = GeoRenderConfig

/**
 * Resolve style for a feature using precedence chain.
 * Available via wildcard import.
 */
public typealias ResolveStyleFunc = (geo.core.Feature, GeoRenderConfig) -> Style

// Utility functions available via wildcard import:
// - resolveStyle(feature, config)
// - mergeStyles(default, user)

// ----------------------------------------------------------------------------
// Convenience Aliases
// ----------------------------------------------------------------------------
// Extension functions available via wildcard import:
// - ColorRGBa.withAlpha(alpha) - create transparent color version
