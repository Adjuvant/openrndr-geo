@file:JvmName("ProjectionPackage")
@file:Suppress("unused")

package geo.projection

// ============================================================================
// geo.projection Package-Level Wildcard Exports
// ============================================================================
// This file provides wildcard exports for the geo.projection package.
// Import with: import geo.projection.*
//
// Exports all projection-related APIs:
// - GeoProjection interface and implementations
// - Projection factories and configuration
// - CRS transformation utilities
// - Helper functions for coordinate manipulation
// ============================================================================

// ----------------------------------------------------------------------------
// Core Projection Interface
// ----------------------------------------------------------------------------
// Re-export GeoProjection interface from GeoProjection.kt

/**
 * Core abstraction for map projections.
 *
 * Allows mixing coordinate systems (lat/lng, BNG) in visualizations.
 * Each projection manages its own coordinate transformations and configuration.
 */
public typealias GeoProjectionExport = GeoProjection

// ----------------------------------------------------------------------------
// Projection Implementations
// ----------------------------------------------------------------------------
// Re-export all projection classes

/**
 * Mercator projection with fitBounds support.
 *
 * Implements the Web Mercator projection with proper zoom/scale semantics
 * and bounding box fitting functionality.
 */
public typealias ProjectionMercatorExport = ProjectionMercator

/**
 * Equirectangular projection for geographic coordinates.
 *
 * Simple linear mapping, suitable for basic world maps.
 */
public typealias ProjectionEquirectangularExport = ProjectionEquirectangular

/**
 * British National Grid projection using EPSG:27700 (OSGB36).
 *
 * Uses proj4j for CRS transformation from WGS84 (EPSG:4326).
 */
public typealias ProjectionBNGExport = ProjectionBNG

/**
 * Identity projection that bypasses all coordinate transformation.
 * Use when data is already in screen coordinates.
 */
public typealias RawProjectionExport = RawProjection

// ----------------------------------------------------------------------------
// Projection Factory
// ----------------------------------------------------------------------------
// Re-export ProjectionFactory and related enums

/**
 * Factory for creating pre-configured projections.
 *
 * Provides convenient methods for creating Mercator, Equirectangular, and BNG
 * projections with common configurations.
 */
public typealias ProjectionFactoryExport = ProjectionFactory

/**
 * Enumeration of supported projection types.
 */
public typealias ProjectionTypeExport = ProjectionType

// Constants
public val PROJECTION_EQUIRECTANGULAR: ProjectionType = ProjectionType.EQUIRECTANGULAR
public val PROJECTION_MERCATOR: ProjectionType = ProjectionType.MERCATOR

// ----------------------------------------------------------------------------
// Projection Configuration
// ----------------------------------------------------------------------------
// Re-export ProjectionConfig and related classes

/**
 * Configuration for map projections.
 *
 * Provides camera-like control over projections including viewport dimensions,
 * center point, zoom level, and optional bounding box.
 */
public typealias ProjectionConfigExport = ProjectionConfig

/**
 * Builder for creating ProjectionConfig instances with a DSL.
 */
public typealias ProjectionConfigBuilderExport = ProjectionConfigBuilder

/**
 * Result of fitBounds calculation, useful for animation.
 */
public typealias FitParametersExport = FitParameters

// Constants
public const val MAX_MERCATOR_LATITUDE: Double = MAX_MERCATOR_LAT

// ----------------------------------------------------------------------------
// CRS Transformation
// ----------------------------------------------------------------------------
// Re-export CRSTransformer and related utilities

/**
 * Lightweight wrapper around proj4j's CoordinateTransform for CRS transformations.
 *
 * Creates a single CoordinateTransform instance in constructor for reuse across
 * many coordinate transformations.
 */
public typealias CRSTransformerExport = CRSTransformer

// CRS Extensions are automatically available via wildcard import

// ----------------------------------------------------------------------------
// Utility Functions
// ----------------------------------------------------------------------------
// Re-export coordinate manipulation utilities from UtilityFunctions.kt

