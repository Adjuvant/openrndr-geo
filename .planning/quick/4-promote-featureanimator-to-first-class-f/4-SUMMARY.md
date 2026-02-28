---
phase: quick
plan: 4
subsystem: animation
plan-type: execute
started: 2026-02-28T00:37:07Z
completed: 2026-02-28T00:40:00Z
duration: 3m
tasks: 2
status: complete
requires: []
provides: [ANIM-01]
affects: [examples/anim/04-stagger-animator.kt]
tech-stack:
  added: []
  patterns:
    - Extension functions on Sequence<Feature>
    - Data class with computed delay property
    - Factory function pattern with default parameters
key-files:
  created:
    - src/main/kotlin/geo/animation/FeatureAnimator.kt
  modified:
    - examples/anim/04-stagger-animator.kt
decisions:
  - Added delay property to FeatureAnimator for clean API (users access via fa.delay)
  - Used Sequence for lazy evaluation following existing staggerByIndex pattern
  - Factory function allows custom animator configuration per feature
---

# Phase Quick Plan 4: Promote FeatureAnimator to First-Class File Summary

**One-liner:** FeatureAnimator promoted from example-local definition to reusable library component in geo.animation package with stagger factory functions.

## What Was Built

### 1. FeatureAnimator Library Component

Created `src/main/kotlin/geo/animation/FeatureAnimator.kt` containing:

- **FeatureAnimator data class** - Pairs a `Feature` with its `GeoAnimator` and includes the computed `delay` value for staggered animations
- **staggerByIndex()** - Extension on `Sequence<Feature>` that creates FeatureAnimator instances with index-based sequential delays
- **staggerByDistance()** - Extension on `Sequence<Feature>` that creates FeatureAnimator instances with distance-based ripple delays

### 2. Updated Example

Modified `examples/anim/04-stagger-animator.kt`:
- Removed local `FeatureAnimator` data class definition (17 lines)
- Added import for `geo.animation.FeatureAnimator`
- Updated animation setup to use library `staggerByIndex()` extension
- Cleaner code using `map { fa.animator.apply { ... }; fa }` pattern

## Code Changes

### New File: FeatureAnimator.kt (185 lines)

Exports three public APIs:
1. `FeatureAnimator` data class
2. `Sequence<Feature>.staggerByIndex(delayMs, animatorFactory)`
3. `Sequence<Feature>.staggerByDistance(origin, factor, animatorFactory)`

### Modified: 04-stagger-animator.kt

```kotlin
// Before: Local definition + manual stagger
val featureAnimators = data.features.take(50).staggerByIndex(50).map { wrapper ->
    val animator = GeoAnimator()
    animator.apply {
        size = 0.0
        ::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay)
    }
    FeatureAnimator(wrapper.feature, animator)
}.toList()

// After: Library function
val featureAnimators = data.features.take(50)
    .asSequence()
    .staggerByIndex(delayMs = 50) { GeoAnimator() }
    .map { fa ->
        fa.animator.apply {
            size = 0.0
            ::size.animate(5.0, 1000, Easing.CubicOut, fa.delay)
        }
        fa
    }
    .toList()
```

## Architecture

### Design Decisions

1. **Three-property data class** - `feature`, `animator`, and `delay` together make the API clean. Users can access the delay via `fa.delay` when calling `animate()`.

2. **Factory function pattern** - The `animatorFactory` parameter allows users to configure animators during creation:
   ```kotlin
   .staggerByIndex(delayMs = 50) {
       GeoAnimator().apply {
           size = 10.0  // Custom initial state
       }
   }
   ```

3. **Sequence for lazy evaluation** - Following the existing `staggerByIndex` in ProceduralMotion, both factory functions return `Sequence<FeatureAnimator>` for deferred computation.

4. **Default parameters** - Both functions have sensible defaults (`delayMs = 50L`, `factor = 10.0`) making the common case simple.

## Commits

| Commit | Message | Files |
|--------|---------|-------|
| 735fc9c | feat(quick-4): create FeatureAnimator class with factory functions | FeatureAnimator.kt |
| fb7d90d | feat(quick-4): update example to use library FeatureAnimator | 04-stagger-animator.kt |

## Verification

- ✅ `./gradlew compileKotlin` passes
- ✅ FeatureAnimator class exists in geo.animation package
- ✅ Example imports FeatureAnimator from library (not local definition)
- ✅ Factory functions available for creating staggered FeatureAnimator sequences
- ✅ No local FeatureAnimator definition in example
- ✅ Pattern is now reusable across codebase

## Deviations from Plan

None - plan executed exactly as written.

## Next Steps

FeatureAnimator is now a first-class library component available for:
- Reuse in other animation examples
- Use in production visualizations requiring staggered effects
- Extension with additional factory functions (e.g., `staggerByProperty`)
