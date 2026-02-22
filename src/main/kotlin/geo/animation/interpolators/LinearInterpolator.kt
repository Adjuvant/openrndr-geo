package geo.animation.interpolators

import org.openrndr.math.Vector2

/**
 * Linear interpolation for screen coordinates.
 *
 * Provides fast, simple interpolation between projected screen coordinates.
 * This is the default interpolator for performance, but may show "straight line"
 * artifacts for long-distance geographic paths (use HaversineInterpolator for accuracy).
 *
 * ## Usage
 * ```kotlin
 * val from = Vector2(100.0, 100.0)
 * val to = Vector2(300.0, 300.0)
 * val midpoint = linearInterpolate(from, to, 0.5)  // Vector2(200.0, 200.0)
 * ```
 *
 * ## Performance vs Accuracy Tradeoff
 * - Linear: Fast, sufficient for local maps and screen-space animations
 * - Haversine: Accurate, required for long-distance great-circle paths (>100km)
 *
 * @param from Starting position as Vector2
 * @param to Target position as Vector2
 * @param t Progress from 0.0 (start) to 1.0 (complete)
 * @return Interpolated position as Vector2
 *
 * @see haversineInterpolate For great-circle interpolation on Earth's surface
 * @author Phase 05-animation - 05-02
 */
fun linearInterpolate(from: Vector2, to: Vector2, t: Double): Vector2 {
    return from + (to - from) * t
}