/**
 * Clamp latitude to valid Mercator range (avoid pole overflow).
 *
 * @param latitude Latitude in degrees
 * @param max Maximum latitude, defaults to 85.05112878
 * @return Clamped latitude in degrees
 */
public val clampLatitudeFunc = ::clampLatitude

/**
 * Normalize longitude to standard range [-180, 180].
 *
 * @param longitude Longitude in degrees
 * @return Normalized longitude in degrees [-180, 180]
 */
public val normalizeLongitudeFunc = ::normalizeLongitude

/**
 * Normalize geographic coordinates to standard range.
 *
 * @param latLng Geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @return Normalized geographic coordinates
 */
public val normalizeCoordinateFunc = ::normalizeCoordinate

/**
 * Check if screen coordinate is visible on screen.
 *
 * @param point Screen coordinate to check
 * @param bounds Screen bounds as Rectangle
 * @return true if point is on screen, false otherwise
 */
public val isOnScreenFunc = ::isOnScreen

/**
 * Check if coordinate is within valid geographic range.
 *
 * @param latLng Geographic coordinates as Vector2 (x=longitude, y=latitude)
 * @return true if coordinates are valid for Mercator
 */
public val isValidCoordinateFunc = ::isValidCoordinate

/**
 * Check if coordinate is within UK bounds for BNG transformation.
 *
 * @param bng BNG coordinates as Vector2 (x=easting, y=northing) in meters
 * @return true if coordinates are within UK grid area
 */
public val isBNGValidFunc = ::isBNGValid

// ----------------------------------------------------------------------------
// Screen Transformation
// ----------------------------------------------------------------------------
// Re-export ScreenTransform utilities

// ScreenTransform is internal - not exported at package level

// ----------------------------------------------------------------------------
// Convenience Factory Functions
// ----------------------------------------------------------------------------
// Provide convenient top-level functions for common projection creation

/**
 * Create a Mercator projection for the given viewport.
 *
 * @param width Screen width in pixels
 * @param height Screen height in pixels
 * @param center Center coordinates (lat/lng), defaults to (0, 0)
 * @param zoomLevel Zoom level (0 = whole world, higher = more zoomed in)
 * @return Configured Mercator projection
 */
public fun mercator(
    width: Double = 800.0,
    height: Double = 600.0,
    center: org.openrndr.math.Vector2? = null,
    zoomLevel: Double = 0.0
): ProjectionMercator = ProjectionFactory.mercator(width, height, center, zoomLevel)

/**
 * Create an Equirectangular projection for the given viewport.
 *
 * @param width Screen width in pixels
 * @param height Screen height in pixels
 * @param center Center coordinates (lat/lng), defaults to (0, 0)
 * @param zoomLevel Zoom level (0 = whole world, higher = more zoomed in)
 * @return Configured Equirectangular projection
 */
public fun equirectangular(
    width: Double = 800.0,
    height: Double = 600.0,
    center: org.openrndr.math.Vector2? = null,
    zoomLevel: Double = 0.0
): ProjectionEquirectangular = ProjectionFactory.equirectangular(width, height, center, zoomLevel)

/**
 * Create a British National Grid projection for the UK.
 *
 * @param width Screen width in pixels
 * @param height Screen height in pixels
 * @return Configured BNG projection
 */
public fun bng(
    width: Double = 800.0,
    height: Double = 600.0
): ProjectionBNG = ProjectionFactory.bng(width, height)

/**
 * Create a projection fitted to the specified geographic bounds.
 *
 * @param bounds Geographic bounds to fit
 * @param width Screen width in pixels
 * @param height Screen height in pixels
 * @param padding Padding in pixels around the viewport (default 20.0)
 * @param projection Projection type to use
 * @return Configured projection fitted to bounds
 */
public fun fitBounds(
    bounds: geo.core.Bounds,
    width: Double,
    height: Double,
    padding: Double = 20.0,
    projection: ProjectionType = ProjectionType.EQUIRECTANGULAR
): GeoProjection = ProjectionFactory.fitBounds(bounds, width, height, padding, projection)
