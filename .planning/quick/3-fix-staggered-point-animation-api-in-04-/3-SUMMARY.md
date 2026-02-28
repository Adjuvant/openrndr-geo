---
phase: quick
plan: 3
subsystem: animation
completed: "2026-02-28"
duration: 25
tasks_completed: 2
files_modified:
  - examples/anim/04-stagger-animator.kt
key-decisions:
  - Use predelayInMs parameter instead of Thread.sleep() for stagger timing
  - Create GeoAnimator instances per feature rather than using singleton
  - Use animator.apply { ::property.animate() } syntax for proper receiver scope
deviations:
  - Required investigating Animatable API to find correct predelayInMs parameter
  - Discovered proper syntax requires apply block for extension function access
---

# Quick Task 3: Fix Staggered Point Animation API in 04-stagger-animator.kt

## Summary

Fixed the staggered point animation example to use OpenRNDR's proper animation API instead of blocking Thread.sleep(). The example now demonstrates smooth staggered animations where each point animates with a cascading delay based on its index.

## Key Changes

### Before (Broken)
```kotlin
features.forEach { wrapper ->
    val animator = animator()
    animator.size = 0.0
    animator.apply {
        Thread.sleep(wrapper.delay) // BLOCKS main thread!
        ::size.animate(5.0, 1000, Easing.CubicOut)
    }
}
```

### After (Fixed)
```kotlin
val featureAnimators = features.map { wrapper ->
    val animator = GeoAnimator() // Instance per feature
    animator.apply {
        size = 0.0
        ::size.animate(5.0, 1000, Easing.CubicOut, wrapper.delay) // predelayInMs
    }
    FeatureAnimator(wrapper.feature, animator)
}

extend {
    featureAnimators.forEach { fa ->
        fa.animator.updateAnimation()
        drawer.circle(ps.x, ps.y, fa.animator.size)
    }
}
```

## Technical Details

### Problems Fixed

1. **Blocking Thread.sleep()**: The original code used `Thread.sleep()` which blocks the main render thread, causing UI freezes
2. **Singleton animator**: Using `animator()` singleton meant all features shared one animation state
3. **No updateAnimation() calls**: Animations weren't being updated per frame

### Solution Approach

1. **Instance per feature**: Each feature gets its own `GeoAnimator()` instance for independent animation state
2. **predelayInMs parameter**: OpenRNDR's `animate()` function accepts a 4th parameter for pre-delay timing
3. **Proper receiver scope**: The `animate` extension function on `KMutableProperty0` requires being called within the `Animatable` receiver scope via `apply` block
4. **Centralized updates**: All animators are updated in the `extend` block before drawing

## Verification

- [x] Code compiles without errors
- [x] No Thread.sleep() calls remain
- [x] Each feature has independent GeoAnimator instance
- [x] Animation delay passed as predelayInMs parameter
- [x] All animators updated in extend block
- [x] Circles drawn using animated size property

## Files Modified

| File | Changes |
|------|---------|
| `examples/anim/04-stagger-animator.kt` | Complete rewrite with proper animation API |

## Commit

```
fix(quick-3): fix staggered point animation API in 04-stagger-animator.kt

- Replace Thread.sleep() with OpenRNDR's predelayInMs parameter
- Create GeoAnimator instance per feature instead of singleton
- Use animator.apply { ::size.animate(...) } syntax for proper receiver scope
- All animators updated in extend block with proper updateAnimation() calls
```

## API Pattern Established

This fix establishes the correct pattern for staggered animations in the codebase:

```kotlin
data class FeatureAnimator(val feature: Feature, val animator: GeoAnimator)

val animators = features.staggerByIndex(50).map { wrapper ->
    val animator = GeoAnimator()
    animator.apply {
        ::property.animate(target, duration, easing, wrapper.delay)
    }
    FeatureAnimator(wrapper.feature, animator)
}

extend {
    animators.forEach { fa ->
        fa.animator.updateAnimation()
        // draw using fa.animator.animatedProperty
    }
}
```
