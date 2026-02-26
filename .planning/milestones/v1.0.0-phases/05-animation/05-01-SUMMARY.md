---
phase: 05-animation
plan: 01
subsystem: animation

tags: [openrndr-animatable, Easing, GeoAnimator, animation-lifecycle]

requires:
  - phase: 04.1-design-fix-crs-aware-geosource-with-auto-reprojection
    provides: CRS-aware data loading for animated geo visualizations

provides:
  - Global animation controller (GeoAnimator) with Animatable lifecycle
  - Convenience easing functions wrapping OpenRNDR Easing enum
  - Extension function pattern for OpenRNDR Program integration
  - Animation infrastructure foundation for tweening primitives (05-02)

affects:
  - 05-02-property-tweening
  - 05-03-procedural-motion
  - All future animation work

tech-stack:
  added: ["EasingExtensions.kt (convenience functions)"]
  patterns: ["Animatable lifecycle", "Global singleton animator", "Extension function pattern"]

key-files:
  created:
    - src/main/kotlin/geo/animation/GeoAnimator.kt
    - src/main/kotlin/geo/animation/EasingExtensions.kt
    - src/test/kotlin/geo/animation/GeoAnimatorTest.kt
  modified: []

key-decisions:
  - "Use OpenRNDR built-in Easing enum rather than orx-easing (commented out in build.gradle)"
  - "Top-level convenience functions instead of Companion extensions (cleaner DSL)"
  - "@JvmStatic singleton property to avoid JVM signature clash with lazy delegate"

patterns-established:
  - "Animatable lifecycle: updateAnimation() each frame before reading properties"
  - "Global singleton: Reuse GeoAnimator.instance across application lifecycle"
  - "Extension function: Program.animator() provides OpenRNDR integration"
  - "Convenience functions: easeInOut(), easeOut(), easeIn() as primary Cubic variants"

duration: 12min
completed: 2026-02-22
---

# Phase 5 Plan 1: GeoAnimator Infrastructure Summary

**GeoAnimator with Animatable lifecycle and 15 convenience easing functions providing DSL-friendly access to OpenRNDR's 13 built-in easing curves.**

## Performance

- **Duration:** 12 min
- **Started:** 2026-02-22T18:55:00Z
- **Completed:** 2026-02-22T19:07:00Z
- **Tasks:** 2 (plus 1 test task)
- **Files modified:** 3 created

## Accomplishments

1. **GeoAnimator class** extending OpenRNDR Animatable with:
   - Singleton pattern using lazy initialization
   - Mutable animated properties (x, y, progress) for tweening
   - @JvmStatic property to avoid JVM signature clash
   - Integration with OpenRNDR's property-based animation system

2. **Program.animator() extension function** for OpenRNDR integration:
   - Returns singleton instance within Program context
   - Follows established extend() pattern from Phases 3-4
   - Documented lifecycle: updateAnimation() each frame

3. **EasingExtensions.kt** with 15 convenience functions:
   - Primary: easeInOut(), easeOut(), easeIn() (Cubic curves)
   - Alternatives: sineInOut(), quadInOut(), quartInOut()
   - Explicit variants: cubicInOut(), cubicOut(), cubicIn()
   - Additional: linear(), none(), sineIn(), sineOut(), quadIn(), quadOut()

4. **7 synthetic tests** verifying:
   - Singleton behavior
   - Animatable inheritance
   - Mutable properties
   - All easing functions return valid Easing enum values

## Task Commits

Each task was committed atomically:

1. **Task 1: Create GeoAnimator** - `43398f7` (feat)
2. **Task 2: Create EasingExtensions** - `d5982d2` (feat)
3. **Tests: Animation infrastructure** - `46d3a76` (test)

**Plan metadata:** TBD (docs commit)

_Note: All 151+ tests pass including new GeoAnimatorTest_

## Files Created/Modified

- `src/main/kotlin/geo/animation/GeoAnimator.kt` - Global animation controller with Animatable lifecycle
- `src/main/kotlin/geo/animation/EasingExtensions.kt` - 15 convenience easing functions
- `src/test/kotlin/geo/animation/GeoAnimatorTest.kt` - 7 synthetic tests for infrastructure

## Decisions Made

1. **Use built-in Easing enum**: OpenRNDR's openrndr-animatable provides 13 easing curves (None, Sine*, Quad*, Cubic*, Quart*). Using these instead of orx-easing keeps dependencies minimal.

2. **Top-level functions**: Instead of `Easing.Companion.easeInOut()`, we use `easeInOut()` as a top-level function. This provides cleaner DSL usage: `::x.animate(target, 2000, easeInOut())`.

3. **@JvmStatic singleton**: The lazy property delegate creates a getter that clashes with explicit getInstance() method. Using @JvmStatic val instance solves this JVM signature conflict.

4. **Mutable properties**: Following CONTEXT.md's zero-allocation requirement for 60fps animation, properties use `var` (mutable) rather than immutable copies.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

**Compilation error: JVM signature clash**
- **Issue:** Original design used `private val instance by lazy` + `fun getInstance()`, causing JVM signature conflict (both generate `getInstance()` method)
- **Resolution:** Changed to `@JvmStatic val instance by lazy` with direct property access
- **Impact:** Cleaner API - users call `GeoAnimator.instance` instead of `GeoAnimator.getInstance()`

**Easing enum location confusion**
- **Issue:** Initially tried `org.openrndr.math.easing.Easing` - doesn't exist
- **Resolution:** Correct package is `org.openrndr.animatable.easing.Easing`
- **Impact:** Corrected import and all 15 convenience functions compile successfully

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

**Ready for 05-02: Property Tweening Primitives**

Foundation established:
- GeoAnimator infrastructure with Animatable lifecycle
- Easing functions accessible for animation configuration
- Extension function pattern for OpenRNDR integration
- Test infrastructure in place

**Deliverables for 05-02:**
- Property tweening DSL (::property.animate(target, duration, easing))
- Interpolators for position, color, size
- Path-based animation with Haversine interpolation
- Timeline and chain composition APIs

---
*Phase: 05-animation*
*Completed: 2026-02-22*
