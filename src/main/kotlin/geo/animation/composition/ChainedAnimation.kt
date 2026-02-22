package geo.animation.composition

import geo.animation.GeoAnimator
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing

/**
 * Builder for fluent chain-based animation composition.
 *
 * ChainedAnimationBuilder enables sequential animations via a fluent API
 * using `then {}` blocks. Each step in the chain executes after the
 * previous animation completes.
 *
 * ## Chain Pattern
 * ```kotlin
 * animate {
 *     // Step 1: Animate X position
 *     ::x.animate(300.0, 1000, Easing.CubicInOut)
 * }.then {
 *     // Step 2: Animate Y position (starts after step 1 completes)
 *     ::y.animate(200.0, 1000, Easing.CubicOut)
 * }.then {
 *     // Step 3: Animate size (starts after step 2 completes)
 *     ::size.animate(20.0, 800, Easing.CubicIn)
 * }
 * ```
 *
 * ## Integration with Extend Loop
 * ```kotlin
 * fun main() = application {
 *     program {
 *         val chain = animate {
 *             ::x.animate(300.0, 1000, Easing.CubicInOut)
 *         }.then {
 *             ::y.animate(200.0, 1000, Easing.CubicOut)
 *         }
 *
 *         extend {
 *             chain.update()  // Updates current animation in chain
 *         }
 *     }
 * }
 * ```
 *
 * ## Comparison with GeoTimeline
 * - **GeoTimeline:** Explicit offsets, all animations run on same controller
 * - **ChainedAnimation:** Sequential completion, each step can use fresh properties
 *
 * @property steps List of animation steps in execution order
 * @property currentStep Index of currently executing step
 * @see GeoTimeline For offset-based timeline composition
 * @see animate Entry point for creating chains
 *
 * @author Phase 05-animation - 05-03
 */
class ChainedAnimationBuilder {

    /**
     * Single step in the animation chain.
     *
     * @property block Lambda that configures animation properties
     * @property animator The GeoAnimator instance for this step
     * @property duration Expected duration of this step's animation
     */
    private data class ChainStep(
        val block: GeoAnimator.() -> Unit,
        val animator: GeoAnimator = GeoAnimator(),
        val duration: Long = 1000L
    )

    /**
     * Animation steps in execution order.
     */
    private val steps = mutableListOf<ChainStep>()

    /**
     * Index of currently active step (-1 if not started).
     */
    private var currentStepIndex: Int = -1

    /**
     * Time when current step started.
     */
    private var stepStartTime: Long = 0

    /**
     * Add a step to the animation chain.
     *
     * The step will execute after all previous steps complete.
     *
     * @param duration Expected duration of this step's animation (default: 1000ms)
     * @param block Lambda configuring the animation for this step
     * @return This builder for fluent chaining
     *
     * ## Example: Multi-Step Animation
     * ```kotlin
     * animate {
     *     // Step 1: Move right
     *     ::x.animate(300.0, 1000, Easing.CubicOut)
     * }.then(800) {  // Step 1 duration is 1000, step 2 starts after
     *     // Step 2: Move down
     *     ::y.animate(200.0, 800, Easing.CubicOut)
     * }.then(500) {
     *     // Step 3: Grow size
     *     ::size.animate(20.0, 500, Easing.CubicIn)
     * }
     * ```
     */
    fun then(duration: Long = 1000L, block: GeoAnimator.() -> Unit): ChainedAnimationBuilder {
        steps.add(ChainStep(block = block, duration = duration))
        return this
    }

    /**
     * Internal method to add the first step (called by animate() function).
     */
    internal fun first(duration: Long = 1000L, block: GeoAnimator.() -> Unit): ChainedAnimationBuilder {
        steps.add(ChainStep(block = block, duration = duration))
        // Start first step immediately
        startStep(0)
        return this
    }

    /**
     * Update the animation chain.
     *
     * Should be called each frame in the OpenRNDR extend block.
     * Automatically advances to next step when current completes.
     *
     * ## Example: Chain in Extend Loop
     * ```kotlin
     * val chain = animate {
     *     ::x.animate(300.0, 1000)
     * }.then {
     *     ::y.animate(200.0, 1000)
     * }
     *
     * extend {
     *     chain.update()  // Handles step advancement automatically
     * }
     * ```
     */
    fun update() {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size) return

        val currentStep = steps[currentStepIndex]
        currentStep.animator.updateAnimation()

        // Check if current step completed
        val elapsed = System.currentTimeMillis() - stepStartTime
        if (elapsed >= currentStep.duration && !currentStep.animator.hasAnimations()) {
            // Advance to next step
            val nextIndex = currentStepIndex + 1
            if (nextIndex < steps.size) {
                startStep(nextIndex)
            }
        }
    }

    /**
     * Start a specific step in the chain.
     */
    private fun startStep(index: Int) {
        if (index < 0 || index >= steps.size) return

        currentStepIndex = index
        stepStartTime = System.currentTimeMillis()

        // Execute the block on the animator to set up animation
        val step = steps[index]
        step.animator.apply(step.block)
    }

    /**
     * Get the currently executing step index (-1 if not started, size if complete).
     */
    fun currentStep(): Int = currentStepIndex

    /**
     * Check if the entire chain has completed.
     *
     * @return true if all steps have finished
     */
    fun isComplete(): Boolean {
        if (steps.isEmpty()) return true
        if (currentStepIndex < steps.size - 1) return false

        val lastStep = steps.last()
        val elapsed = System.currentTimeMillis() - stepStartTime
        return elapsed >= lastStep.duration && !lastStep.animator.hasAnimations()
    }

    /**
     * Get total number of steps in the chain.
     */
    fun stepCount(): Int = steps.size
}

/**
 * Entry point for creating chained animations.
 *
 * Starts a fluent animation chain using `then {}` for sequential steps.
 *
 * ## Example: Simple Chain
 * ```kotlin
 * val chain = animate {
 *     ::x.animate(300.0, 1000, Easing.CubicInOut)
 * }.then {
 *     ::y.animate(200.0, 1000, Easing.CubicOut)
 * }
 * ```
 *
 * ## Example: Complex Chain with Feature Animation
 * ```kotlin
 * val chain = animate {
 *     // Step 1: Fade in
 *     ::opacity.animate(1.0, 500, Easing.Linear)
 * }.then(1000) {
 *     // Step 2: Move to position
 *     ::x.animate(targetX, 1000, Easing.CubicOut)
 *     ::y.animate(targetY, 1000, Easing.CubicOut)
 * }.then(500) {
 *     // Step 3: Scale up
 *     ::size.animate(20.0, 500, Easing.CubicIn)
 * }.then(300) {
 *     // Step 4: Change color
 *     ::colorR.animate(1.0, 300, Easing.Linear)
 * }
 *
 * extend {
 *     chain.update()
 * }
 * ```
 *
 * @param duration Duration of first animation step (default: 1000ms)
 * @param block Lambda configuring the first step's animation
 * @return ChainedAnimationBuilder for fluent `then {}` chaining
 *
 * @see ChainedAnimationBuilder.then Add subsequent animation steps
 * @see GeoTimeline For offset-based composition alternative
 */
fun animate(duration: Long = 1000L, block: GeoAnimator.() -> Unit): ChainedAnimationBuilder {
    return ChainedAnimationBuilder().first(duration, block)
}
