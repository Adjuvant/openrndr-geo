package geo.animation

import org.openrndr.animatable.easing.Easing

/**
 * Convenience functions for OpenRNDR's built-in [Easing] enum.
 *
 * OpenRNDR's openrndr-animatable provides 13 easing enum constants:
 * - **None**: Linear, constant velocity (equivalent to `linear()`)
 * - **SineIn**, **SineOut**, **SineInOut**: Sinusoidal curves
 * - **QuadIn**, **QuadOut**, **QuadInOut**: Quadratic curves
 * - **CubicIn**, **CubicOut**, **CubicInOut**: Cubic curves
 * - **QuartIn**, **QuartOut**, **QuartInOut**: Quartic curves
 *
 * These top-level functions provide a more convenient DSL-style API
 * for accessing the most commonly used easing functions.
 *
 * ## Usage with GeoAnimator
 * ```kotlin
 * import geo.animation.*
 *
 * fun main() = application {
 *     program {
 *         val animator = animator()
 *
 *         animator.apply {
 *             // Use convenience easing functions
 *             ::x.animate(targetX, 2000, easeInOut())
 *             ::y.animate(targetY, 2000, easeOut())
 *         }
 *
 *         extend {
 *             animator.updateAnimation()
 *             drawer.circle(animator.x, animator.y, 100.0)
 *         }
 *     }
 * }
 * ```
 *
 * ## Easing Characteristics
 *
 * | Function | Behavior | Best For |
 * |----------|----------|----------|
 * | `linear()` / `none()` | Constant velocity | Mechanical motion |
 * | `easeIn()` | Accelerates from zero | Building momentum, exits |
 * | `easeOut()` | Decelerates to stop | Natural settling, entrances |
 * | `easeInOut()` | Accelerate then decelerate | Most UI animations (default) |
 * | `sineInOut()` | Gentle S-curve | Subtle, organic motion |
 * | `quadInOut()` | Less aggressive than cubic | Gentle UI animations |
 * | `quartInOut()` | More aggressive than cubic | Emphatic transitions |
 *
 * ## Implementation Note
 * These are convenience wrappers that return OpenRNDR's Easing enum values.
 * They do NOT implement custom easing math - they simply provide a
 * more discoverable, DSL-friendly API for the existing OpenRNDR functionality.
 *
 * @see org.openrndr.animatable.easing.Easing OpenRNDR's easing enum
 * @see GeoAnimator Animation controller that uses these easing functions
 *
 * @author Phase 05-animation - 05-01
 */

/**
 * Linear easing - constant velocity, no acceleration.
 *
 * Animation progresses at a constant rate throughout the duration.
 * Best for: Mechanical motion, UI transitions where no "feel" is needed.
 *
 * Alias for [none()].
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, linear())
 * ```
 *
 * @return Easing.None
 */
fun linear(): Easing = Easing.None

/**
 * No easing - linear, constant velocity.
 *
 * Same as [linear()]. Use whichever naming feels more natural for your code.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, none())
 * ```
 *
 * @return Easing.None
 */
fun none(): Easing = Easing.None

/**
 * Ease In-Out Cubic - smooth acceleration and deceleration.
 *
 * Default recommendation: starts slow, speeds up in middle, slows at end.
 * Best for: Most UI animations, natural-feeling motion.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, easeInOut())
 * ```
 *
 * @return Easing.CubicInOut
 */
fun easeInOut(): Easing = Easing.CubicInOut

/**
 * Ease Out Cubic - deceleration only.
 *
 * Starts fast, decelerates to smooth stop.
 * Best for: Elements entering screen, natural settling motion.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, easeOut())
 * ```
 *
 * @return Easing.CubicOut
 */
fun easeOut(): Easing = Easing.CubicOut

/**
 * Ease In Cubic - acceleration only.
 *
 * Starts slow, accelerates to end.
 * Best for: Elements leaving screen, building momentum.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, easeIn())
 * ```
 *
 * @return Easing.CubicIn
 */
fun easeIn(): Easing = Easing.CubicIn

/**
 * Sine In-Out easing.
 *
 * Gentle S-curve using sinusoidal interpolation.
 * More subtle than cubic curves.
 * Best for: Organic, natural-feeling motion.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, sineInOut())
 * ```
 *
 * @return Easing.SineInOut
 */
fun sineInOut(): Easing = Easing.SineInOut

/**
 * Quadratic In-Out easing.
 *
 * Less aggressive than cubic curves. Good for subtle, gentle motion.
 * Best for: Delicate UI transitions where cubic feels too strong.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, quadInOut())
 * ```
 *
 * @return Easing.QuadInOut
 */
fun quadInOut(): Easing = Easing.QuadInOut

/**
 * Quartic In-Out easing.
 *
 * More aggressive than cubic curves. Strong acceleration/deceleration.
 * Best for: Emphatic transitions, drawing attention.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, quartInOut())
 * ```
 *
 * @return Easing.QuartInOut
 */
fun quartInOut(): Easing = Easing.QuartInOut

/**
 * Quadratic Out easing.
 *
 * Gentle deceleration, less pronounced than cubic.
 * Best for: Subtle entrance animations.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, quadOut())
 * ```
 *
 * @return Easing.QuadOut
 */
fun quadOut(): Easing = Easing.QuadOut

/**
 * Quadratic In easing.
 *
 * Gentle acceleration, less pronounced than cubic.
 * Best for: Subtle exit animations.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, quadIn())
 * ```
 *
 * @return Easing.QuadIn
 */
fun quadIn(): Easing = Easing.QuadIn

/**
 * Sine Out easing.
 *
 * Gentle deceleration using sinusoidal curve.
 * Best for: Very subtle settling motion.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, sineOut())
 * ```
 *
 * @return Easing.SineOut
 */
fun sineOut(): Easing = Easing.SineOut

/**
 * Sine In easing.
 *
 * Gentle acceleration using sinusoidal curve.
 * Best for: Very subtle acceleration.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, sineIn())
 * ```
 *
 * @return Easing.SineIn
 */
fun sineIn(): Easing = Easing.SineIn

/**
 * Cubic In-Out easing (explicit).
 *
 * Same as [easeInOut()]. Available for explicit naming when needed.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, cubicInOut())
 * ```
 *
 * @return Easing.CubicInOut
 */
fun cubicInOut(): Easing = Easing.CubicInOut

/**
 * Cubic Out easing (explicit).
 *
 * Same as [easeOut()]. Available for explicit naming when needed.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, cubicOut())
 * ```
 *
 * @return Easing.CubicOut
 */
fun cubicOut(): Easing = Easing.CubicOut

/**
 * Cubic In easing (explicit).
 *
 * Same as [easeIn()]. Available for explicit naming when needed.
 *
 * ```kotlin
 * ::x.animate(targetX, 2000, cubicIn())
 * ```
 *
 * @return Easing.CubicIn
 */
fun cubicIn(): Easing = Easing.CubicIn
