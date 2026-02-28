---
phase: quick
type: execute
plan: 4
wave: 1
depends_on: []
files_modified:
  - src/main/kotlin/geo/animation/FeatureAnimator.kt
  - examples/anim/04-stagger-animator.kt
autonomous: true
requirements:
  - ANIM-01: Promote FeatureAnimator to first-class library component
must_haves:
  truths:
    - FeatureAnimator class exists in geo.animation package
    - FeatureAnimator pairs Feature with GeoAnimator instance
    - Example imports FeatureAnimator from library (not local definition)
    - Factory functions available for creating staggered FeatureAnimator sequences
  artifacts:
    - path: src/main/kotlin/geo/animation/FeatureAnimator.kt
      provides: FeatureAnimator data class and stagger extension functions
      exports: [FeatureAnimator, Feature.staggerByIndex, Feature.staggerByDistance]
  key_links:
    - from: examples/anim/04-stagger-animator.kt
      to: geo.animation.FeatureAnimator
      via: import statement
---

<objective>
Promote FeatureAnimator from example-local definition to first-class library component in geo.animation package.

Purpose: Make staggered animation patterns reusable across the codebase, not just in example code.
Output: FeatureAnimator.kt in geo.animation with factory functions, updated example importing from library.
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@.agents/skills/kotlin/language/SKILL.md
</execution_context>

<context>
@.planning/STATE.md
@src/main/kotlin/geo/animation/GeoAnimator.kt
@src/main/kotlin/geo/animation/ProceduralMotion.kt
@src/main/kotlin/geo/Feature.kt
@examples/anim/04-stagger-animator.kt
</context>

<tasks>

<task type="auto">
  <name>Create FeatureAnimator class with factory functions</name>
  <files>src/main/kotlin/geo/animation/FeatureAnimator.kt</files>
  <action>
Create a new file src/main/kotlin/geo/animation/FeatureAnimator.kt containing:

1. FeatureAnimator data class (move from example):
   ```kotlin
   data class FeatureAnimator(
       val feature: geo.Feature, 
       val animator: GeoAnimator
   )
   ```

2. Extension function on Sequence&lt;Feature&gt; for index-based stagger:
   ```kotlin
   fun Sequence&lt;Feature&gt;.staggerByIndex(
       delayMs: Long = 50L,
       animatorFactory: () -> GeoAnimator = { GeoAnimator() }
   ): Sequence&lt;FeatureAnimator&gt;
   ```
   - Maps each Feature to FeatureAnimator with GeoAnimator
   - Computes predelay for each animator based on index
   - Returns Sequence for lazy evaluation

3. Extension function on Sequence&lt;Feature&gt; for distance-based stagger:
   ```kotlin
   fun Sequence&lt;Feature&gt;.staggerByDistance(
       origin: Vector2,
       factor: Double = 10.0,
       animatorFactory: () -> GeoAnimator = { GeoAnimator() }
   ): Sequence&lt;FeatureAnimator&gt;
   ```
   - Maps each Feature to FeatureAnimator
   - Computes predelay based on distance from origin
   - Returns Sequence for lazy evaluation

Add proper KDoc documentation following existing patterns in GeoAnimator.kt and ProceduralMotion.kt.
Include imports for geo.Feature and org.openrndr.math.Vector2.
  </action>
  <verify>
    <automated>./gradlew compileKotlin --quiet 2>&1 | grep -E "(BUILD|error:|FAILED)" | head -5</automated>
    <manual>File exists with FeatureAnimator class and extension functions</manual>
  </verify>
  <done>
    - FeatureAnimator.kt compiles successfully
    - File exports FeatureAnimator, Sequence&lt;Feature&gt;.staggerByIndex(), Sequence&lt;Feature&gt;.staggerByDistance()
    - Documentation follows existing patterns
  </done>
</task>

<task type="auto">
  <name>Update example to use library FeatureAnimator</name>
  <files>examples/anim/04-stagger-animator.kt</files>
  <action>
Modify examples/anim/04-stagger-animator.kt:

1. Remove the local data class definition (lines 13-20):
   ```kotlin
   /**
    * Data class pairing a feature with its dedicated animator...
    */
   data class FeatureAnimator(val feature: geo.Feature, val animator: GeoAnimator)
   ```

2. Add import for FeatureAnimator:
   ```kotlin
   import geo.animation.FeatureAnimator
   ```

3. Replace the manual stagger logic (lines 41-49) with the new library function:
   ```kotlin
   // Create staggered animations using library function
   val featureAnimators = data.features.take(50)
       .asSequence()
       .staggerByIndex(delayMs = 50) { GeoAnimator() }
       .map { fa ->
           fa.animator.apply {
               size = 0.0
               ::size.animate(5.0, 1000, Easing.CubicOut, fa.animator.currentAnimationDelay)
           }
           fa
       }
       .toList()
   ```
   
   OR use a cleaner approach - the extension function should handle creating animators with predelay:
   ```kotlin
   val featureAnimators = data.features.take(50)
       .asSequence()
       .staggerByIndex(delayMs = 50) { 
           val animator = GeoAnimator()
           animator.size = 0.0
           animator::size.animate(5.0, 1000, Easing.CubicOut)
           animator
       }
       .toList()
   ```

   Wait - looking more carefully at the example, the predelay is passed to animate(). So the factory function should create animators, and the stagger functions should set up predelay. Let me reconsider...

   Actually, looking at line 46: `::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)`
   
   The 4th parameter to animate() is the predelay. So the extension function should:
   1. Create GeoAnimator for each feature
   2. Store the delay somewhere accessible (perhaps in a property or return it alongside)
   
   Actually, looking at Animatable API, animate() takes (target, duration, easing, predelay).
   So we need to pass the predelay when calling animate().

   Better approach: Keep FeatureAnimator simple, add helper extension that applies animation config:
   ```kotlin
   val featureAnimators = data.features.take(50)
       .asSequence()
       .staggerByIndex(delayMs = 50)
       .onEach { fa ->
           fa.animator.size = 0.0
           // The predelay is handled by the stagger function internally
       }
       .toList()
   ```

   Wait - I think the cleanest API is:
   1. staggerByIndex returns Sequence&lt;FeatureAnimator&gt; 
   2. Each FeatureAnimator has its animator with predelay already configured (or provide access to delay)

   Actually, simplest for now: Just move the data class to library. The example can still do the animation setup manually. We can add more sophisticated helpers later.

   REVISED approach for this task:
   - Import FeatureAnimator from library
   - Keep the example logic mostly the same but use imported FeatureAnimator
   - The manual staggerByIndex + map is fine for now
  </action>
  <verify>
    <automated>./gradlew compileKotlin --quiet 2>&1 | grep -E "(BUILD|error:|FAILED)" | head -5</automated>
    <manual>Example compiles and FeatureAnimator is imported from geo.animation</manual>
  </verify>
  <done>
    - Example imports FeatureAnimator from geo.animation
    - Local data class definition removed
    - Example compiles successfully
  </done>
</task>

</tasks>

<verification>
Overall verification:
1. `./gradlew compileKotlin` passes
2. FeatureAnimator class is in geo.animation package
3. Example imports and uses library FeatureAnimator
4. No local FeatureAnimator definition in example
</verification>

<success_criteria>
- FeatureAnimator promoted from example to src/main/kotlin/geo/animation/FeatureAnimator.kt
- Example updated to import from library
- Code compiles without errors
- FeatureAnimator pattern is now reusable across codebase
</success_criteria>

<output>
After completion, create `.planning/quick/4-promote-featureanimator-to-first-class-f/4-SUMMARY.md`
</output>
