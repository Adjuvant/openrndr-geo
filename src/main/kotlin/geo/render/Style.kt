package geo.render

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin

/**
 * Style configuration for rendering geo primitives.
 *
 * Mutable data class optimized for zero-allocation performance in real-time animation.
 * Properties can be mutated directly without creating new objects.
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
 *
 * ## Animation Performance
 * For smooth framerates in animation, mutate properties rather than creating new Style objects:
 * ```kotlin
 * val style = Style { size = 5.0 }
 * animate {
 *     style.size += 0.1  // Zero-allocation mutation
 *     drawPoint(drawer, x, y, style)
 * }
 * ```
 *
 * @property fill Fill color for enclosed shapes (null = no fill)
 * @property stroke Stroke color for outlines (null = no stroke)
 * @property strokeWeight Width of stroke lines in pixels
 * @property size Point marker size in pixels (diameter for circles, side length for squares/triangles)
 * @property shape Point marker shape (Circle, Square, Triangle)
 * @property lineCap How line ends are rendered (BUTT, ROUND, SQUARE)
 * @property lineJoin How line corners are joined (MITER, ROUND, BEVEL)
 * @property miterLimit Maximum miter length for miter joins
 */
data class Style(
    var fill: ColorRGBa? = null,
    var stroke: ColorRGBa? = ColorRGBa.WHITE,
    var strokeWeight: Double = 1.0,
    var size: Double = 5.0,
    var shape: Shape = Shape.Circle,
    var lineCap: LineCap = LineCap.BUTT,
    var lineJoin: LineJoin = LineJoin.MITER,
    var miterLimit: Double = 4.0
) {
    companion object {
        /**
         * Create a Style using DSL syntax.
         *
         * Enables type-safe builder pattern:
         * ```kotlin
         * val style = Style {
         *     fill = ColorRGBa.RED
         *     size = 10.0
         * }
         * ```
         *
         * @param block Configuration lambda applied to new Style instance
         * @return Configured Style instance
         */
        operator fun invoke(block: Style.() -> Unit): Style {
            val style = Style()
            style.block()
            return style
        }

        /**
         * Create a transparent style for invisible/hidden geometry.
         *
         * Useful for creating "ghost" features or placeholders.
         *
         * @param alpha Transparency level (0.0 = fully transparent, 1.0 = fully opaque)
         * @return Style with transparent fill and stroke
         */
        fun transparent(alpha: Double = 0.0) = Style().apply {
            fill = ColorRGBa.WHITE.withAlpha(alpha)
            stroke = ColorRGBa.WHITE.withAlpha(alpha)
        }
    }
}

/**
 * Extension function for ColorRGBa to create transparent version.
 *
 * @param alpha Alpha value (0.0 = transparent, 1.0 = opaque)
 * @return Color with specified alpha
 */
fun ColorRGBa.withAlpha(alpha: Double): ColorRGBa =
    ColorRGBa(r, g, b, alpha)
