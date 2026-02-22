package geo.animation.composition

import geo.animation.GeoAnimator
import org.openrndr.animatable.Animatable

/**
 * Timeline-based animation composition for coordinated multi-step animations.
 *
 * GeoTimeline provides explicit timing control for animation sequences via an
 * offset-based system. Each animation added to the timeline can specify when
 * it starts relative to the timeline's start or previous animations.
 *
 * ## Timeline DSL Pattern
 * ```kotlin
 * val timeline = GeoTimeline {
 *     // Animation starts immediately (offset = 0)
 *     add(firstAnimation)
 *
 *     // Animation starts 200ms after timeline start
 *     add(secondAnimation, offset = 200)
 *
 *     // Animation starts 500ms after timeline start
 *     add(thirdAnimation, offset = 500)
 * }
 *
 * // In extend block
 * extend {
 *     timeline.update()  // Updates all timeline animations
 * }
 * ```
 *
 * ## Integration with GeoAnimator
 * ```kotlin
 * val timeline = GeoTimeline {
 *     val animator = GeoAnimator()
 *     animator.apply {
 *         ::x.animate(300.0, 1000, Easing.CubicInOut)
 *     }
 *     add(animator, offset = 0)
 *
 *     val second = GeoAnimator()
 *     second.apply {
 *         ::y.animate(200.0, 1000, Easing.CubicOut)
 *     }
 *     add(second, offset = 500)  // Starts 500ms after first
 * }
 *
 * extend {
 *     timeline.update()
 * }
 * ```
 *
 * ## Use Cases
 * - **Sequential animations:** Chain animations with explicit offsets
 * - **Coordinated effects:** Synchronize multiple feature animations
 * - **Timeline scrubbing:** Pause/resume entire timeline via update() control
 *
 * @property entries Internal list of timeline entries with computed delays
 * @see ChainedAnimationBuilder For fluent chain-based composition alternative
 * @see GeoAnimator For individual animation controller
 *
 * @author Phase 05-animation - 05-03
 */
class GeoTimeline(init: (GeoTimeline.() -> Unit) = {}) {

    /**
     * Internal entry tracking animation with its timeline offset.
     *
     * @property animation The Animatable to update (typically a GeoAnimator)
     * @property offset Milliseconds after timeline start when animation should begin
     */
    private data class TimelineEntry(
        val animation: Animatable,
        val offset: Long
    )

    /**
     * Timeline entries in order of addition.
     */
    private val entries = mutableListOf<TimelineEntry>()

    /**
     * Timeline start time for computing animation delays.
     */
    private val startTime: Long = System.currentTimeMillis()

    init {
        init()
    }

    /**
     * Add an animation to the timeline with optional offset.
     *
     * The offset determines when the animation becomes active relative to
     * the timeline's start time. Animations with offset=0 start immediately.
     *
     * ## Example: Building Timeline Step by Step
     * ```kotlin
     * val timeline = GeoTimeline()
     *
     * // First animation starts immediately
     * val anim1 = GeoAnimator().apply {
     *     ::x.animate(100.0, 1000)
     * }
     * timeline.add(anim1)
     *
     * // Second animation starts 200ms later
     * val anim2 = GeoAnimator().apply {
     *     ::y.animate(100.0, 1000)
     * }
     * timeline.add(anim2, offset = 200)
     *
     * // Third animation starts 500ms after timeline start
     * val anim3 = GeoAnimator().apply {
     *     ::size.animate(20.0, 1000)
     * }
     * timeline.add(anim3, offset = 500)
     * ```
     *
     * @param animation The Animatable to add to timeline (typically GeoAnimator)
     * @param offset Milliseconds after timeline start when animation should be active (default: 0)
     */
    fun add(animation: Animatable, offset: Long = 0L) {
        entries.add(TimelineEntry(animation, offset))
    }

    /**
     * Update all animations in the timeline.
     *
     * Should be called each frame in the OpenRNDR extend block.
     * Only updates animations whose offset has been reached.
     *
     * ## Example: Timeline in Extend Loop
     * ```kotlin
     * fun main() = application {
     *     program {
     *         val timeline = GeoTimeline {
     *             val a1 = GeoAnimator().apply {
     *                 ::x.animate(300.0, 2000)
     *             }
     *             add(a1)
     *
     *             val a2 = GeoAnimator().apply {
     *                 ::y.animate(200.0, 2000)
     *             }
     *             add(a2, offset = 500)
     *         }
     *
     *         extend {
     *             timeline.update()  // Updates a1 immediately, a2 after 500ms
     *
     *             // Render using animated values...
     *         }
     *     }
     * }
     * ```
     *
     * @see GeoAnimator.updateAnimation Called internally for each active animation
     */
    fun update() {
        val elapsed = System.currentTimeMillis() - startTime
        entries.forEach { entry ->
            if (elapsed >= entry.offset) {
                entry.animation.updateAnimation()
            }
        }
    }

    /**
     * Check if all animations in the timeline have completed.
     *
     * @return true if all timeline entries have finished their animations
     */
    fun isComplete(): Boolean {
        val elapsed = System.currentTimeMillis() - startTime
        return entries.all { entry ->
            // Consider complete if offset hasn't been reached yet (not started)
            // OR if animation has no more animations running
            elapsed < entry.offset || !entry.animation.hasAnimations()
        }
    }

    /**
     * Get the number of animations in the timeline.
     */
    fun size(): Int = entries.size

    /**
     * Check if timeline has no animations.
     */
    fun isEmpty(): Boolean = entries.isEmpty()
}
