package geo.render

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin

/**
 * Default style configurations for different geometry types.
 *
 * Provides sensible defaults per geometry type that users can override.
 * System default provides base values, user style merges with override precedence.
 *
 * ## Usage
 * ```kotlin
 * // Use defaults directly
 * drawPoint(point)  // Uses defaultPointStyle
 *
 * // Override specific properties
 * drawPoint(point, Style { size = 10.0 })  // Keeps default colors
 * ```
 *
 * ## Merge Pattern
 * User style takes precedence on conflicts, defaults fill gaps:
 * ```kotlin
 * val merged = mergeStyles(defaultPointStyle, userStyle)
 * ```
 */
object StyleDefaults {

    /**
     * Default style for Point geometry.
     *
     * - White fill and stroke for visibility
     * - 1.0 pixel stroke weight
     * - 5.0 pixel size (diameter for circles, side for squares)
     * - Circular shape
     */
    val defaultPointStyle = Style(
        fill = ColorRGBa.WHITE,
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        size = 5.0,
        shape = Shape.Circle
    )

    /**
     * Default style for LineString geometry.
     *
     * - No fill (lines don't have fill)
     * - White stroke for visibility
     * - 1.0 pixel stroke weight
     * - Butt line caps and miter joins
     */
    val defaultLineStyle = Style(
        fill = null,
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        lineCap = LineCap.BUTT,
        lineJoin = LineJoin.MITER
    )

    /**
     * Default style for Polygon geometry.
     *
     * - Fully transparent fill (outline only by default)
     * - White stroke for visibility
     * - 1.0 pixel stroke weight
     * - Butt line caps and miter joins
     */
    val defaultPolygonStyle = Style(
        fill = ColorRGBa.WHITE.withAlpha(0.0), // transparent fill
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        lineCap = LineCap.BUTT,
        lineJoin = LineJoin.MITER
    )
}

/**
 * Merge system default style with user-provided style.
 *
 * User values take precedence on conflicts, defaults fill gaps.
 * Preserves zero-allocation by returning new Style only when needed.
 *
 * ## Behavior
 * - If userStyle is null: returns copy of default
 * - If userStyle has non-null value: uses user value
 * - If userStyle has null value: uses default value
 * - Numeric values: 0.0 is treated as "use default" for some properties
 *
 * @param default System default style for geometry type
 * @param user User-provided style (may be null or partial)
 * @return Merged style with user preferences applied
 */
fun mergeStyles(default: Style, user: Style?): Style {
    if (user == null) return default

    return Style().apply {
        fill = user.fill ?: default.fill
        stroke = user.stroke ?: default.stroke
        strokeWeight = if (user.strokeWeight != 0.0) user.strokeWeight else default.strokeWeight
        size = if (user.size != 0.0) user.size else default.size
        shape = user.shape
        lineCap = user.lineCap
        lineJoin = user.lineJoin
        miterLimit = if (user.miterLimit != 0.0) user.miterLimit else default.miterLimit
    }
}
