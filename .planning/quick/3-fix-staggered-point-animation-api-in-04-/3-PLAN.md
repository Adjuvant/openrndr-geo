---
phase: quick
plan: 3
type: execute
wave: 1
depends_on: []
files_modified:
  - examples/anim/04-stagger-animator.kt
autonomous: true
requirements:
  - Fix staggered point animation API bug
must_haves:
  truths:
    - Animation uses proper Animatable delay mechanism instead of Thread.sleep
    - Each point animates with staggered timing based on its index
    - All animators are updated in the extend block
    - Points animate from size 0 to 5 with CubicOut easing
  artifacts:
    - path: examples/anim/04-stagger-animator.kt
      provides: Working stagger animation example
      contains:
        - "::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)"
        - "animators.forEach { it.updateAnimation() }"
        - "drawer.circle(ps.x, ps.y, it.animator.size)"
  key_links:
    - from: AnimationWrapper.delay
      to: animate() delay parameter
      via: "wrapper.delay passed as 4th parameter to animate()"
    - from: extend block
      to: GeoAnimator.updateAnimation()
      via: "animators.forEach { it.updateAnimation() }"
---

<objective>
Fix the staggered point animation API bug in 04-stagger-animator.kt

Purpose: The current implementation uses `Thread.sleep()` which blocks the main thread and prevents proper animation. The fix implements proper staggered animations using OpenRNDR's built-in delay mechanism.

Output: A working example demonstrating staggered point animations with proper timing
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@~/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@examples/anim/04-stagger-animator.kt
@src/main/kotlin/geo/animation/ProceduralMotion.kt
@src/main/kotlin/geo/animation/GeoAnimator.kt
</context>

<tasks>

<task type="auto">
  <name>task 1: Fix stagger animation to use proper delay mechanism</name>
  <files>examples/anim/04-stagger-animator.kt</files>
  <action>
    Rewrite the 04-stagger-animator.kt example to fix the animation bug:

    1. Create a data class to hold each feature with its animator (since each needs independent animation state)
    2. Change from using `Thread.sleep()` (which blocks) to using the `animate()` delay parameter
    3. Move animation setup to happen once per feature with proper delay parameter
    4. Store all animators and update them in the extend block
    5. Use the animated size property when drawing circles

    Key changes needed:
    - Replace `Thread.sleep(wrapper.delay)` with `::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)`
    - Create a list to hold Feature+Animator pairs
    - In extend block: iterate through pairs, call updateAnimation() on each, then draw with animated size
    - Remove the blocking sleep call entirely

    Pattern to follow:
    ```kotlin
    data class FeatureAnimator(val feature: Feature, val animator: GeoAnimator)
    
    val featureAnimators = features.map { wrapper ->
        val animator = GeoAnimator() // Create instance, not singleton
        animator.size = 0.0
        animator::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)
        FeatureAnimator(wrapper.feature, animator)
    }
    
    extend {
        featureAnimators.forEach { fa ->
            fa.animator.updateAnimation()
            val p = fa.feature.geometry as Point
            val ps = p.toScreen(projection)
            drawer.circle(ps.x, ps.y, fa.animator.size)
        }
    }
    ```
  </action>
  <verify>
    <automated>./gradlew compileKotlin 2>&1 | grep -q "BUILD SUCCESSFUL" && echo "Compilation successful" || echo "Compilation failed"</automated>
    <manual>Review the code to ensure no Thread.sleep() calls remain and proper delay parameter is used</manual>
  </verify>
  <done>Example compiles without errors, uses animate() with delay parameter, updates all animators in extend block</done>
</task>

<task type="auto">
  <name>task 2: Verify example runs and produces staggered animation</name>
  <files>examples/anim/04-stagger-animator.kt</files>
  <action>
    Run a quick syntax and import verification on the example:

    1. Verify all imports are present and correct
    2. Ensure GeoAnimator is instantiated properly (not using singleton for multiple animations)
    3. Confirm the AnimationWrapper import is used correctly
    4. Check that Easing is imported from correct package

    Fix any compilation issues that arise from the refactor.
  </action>
  <verify>
    <automated>./gradlew compileExamples 2>&1 | grep -E "(04-stagger-animator|BUILD)" | tail -5</automated>
  </verify>
  <done>Example file compiles as part of examples module with no errors</done>
</task>

</tasks>

<verification>
- Example compiles without errors
- No Thread.sleep() calls remain in the code
- Each feature has its own GeoAnimator instance (not singleton)
- Animation delay is passed as parameter to animate() method
- All animators are updated in the extend block before drawing
- Circles are drawn using the animated size property
</verification>

<success_criteria>
- Staggered animation example works correctly with proper timing
- Animation runs smoothly without blocking the main thread
- Each point animates in sequence based on its index delay
- Code follows OpenRNDR animation best practices
</success_criteria>

<output>
After completion, create `.planning/quick/3-fix-staggered-point-animation-api-in-04-/3-SUMMARY.md`
</output>
