package geo.render

import geo.Feature
import geo.internal.OptimizedFeature
import geo.projection.GeoProjection
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin

/**
 * Configuration for geo rendering with DSL builder support.
 *
 * Style properties (fill, stroke, strokeWeight, etc.) are exposed directly
 * on this class for simple one-block styling. They delegate to an internal
 * Style instance which acts as the global/fallback style for the draw call.
 *
 * ## Tier 1: Quick styling
 * ```kotlin
 * drawer.geo(source) {
 *     fill = ColorRGBa.PINK.opacify(0.8)
 *     stroke = ColorRGBa.RED
 *     strokeWeight = 2.0
 * }
 * ```
 *
 * ## Tier 2: Full config
 * ```kotlin
 * drawer.geo(source) {
 *     projection = myProjection
 *     fill = ColorRGBa.WHITE          // global fallback
 *     strokeWeight = 1.0
 *     styleByType = mapOf(
 *         "Polygon" to Style { fill = ColorRGBa.RED },
 *         "LineString" to Style { stroke = ColorRGBa.BLUE }
 *     )
 *     styleByFeature = { feature ->
 *         if (feature.doubleProperty("pop") > 1_000_000)
 *             Style { stroke = ColorRGBa.YELLOW; strokeWeight = 3.0 }
 *         else null  // fall through
 *     }
 * }
 * ```
 *
 * Resolution order: styleByFeature → styleByType → top-level properties → StyleDefaults
 */
class GeoRenderConfig {

    // -- Projection --------------------------------------------------------
    var projection: GeoProjection? = null

    // -- Delegated style surface ------------------------------------------
    // Internal Style instance; top-level fill/stroke/etc. read and write
    // through this. Replaces the old `var style: Style?` field.
    private val _style = Style()
    private var _styleExplicitlySet = false

    var fill: ColorRGBa?
        get() = _style.fill
        set(value) { _style.fill = value; _styleExplicitlySet = true }

    var stroke: ColorRGBa?
        get() = _style.stroke
        set(value) { _style.stroke = value; _styleExplicitlySet = true }

    var strokeWeight: Double
        get() = _style.strokeWeight
        set(value) { _style.strokeWeight = value; _styleExplicitlySet = true }

    var size: Double
        get() = _style.size
        set(value) { _style.size = value; _styleExplicitlySet = true }

    var shape: Shape
        get() = _style.shape
        set(value) { _style.shape = value; _styleExplicitlySet = true }

    var lineCap: LineCap
        get() = _style.lineCap
        set(value) { _style.lineCap = value; _styleExplicitlySet = true }

    var lineJoin: LineJoin
        get() = _style.lineJoin
        set(value) { _style.lineJoin = value; _styleExplicitlySet = true }

    var miterLimit: Double
        get() = _style.miterLimit
        set(value) { _style.miterLimit = value; _styleExplicitlySet = true }

    // -- Advanced style config --------------------------------------------
    var styleByType: Map<String, Style> = emptyMap()
    var styleByFeature: ((Feature) -> Style?)? = null
    internal var styleByOptimizedFeature: ((OptimizedFeature) -> Style?)? = null

    // -- Helpers -----------------------------------------------------------

    /**
     * Returns the internal Style if any property was explicitly set,
     * or null if the user never touched style properties.
     * Used by the resolution chain to decide whether top-level
     * style should override StyleDefaults.
     */
    fun resolvedStyle(): Style? = if (_styleExplicitlySet) _style else null

    /**
     * Returns a snapshot for safe iteration during render.
     */
    fun snapshot(): GeoRenderConfig {
        val copy = GeoRenderConfig()
        copy.projection = projection
        if (_styleExplicitlySet) {
            copy.fill = _style.fill
            copy.stroke = _style.stroke
            copy.strokeWeight = _style.strokeWeight
            copy.size = _style.size
            copy.shape = _style.shape
            copy.lineCap = _style.lineCap
            copy.lineJoin = _style.lineJoin
            copy.miterLimit = _style.miterLimit
        }
        copy.styleByType = styleByType.toMap()
        copy.styleByFeature = styleByFeature
        copy.styleByOptimizedFeature = styleByOptimizedFeature
        return copy
    }

    companion object {
        operator fun invoke(block: GeoRenderConfig.() -> Unit): GeoRenderConfig {
            return GeoRenderConfig().apply(block)
        }
    }
}

/**
 * Resolve style for a feature using precedence chain:
 * 
 * 1. **Per-feature function** (`styleByFeature`) — Highest priority
 *    - Called for each feature, returns Style? 
 *    - Return null to fall through to next level
 * 
 * 2. **By-type map** (`styleByType`) — Medium priority
 *    - Keyed by geometry type: "Point", "LineString", "Polygon", etc.
 *    - Absent key falls through to next level
 * 
 * 3. **Global style** (`style`) — Low priority
 *    - Applied to all features if specified
 *    - null falls through to default
 * 
 * 4. **Geometry-type default** (`StyleDefaults.forGeometry`) — Fallback
 *    - Provides sensible defaults per geometry type
 *    - Always returns a valid Style
 * 
 * ## Example
 * ```kotlin
 * drawer.geo(source) {
 *     // Global style (lowest priority)
 *     style = Style { stroke = ColorRGBa.WHITE }
 *     
 *     // Type-based styling
 *     styleByType = mapOf(
 *         "Polygon" to Style { fill = ColorRGBa.RED }
 *     )
 *     
 *     // Per-feature styling (highest priority)
 *     styleByFeature = { feature ->
 *         when {
 *             feature.doubleProperty("pop") > 1_000_000 -> 
 *                 Style { stroke = ColorRGBa.YELLOW; strokeWeight = 3.0 }
 *             else -> null  // Fall through to styleByType/style
 *         }
 *     }
 * }
 * ```
 *
 * @param feature The feature to resolve style for
 * @param config The GeoRenderConfig containing style options
 * @return The resolved Style for the feature
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
    config.resolvedStyle()?.let { return it }
    
    // 4. Default
    return StyleDefaults.forGeometry(feature.geometry)
}
