package geo.animation

import geo.Feature
import org.openrndr.math.Vector2

/**
 * Data class pairing a feature with its dedicated animator and animation delay.
 *
 * FeatureAnimator combines a geographic feature with a GeoAnimator instance to enable
 * independent, staggered animations for each feature. The [delay] property contains
 * the computed stagger delay that should be passed to animation functions.
 *
 * ## Usage Pattern
 * ```kotlin
 * val featureAnimators = features.asSequence()
 *     .staggerByIndex(delayMs = 50) { GeoAnimator() }
 *     .onEach { fa ->
 *         fa.animator.size = 0.0
 *         // Pass fa.delay as the predelay parameter to animate()
 *         fa.animator::size.animate(20.0, 1000, Easing.CubicOut, fa.delay)
 *     }
 *     .toList()
 *
 * // In render loop
 * featureAnimators.forEach { fa ->
 *     fa.animator.updateAnimation()
 *     val point = fa.feature.geometry as Point
 *     val screenPoint = point.toScreen(projection)
 *     drawer.circle(screenPoint.x, screenPoint.y, fa.animator.size)
 * }
 * ```
 *
 * @property feature The geographic feature to animate
 * @property animator The GeoAnimator instance managing this feature's animations
 * @property delay The computed stagger delay in milliseconds for this feature.
 *                 Pass this to animate() as the predelay parameter.
 *
 * @see GeoAnimator Base animator class providing animation infrastructure
 * @see staggerByIndex Create staggered FeatureAnimator sequence by index
 * @see staggerByDistance Create staggered FeatureAnimator sequence by distance
 *
 * @author Phase quick - Plan 4
 */
data class FeatureAnimator(
    val feature: Feature,
    val animator: GeoAnimator,
    val delay: Long = 0L
)

/**
 * Create staggered FeatureAnimator instances based on index position.
 *
 * Creates a sequence of FeatureAnimator instances with sequentially increasing delay
 * values. The delay is computed as `index * delayMs` and stored in [FeatureAnimator.delay]
 * for use when configuring animations.
 *
 * ## Example: Sequential Feature Animation
 * ```kotlin
 * val featureAnimators = data.features.take(50)
 *     .asSequence()
 *     .staggerByIndex(delayMs = 50) { GeoAnimator() }
 *     .onEach { fa ->
 *         fa.animator.size = 0.0
 *         // Use fa.delay for non-blocking stagger delay
 *         fa.animator::size.animate(5.0, 1000, Easing.CubicOut, fa.delay)
 *     }
 *     .toList()
 *
 * // In render loop
 * featureAnimators.forEach { fa ->
 *     fa.animator.updateAnimation()
 *     val point = fa.feature.geometry as Point
 *     val screenPoint = point.toScreen(projection)
 *     drawer.circle(screenPoint.x, screenPoint.y, fa.animator.size)
 * }
 * ```
 *
 * ## Example: Custom Animator Factory
 * ```kotlin
 * val animators = features.asSequence()
 *     .staggerByIndex(delayMs = 100) {
 *         GeoAnimator().apply {
 *             // Configure initial animator state
 *             size = 10.0
 *             progress = 0.0
 *         }
 *     }
 *     .toList()
 * ```
 *
 * ## Performance Characteristics
 * - **Time complexity:** O(n) where n = number of features
 * - **Memory allocation:** One FeatureAnimator per feature (lightweight)
 * - **Lazy evaluation:** Returns Sequence, computation deferred until consumed
 * - **Delay computation:** Computed during iteration, accessible via FeatureAnimator.delay
 *
 * @param delayMs Delay between consecutive features in milliseconds (default: 50ms)
 * @param animatorFactory Factory function to create GeoAnimator instances for each feature.
 *                        Called once per feature to create independent animator instances.
 * @return Sequence of FeatureAnimator instances with computed delay values
 *
 * @see staggerByDistance For distance-based ripple effects
 * @see FeatureAnimator Pairs feature with dedicated animator and delay
 *
 * @author Phase quick - Plan 4
 */
fun Sequence<Feature>.staggerByIndex(
    delayMs: Long = 50L,
    animatorFactory: () -> GeoAnimator = { GeoAnimator() }
): Sequence<FeatureAnimator> =
    mapIndexed { index, feature ->
        val animator = animatorFactory()
        val delay = index * delayMs
        FeatureAnimator(feature, animator, delay)
    }

/**
 * Create staggered FeatureAnimator instances based on distance from an origin point.
 *
 * Creates a sequence of FeatureAnimator instances with delay values proportional
 * to their distance from the specified origin. Features farther from the origin
 * have larger delay values, creating a ripple or shockwave effect.
 *
 * ## Example: Ripple Effect from City Center
 * ```kotlin
 * val cityCenter = Vector2(0.0, 0.0)
 * val featureAnimators = buildings.asSequence()
 *     .staggerByDistance(origin = cityCenter, factor = 10.0) { GeoAnimator() }
 *     .onEach { fa ->
 *         fa.animator.size = 0.0
 *         // Use fa.delay for distance-based stagger
 *         fa.animator::size.animate(20.0, 1000, Easing.CubicOut, fa.delay)
 *     }
 *     .toList()
 * ```
 *
 * ## Example: Earthquake Shockwave from Epicenter
 * ```kotlin
 * val epicenter = Vector2(-122.4194, 37.7749) // San Francisco
 * val featureAnimators = features.asSequence()
 *     .staggerByDistance(origin = epicenter, factor = 5.0) { GeoAnimator() }
 *     .onEach { fa ->
 *         // Closer features start shaking sooner (smaller delay)
 *         fa.animator.progress = 0.0
 *         fa.animator::progress.animate(1.0, 2000, Easing.CubicOut, fa.delay)
 *     }
 *     .toList()
 * ```
 *
 * ## Distance Calculation
 * Uses Euclidean distance from the feature's bounding box center to the origin point.
 * Ensure the origin uses the same coordinate space as the features for accurate results.
 *
 * ## Performance Characteristics
 * - **Time complexity:** O(n) where n = number of features
 * - **Memory allocation:** One FeatureAnimator per feature
 * - **Distance calculation:** O(1) per feature using bounding box center
 * - **Lazy evaluation:** Returns Sequence, computation deferred until consumption
 *
 * @param origin The geographic origin point from which to calculate distances
 * @param factor Milliseconds of delay per unit distance (default: 10.0ms per unit)
 * @param animatorFactory Factory function to create GeoAnimator instances for each feature.
 *                        Called once per feature to create independent animator instances.
 * @return Sequence of FeatureAnimator instances with distance-based delay values
 *
 * @see staggerByIndex For index-based sequential delays
 * @see FeatureAnimator Pairs feature with dedicated animator and delay
 * @see Vector2.distanceTo Calculates Euclidean distance between points
 *
 * @author Phase quick - Plan 4
 */
fun Sequence<Feature>.staggerByDistance(
    origin: Vector2,
    factor: Double = 10.0,
    animatorFactory: () -> GeoAnimator = { GeoAnimator() }
): Sequence<FeatureAnimator> =
    map { feature ->
        val (centerX, centerY) = feature.geometry.boundingBox.center
        val featurePoint = Vector2(centerX, centerY)
        val distance = featurePoint.distanceTo(origin)
        val delay = (distance * factor).toLong()

        val animator = animatorFactory()
        FeatureAnimator(feature, animator, delay)
    }
