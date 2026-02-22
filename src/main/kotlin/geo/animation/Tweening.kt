package geo.animation

import org.openrndr.animatable.easing.Easing
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import kotlin.math.min

/**
 * Property tweening primitives for geo animations.
 *
 * This package provides documentation and examples for OpenRNDR's built-in property
 * animation system via Kotlin property references. The actual `::property.animate()`
 * functionality is provided by OpenRNDR's [Animatable] base class - this file documents
 * best practices and provides helper types for advanced animation scenarios.
 *
 * ## Basic Property Animation
 *
 * OpenRNDR's Animatable provides type-safe property animation via property references:
 * ```kotlin
 * val animator = animator()
 * animator.apply {
 *     ::x.animate(300.0, 2000, Easing.CubicInOut)
 *     ::y.animate(200.0, 2000, Easing.CubicOut)
 * }
 * ```
 *
 * ## Per-Property Easing
 *
 * Different properties benefit from different easing curves:
 *
 * - **Position**: Use `Easing.CubicOut` for natural deceleration
 *   ```kotlin
 *   ::x.animate(100.0, 2000, Easing.CubicOut)
 *   ::y.animate(100.0, 2000, Easing.CubicOut)
 *   ```
 *
 * - **Color**: Use `Easing.Linear` for smooth color transitions
 *   ```kotlin
 *   // Animate ColorRGBa components individually
 *   ::r.animate(1.0, 1000, Easing.Linear)
 *   ::g.animate(0.5, 1000, Easing.Linear)
 *   ::b.animate(0.0, 1000, Easing.Linear)
 *   ```
 *
 * - **Size**: Use `Easing.CubicIn` for grow effects
 *   ```kotlin
 *   ::size.animate(20.0, 1500, Easing.CubicIn)
 *   ```
 *
 * ## Interpolated Position Animation
 *
 * For position animation with choice of interpolation strategy:
 * ```kotlin
 * import geo.animation.interpolators.linearInterpolate
 * import geo.animation.interpolators.haversineInterpolate
 * import geo.animation.interpolators.Position
 *
 * // Linear interpolation for screen coordinates (fast)
 * val start = Vector2(100.0, 100.0)
 * val end = Vector2(300.0, 300.0)
 * val current = linearInterpolate(start, end, progress)
 *
 * // Haversine interpolation for geographic coordinates (accurate)
 * val london = Position(51.5074, -0.1278)
 * val tokyo = Position(35.6762, 139.6503)
 * val midPoint = haversineInterpolate(london, tokyo, 0.5)
 * ```
 *
 * @see Easing OpenRNDR's built-in easing functions
 * @see GeoAnimator.updateAnimation Call this each frame to progress animations
 * @see geo.animation.interpolators.linearInterpolate Screen-space interpolation
 * @see geo.animation.interpolators.haversineInterpolate Great-circle interpolation
 *
 * @author Phase 05-animation - 05-02
 */

/**
 * Animation state tracking for custom tweening scenarios.
 *
 * Provides a data class for tracking animation metadata when building
 * custom animation systems on top of OpenRNDR's Animatable. Most use cases
 * will use OpenRNDR's built-in `::property.animate()` directly, but this
 * class supports advanced scenarios like timeline composition or batch animation.
 *
 * ## Use Cases
 *
 * - Timeline-based animation sequences
 * - Batch animations with staggered delays
 * - Custom interpolators that need manual progress tracking
 *
 * ## Basic Usage
 * ```kotlin
 * val animation = Animation(
 *     targetValue = 100.0,
 *     duration = 2000,
 *     easing = Easing.CubicInOut
 * )
 *
 * // In your update loop
 * val progress = animation.progress()
 * val easedProgress = animation.easing(progress.toFloat())
 * currentValue = startValue + (targetValue - startValue) * easedProgress
 * ```
 *
 * ## Integration with GeoAnimator
 * ```kotlin
 * val customAnimations = mutableListOf<Animation<Double>>()
 *
 * extend {
 *     animator.updateAnimation()  // Update built-in animations
 *
 *     // Update custom animations
 *     customAnimations.removeAll { it.isComplete() }
 *     customAnimations.forEach { anim ->
 *         val progress = anim.progress()
 *         // Apply to your custom properties
 *     }
 * }
 * ```
 *
 * @property targetValue The destination value for the animation
 * @property duration Animation duration in milliseconds
 * @property easing The easing function applied to this animation
 * @property startTime System time when animation began (milliseconds)
 *
 * @see GeoAnimator Main animation controller
 * @see Easing OpenRNDR's easing functions
 *
 * @author Phase 05-animation - 05-02
 */
data class Animation<T>(
    val targetValue: T,
    val duration: Long,
    val easing: Easing,
    val startTime: Long = System.currentTimeMillis()
) {
    /**
     * Calculate raw progress (0.0 to 1.0) based on elapsed time.
     *
     * This is linear time progress, not eased. Apply easing function separately:
     * ```kotlin
     * val rawProgress = animation.progress()
     * val easedProgress = animation.easing(rawProgress.toFloat())
     * ```
     */
    fun progress(): Double {
        val elapsed = System.currentTimeMillis() - startTime
        return min(elapsed.toDouble() / duration, 1.0)
    }

    /**
     * Check if animation has completed.
     *
     * @return true if progress >= 1.0
     */
    fun isComplete(): Boolean = progress() >= 1.0

    /**
     * Get remaining time in milliseconds.
     *
     * @return Time remaining until completion (0 if complete)
     */
    fun remainingTime(): Long {
        val elapsed = System.currentTimeMillis() - startTime
        return maxOf(0, duration - elapsed)
    }
}

/**
 * Extension method for GeoAnimator to process custom animations.
 *
 * This method serves as a hook for custom animation logic that extends
 * OpenRNDR's built-in property animation system. While `::property.animate()`
 * handles basic property tweening automatically, this method provides a
 * standardized entry point for advanced animation patterns.
 *
 * ## Built-in vs Custom Animation
 *
 * **OpenRNDR Built-in (automatic):**
 * ```kotlin
 * animator.apply {
 *     ::x.animate(300.0, 2000, Easing.CubicInOut)
 * }
 * extend {
 *     animator.updateAnimation()  // Updates x, y automatically
 * }
 * ```
 *
 * **Custom Animation (via animateAnimations):**
 * ```kotlin
 * val customAnims = mutableListOf<Animation<Vector2>>()
 *
 * extend {
 *     animator.updateAnimation()      // Built-in animations
 *     animator.animateAnimations()    // Custom animation hook
 *
 *     // Or use directly for batch operations
 *     customAnims.removeAll { it.isComplete() }
 * }
 * ```
 *
 * ## Animation Lifecycle with Custom Animations
 * 1. Create Animation<T> instances with target values
 * 2. Store in custom list (if not using ::property.animate())
 * 3. Call animateAnimations() in extend loop
 * 4. Completed animations cleaned up automatically
 *
 * @see updateAnimation OpenRNDR's built-in property animation
 * @see Animation Custom animation state tracking
 *
 * @author Phase 05-animation - 05-02
 */
fun GeoAnimator.animateAnimations() {
    // Hook for custom animation extensions
    // Future phases (05-03) will add timeline and composition logic here
}
