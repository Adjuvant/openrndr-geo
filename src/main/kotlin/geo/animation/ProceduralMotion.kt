package geo.animation

import geo.core.Feature
import org.openrndr.math.Vector2

/**
 * Lightweight wrapper pairing a feature with computed animation delay.
 *
 * AnimationWrapper is used by stagger functions to attach computed delays
 * to features without mutating the Feature objects themselves. This maintains
 * immutability of geo data while enabling flexible animation timing.
 *
 * ## Usage Pattern
 * ```kotlin
 * val staggered = features.staggerByIndex(50)
 * staggered.forEach { wrapper ->
 *     delay(wrapper.delay)
 *     animateFeature(wrapper.feature)
 * }
 * ```
 *
 * @property feature The geo feature to animate
 * @property delay The computed delay in milliseconds before animation should start
 *
 * @see staggerByIndex Index-based sequential stagger
 * @see staggerByDistance Spatial ripple stagger from origin point
 *
 * @author Phase 05-animation - 05-03
 */
data class AnimationWrapper(
    val feature: Feature,
    val delay: Long
)

/**
 * Apply index-based stagger delays to a sequence of features.
 *
 * Creates sequential delays based on feature position in the sequence.
 * Useful for ordered reveals, cascading animations, and progressive loading effects.
 *
 * ## Example: Sequential Feature Reveal
 * ```kotlin
 * val features = loadGeoPackage("cities.gpkg").features.toList()
 *
 * // Each feature starts 50ms after the previous one
 * features.asSequence()
 *     .staggerByIndex(delayMs = 50)
 *     .forEach { wrapper ->
 *         println("${wrapper.feature.id}: starts at ${wrapper.delay}ms")
 *     }
 *
 * // Output: feature0: 0ms, feature1: 50ms, feature2: 100ms, ...
 * ```
 *
 * ## Example: Cascade Animation with GeoAnimator
 * ```kotlin
 * val staggered = features.asSequence().staggerByIndex(100)
 *
 * staggered.forEachIndexed { index, wrapper ->
 *     val animator = object : GeoAnimator() {
 *         var size = 5.0
 *     }
 *     animator.apply {
 *         // Use stagger delay for natural cascade effect
 *         Thread.sleep(wrapper.delay) // Or use timeline offset
 *         ::size.animate(20.0, 1000, Easing.CubicOut)
 *     }
 * }
 * ```
 *
 * ## Performance Characteristics
 * - **Time complexity:** O(n) where n = number of features
 * - **Memory allocation:** One AnimationWrapper per feature (lightweight)
 * - **Lazy evaluation:** Returns Sequence, computation deferred until consumed
 *
 * @param delayMs Delay between consecutive features in milliseconds (default: 50ms)
 * @return Sequence of AnimationWrappers with computed delays (0ms, 50ms, 100ms, ...)
 *
 * @see staggerByDistance For distance-based ripple effects
 * @see AnimationWrapper Pairs feature with computed delay
 *
 * @author Phase 05-animation - 05-03
 */
fun Sequence<Feature>.staggerByIndex(delayMs: Long = 50L): Sequence<AnimationWrapper> =
    mapIndexed { index, feature ->
        AnimationWrapper(
            feature = feature,
            delay = index * delayMs
        )
    }

/**
 * Apply spatial stagger delays based on distance from a geographic origin.
 *
 * Creates ripple-like delays where features farther from the origin start later.
 * Useful for earthquake shockwaves, explosion effects, and geographic wave animations.
 *
 * ## Example: Ripple Effect from City Center
 * ```kotlin
 * val cityCenter = Vector2(0.0, 0.0) // Origin point
 * val features = loadGeoPackage("buildings.gpkg").features.toList()
 *
 * // Buildings farther from center start later (10ms per unit distance)
 * val ripple = features.asSequence()
 *     .staggerByDistance(origin = cityCenter, factor = 10.0)
 *     .sortedBy { it.delay } // Sort by delay for ordered processing
 *
 * ripple.forEach { wrapper ->
 *     println("${wrapper.feature.id}: ${wrapper.delay}ms from center")
 * }
 * ```
 *
 * ## Example: Earthquake Shockwave Visualization
 * ```kotlin
 * // Epicenter at earthquake origin
 * val epicenter = Vector2(-122.4194, 37.7749) // San Francisco
 *
 * features.asSequence()
 *     .staggerByDistance(origin = epicenter, factor = 5.0)
 *     .forEach { wrapper ->
 *         val animator = animator()
 *         animator.apply {
 *             // Delay proportional to distance from epicenter
 *             // Closer features shake sooner
 *             val distanceDelay = wrapper.delay
 *
 *             // Animate color from white to red based on shockwave arrival
 *             ::progress.animate(1.0, 1000 + distanceDelay, Easing.CubicOut)
 *         }
 *     }
 * ```
 *
 * ## Distance Calculation
 * Uses Euclidean distance in the coordinate space of the origin Vector2.
 * For geographic coordinates, ensure origin uses the same CRS as features.
 *
 * ## Performance Characteristics
 * - **Time complexity:** O(n) where n = number of features
 * - **Memory allocation:** One AnimationWrapper per feature
 * - **Distance calculation:** O(1) per feature using Vector2.distanceTo()
 * - **Lazy evaluation:** Returns Sequence, computation deferred until consumed
 *
 * @param origin The geographic origin point from which to calculate distances
 * @param factor Milliseconds of delay per unit distance (default: 10.0ms per unit)
 * @return Sequence of AnimationWrappers with delays proportional to distance from origin
 *
 * @see staggerByIndex For index-based sequential delays
 * @see AnimationWrapper Pairs feature with computed delay
 * @see Vector2.distanceTo Calculates Euclidean distance between points
 *
 * @author Phase 05-animation - 05-03
 */
fun Sequence<Feature>.staggerByDistance(
    origin: Vector2,
    factor: Double = 10.0
): Sequence<AnimationWrapper> =
    map { feature ->
        val (centerX, centerY) = feature.geometry.boundingBox.center
        val featurePoint = Vector2(centerX, centerY)
        val distance = featurePoint.distanceTo(origin)
        val delay = (distance * factor).toLong()

        AnimationWrapper(
            feature = feature,
            delay = delay
        )
    }
