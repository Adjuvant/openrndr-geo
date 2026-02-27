package geo.render

import geo.Feature
import geo.projection.GeoProjection

/**
 * Configuration for geo rendering with DSL builder support.
 *
 * ## Tier 1: Beginner (no config)
 * ```kotlin
 * drawer.geo(source)  // Auto-fit, default style
 * ```
 *
 * ## Tier 2: Professional (with config block)
 * ```kotlin
 * drawer.geo(source) {
 *     projection = ProjectionMercator { width = 800; height = 600 }
 *     style = Style { stroke = ColorRGBa.WHITE; strokeWeight = 1.0 }
 *     styleByType = mapOf(
 *         "Polygon" to Style { fill = ColorRGBa.RED },
 *         "LineString" to Style { stroke = ColorRGBa.BLUE }
 *     )
 *     styleByFeature = { feature ->
 *         if (feature.doubleProperty("pop") > 1000000) {
 *             Style { stroke = ColorRGBa.YELLOW; strokeWeight = 2.0 }
 *         } else null  // Falls back to styleByType or style
 *     }
 * }
 * ```
 *
 * @property projection Optional projection (null = auto-fit to data bounds)
 * @property style Global style override
 * @property styleByType Style map keyed by geometry type name
 * @property styleByFeature Per-feature style function (returns null for fallback)
 */
data class GeoRenderConfig(
    var projection: GeoProjection? = null,
    var style: Style? = null,
    var styleByType: Map<String, Style> = emptyMap(),
    var styleByFeature: ((Feature) -> Style?)? = null
) {
    companion object {
        /**
         * Create GeoRenderConfig using DSL syntax.
         * Follows Style { } and ProjectionMercator { } patterns.
         */
        operator fun invoke(block: GeoRenderConfig.() -> Unit): GeoRenderConfig {
            return GeoRenderConfig().apply(block)
        }
    }
    
    /**
     * Returns a snapshot of this config for safe iteration.
     * Prevents mutation during render loop.
     */
    fun snapshot(): GeoRenderConfig = GeoRenderConfig(
        projection = projection,
        style = style,
        styleByType = styleByType.toMap(),
        styleByFeature = styleByFeature
    )
}

/**
 * Resolve style for a feature using precedence chain:
 * 1. styleByFeature(feature) returns non-null → use it
 * 2. styleByType[geometryType] exists → use it
 * 3. style global override → use it
 * 4. Geometry-type default → use StyleDefaults
 */
fun resolveStyle(feature: Feature, config: GeoRenderConfig): Style {
    // 1. Per-feature function
    config.styleByFeature?.invoke(feature)?.let { return it }
    
    // 2. By-type map
    val typeName = when (feature.geometry) {
        is geo.Point -> "Point"
        is geo.LineString -> "LineString"
        is geo.Polygon -> "Polygon"
        is geo.MultiPoint -> "MultiPoint"
        is geo.MultiLineString -> "MultiLineString"
        is geo.MultiPolygon -> "MultiPolygon"
    }
    config.styleByType[typeName]?.let { return it }
    
    // 3. Global style
    config.style?.let { return it }
    
    // 4. Default
    return StyleDefaults.forGeometry(feature.geometry)
}
