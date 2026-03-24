package geo.core

import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType

/**
 * Create a projection that fits this source's bounds to the viewport.
 *
 * Creates a Mercator projection that fits the data bounds to fill 100%
 * of the specified viewport dimensions (tight fit, no padding).
 *
 * ## Usage
 * ```kotlin
 * val data = loadGeo("world.json")
 * val projection = data.projectToFit(width = 800, height = 600)
 * drawer.geo(data, projection) { stroke = ColorRGBa.WHITE }
 * ```
 *
 * @param width Viewport width in pixels
 * @param height Viewport height in pixels
 * @param projectionType Projection type to use (default: MERCATOR)
 * @return GeoProjection fitted to data bounds with tight fit (100%)
 */
fun GeoSource.projectToFit(
    width: Int,
    height: Int,
    projectionType: ProjectionType = ProjectionType.MERCATOR
): GeoProjection {
    return ProjectionFactory.fitBounds(
        bounds = this.boundingBox(),
        width = width.toDouble(),
        height = height.toDouble(),
        padding = 20.0,  // Tight fit: 100% of viewport
        projection = projectionType
    )
}

/**
 * Create a projection that fits this source's bounds to the drawer viewport.
 *
 * Convenience overload that uses the drawer's current dimensions.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val projection = data.projectToFit(drawer)
 *     drawer.geo(data, projection)
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer to get viewport dimensions from
 * @param projectionType Projection type to use (default: MERCATOR)
 * @return GeoProjection fitted to data bounds
 */
fun GeoSource.projectToFit(
    drawer: org.openrndr.draw.Drawer,
    projectionType: ProjectionType = ProjectionType.MERCATOR
): GeoProjection {
    return projectToFit(drawer.width, drawer.height, projectionType)
}

/**
 * Create a projection that fits this cached source's bounds to the viewport.
 *
 * Extension for CachedGeoSource to enable the three-line workflow:
 * ```kotlin
 * val data = loadGeo("world.json")
 * val projection = data.projectToFit(width, height)
 * drawer.geo(data, projection)
 * ```
 *
 * @param width Viewport width in pixels
 * @param height Viewport height in pixels
 * @param projectionType Projection type to use (default: MERCATOR)
 * @return GeoProjection fitted to data bounds
 */
fun CachedGeoSource.projectToFit(
    width: Int,
    height: Int,
    projectionType: ProjectionType = ProjectionType.MERCATOR
): GeoProjection {
    return delegate.projectToFit(width, height, projectionType)
}

/**
 * Create a projection that fits this cached source's bounds to the drawer viewport.
 *
 * @param drawer OpenRNDR Drawer to get viewport dimensions from
 * @param projectionType Projection type to use (default: MERCATOR)
 * @return GeoProjection fitted to data bounds
 */
fun CachedGeoSource.projectToFit(
    drawer: org.openrndr.draw.Drawer,
    projectionType: ProjectionType = ProjectionType.MERCATOR
): GeoProjection {
    return delegate.projectToFit(drawer.width, drawer.height, projectionType)
}
