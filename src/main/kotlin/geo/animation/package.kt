@file:JvmName("AnimationPackage")
@file:Suppress("unused")

package geo.animation

// ============================================================================
// geo.animation Package-Level Wildcard Exports
// ============================================================================
// This file provides wildcard exports for the geo.animation package.
// Import with: import geo.animation.*
//
// Exports all animation-related APIs:
// - GeoAnimator and FeatureAnimator
// - Tweening utilities
// - Easing extensions
// - Stagger animation helpers
// ============================================================================

// ----------------------------------------------------------------------------
// Core Animation Classes
// ----------------------------------------------------------------------------
// Re-export GeoAnimator and FeatureAnimator

/**
 * Global animation controller for geo visualizations with Animatable lifecycle.
 *
 * GeoAnimator provides a singleton-based animation system that integrates with OpenRNDR's
 * built-in Animatable API. It manages per-frame animation updates.
 *
 * ## Usage Pattern
 * ```kotlin
 * fun main() = application {
 *     program {
 *         val animator = animator()
 *         extend {
 *             animator.updateAnimation()
 *             drawer.circle(animator.x, animator.y, 100.0)
 *         }
 *     }
 * }
 * ```
 */
public typealias GeoAnimatorExport = GeoAnimator

/**
 * Data class pairing a feature with its dedicated animator and animation delay.
 *
 * FeatureAnimator combines a geographic feature with a GeoAnimator instance to enable
 * independent, staggered animations for each feature.
 */
public typealias FeatureAnimatorExport = FeatureAnimator

/**
 * Extension function to access the global GeoAnimator singleton.
 *
 * @receiver The OpenRNDR Program context
 * @return The GeoAnimator singleton instance
 */
public typealias AnimatorFunc = org.openrndr.Program.() -> GeoAnimator

// ----------------------------------------------------------------------------
// Tweening Utilities
// ----------------------------------------------------------------------------
// Re-export Tweening utilities

/**
 * Property tweening primitives for geo animations.
 *
 * This package provides documentation and examples for OpenRNDR's built-in property
 * animation system via Kotlin property references.
 */
public typealias TweeningExport = Unit  // Marker for documentation

/**
 * Animation state tracking for custom tweening scenarios.
 *
 * Provides a data class for tracking animation metadata when building
 * custom animation systems on top of OpenRNDR's Animatable.
 */
public typealias AnimationExport<T> = Animation<T>

/**
 * Extension method for GeoAnimator to process custom animations.
 */
public typealias AnimateAnimationsFunc = GeoAnimator.() -> Unit

// ----------------------------------------------------------------------------
// Easing Extensions
// ----------------------------------------------------------------------------
// Re-export all easing convenience functions

/**
 * Linear easing - constant velocity, no acceleration.
 */
public val linearEase: () -> org.openrndr.animatable.easing.Easing = ::linear

/**
 * No easing - linear, constant velocity.
 */
public val noEase: () -> org.openrndr.animatable.easing.Easing = ::none

/**
 * Ease In-Out Cubic - smooth acceleration and deceleration.
 *
 * Default recommendation: starts slow, speeds up in middle, slows at end.
 */
public val easeInOutCubic: () -> org.openrndr.animatable.easing.Easing = ::easeInOut

/**
 * Ease Out Cubic - deceleration only.
 *
 * Starts fast, decelerates to smooth stop.
 */
public val easeOutCubic: () -> org.openrndr.animatable.easing.Easing = ::easeOut

/**
 * Ease In Cubic - acceleration only.
 *
 * Starts slow, accelerates to end.
 */
public val easeInCubic: () -> org.openrndr.animatable.easing.Easing = ::easeIn

/**
 * Sine In-Out easing.
 *
 * Gentle S-curve using sinusoidal interpolation.
 */
public val sineInOutEase: () -> org.openrndr.animatable.easing.Easing = ::sineInOut

/**
 * Quadratic In-Out easing.
 *
 * Less aggressive than cubic curves. Good for subtle, gentle motion.
 */
public val quadInOutEase: () -> org.openrndr.animatable.easing.Easing = ::quadInOut

/**
 * Quartic In-Out easing.
 *
 * More aggressive than cubic curves. Strong acceleration/deceleration.
 */
public val quartInOutEase: () -> org.openrndr.animatable.easing.Easing = ::quartInOut

/**
 * Quadratic Out easing.
 *
 * Gentle deceleration, less pronounced than cubic.
 */
public val quadOutEase: () -> org.openrndr.animatable.easing.Easing = ::quadOut

/**
 * Quadratic In easing.
 *
 * Gentle acceleration, less pronounced than cubic.
 */
public val quadInEase: () -> org.openrndr.animatable.easing.Easing = ::quadIn

/**
 * Sine Out easing.
 *
 * Gentle deceleration using sinusoidal curve.
 */
public val sineOutEase: () -> org.openrndr.animatable.easing.Easing = ::sineOut

/**
 * Sine In easing.
 *
 * Gentle acceleration using sinusoidal curve.
 */
public val sineInEase: () -> org.openrndr.animatable.easing.Easing = ::sineIn

/**
 * Cubic In-Out easing (explicit).
 */
public val cubicInOutEase: () -> org.openrndr.animatable.easing.Easing = ::cubicInOut

/**
 * Cubic Out easing (explicit).
 */
public val cubicOutEase: () -> org.openrndr.animatable.easing.Easing = ::cubicOut

/**
 * Cubic In easing (explicit).
 */
public val cubicInEase: () -> org.openrndr.animatable.easing.Easing = ::cubicIn

// ----------------------------------------------------------------------------
// Stagger Animation Helpers
// ----------------------------------------------------------------------------
// Re-export stagger functions from FeatureAnimator.kt

/**
 * Create staggered FeatureAnimator instances based on index position.
 *
 * Creates a sequence of FeatureAnimator instances with sequentially increasing delay
 * values. The delay is computed as `index * delayMs`.
 *
 * @param delayMs Delay between consecutive features in milliseconds (default: 50ms)
 * @param animatorFactory Factory function to create GeoAnimator instances for each feature
 * @return Sequence of FeatureAnimator instances with computed delay values
 */
// Stagger functions are extension functions on Sequence<Feature> - available via wildcard import
// Use: features.asSequence().staggerByIndex(delayMs = 50) { GeoAnimator() }

/**
 * Create staggered FeatureAnimator instances based on distance from an origin point.
 *
 * Creates a sequence of FeatureAnimator instances with delay values proportional
 * to their distance from the specified origin, creating a ripple or shockwave effect.
 *
 * @param origin The geographic origin point from which to calculate distances
 * @param factor Milliseconds of delay per unit distance (default: 10.0ms per unit)
 * @param animatorFactory Factory function to create GeoAnimator instances for each feature
 * @return Sequence of FeatureAnimator instances with distance-based delay values
 */
// Use: features.asSequence().staggerByDistance(origin = center, factor = 10.0) { GeoAnimator() }

// ----------------------------------------------------------------------------
// Procedural Motion
// ----------------------------------------------------------------------------
// Re-export ProceduralMotion if available

// ProceduralMotion.kt contents are exported automatically

// ----------------------------------------------------------------------------
// Interpolators
// ----------------------------------------------------------------------------
// Re-export interpolator utilities

// Interpolators from interpolators/ subdirectory are available via wildcard import
