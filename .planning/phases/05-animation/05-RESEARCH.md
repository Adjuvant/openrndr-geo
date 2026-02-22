# Phase 5: Animation - Research

**Researched:** 2026-02-22
**Domain:** OpenRNDR animation, Kotlin DSLs, geospatial interpolation
**Confidence:** MEDIUM

## Summary

Phase 5 focuses on creating an animation system for geo primitives using OpenRNDR's built-in `Animatable` API, enhanced with Kotlin DSL patterns for fluent configuration. Research reveals that OpenRNDR provides a mature property-based animation framework (openrndr-animatable) that supports easing functions, property references, and chaining via `.complete()`. The orx-easing library offers additional easing choices while maintaining full compatibility with Animatable. Geo-specific interpolation uses the Haversine formula for great-circle paths, with both constant-velocity and constant-speed interpolation options.

**Primary recommendation:** Extend OpenRNDR's `Animatable` class with geo-specific wrappers using the project's established `invoke()` DSL pattern. Implement dual-scope animation (global controller + per-feature animators) with per-property easing and stagger effects. Use orx-easing for additional easing functions and constant-speed interpolation via arc-length parameterization.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| openrndr-animatable | 0.4.5+ | Property-based animation API | Native OpenRNDR support, uses property references (::x.animate()), built-in easing functions |
| Kotlin invoke() operator | 1.9+ | DSL syntax pattern | Consistent with existing Style and GeoLayer patterns, type-safe builders |
| Animatable.updateAnimation() | 0.4.5+ | Animation update loop | OpenRNDR's built-in per-frame update system, integrates with extend block |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| orx-easing | 0.4.5+ | Additional easing functions | When user needs extra easing options beyond Animatable defaults |
| Haversine formula | - | Great-circle interpolation for geo paths | For accurate spherical interpolation (not linear) on Earth surface |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| openrndr-animatable | Manual time-based animation | Animatable provides property abstraction, easing, chaining already |
| orx-easing | orx-fcurve | orx-fcurve is for keyframe timeline system, not property tweening - different use case |
| Haversine interpolation | Linear interpolation | Linear is faster but inaccurate for long distances (>100km), shows "straight line on map" artifact |

**Installation:**
```kotlin
// Already included in OpenRNDR core - no new dependencies needed
implementation("org.openrndr:openrndr-animatable:${openrndrVersion}")

// Optional: additional easing functions
implementation("org.openrndr.extra:orx-easing:${orxVersion}")
```

**Note:** orx-easing is commented out in build.gradle.kts (line 25). If using, uncomment and reload Gradle.

## Architecture Patterns

### Recommended Project Structure
```
src/
├── geo/
│   ├── animation/
│   │   ├── GeoAnimator.kt              # Main animator interface and implementation
│   │   ├── AnimationConfig.kt          # DSL configuration builder
│   │   ├── EasingExtensions.kt        # Convenience extensions for easing
│   │   ├── GeoInterpolator.kt         # Path interpolation (great-circle, linear)
│   │   ├── StaggerEffects.kt          # Index-based and spatial stagger
│   │   └── internal/
│   │       ├── AnimatorState.kt       # Internal state management
│   │       └── InterpolationMath.kt    # Haversine, arc-length parameterization
```

### Pattern 1: Extending Animatable with Geo-Specific Wrappers
**What:** Create geo-specific animation wrappers around OpenRNDR's Animatable class, leveraging property references for type-safe animation
**When to use:** When animating geo primitives (points on paths, properties like position, color, size)
**Example:**
```kotlin
// Source: OpenRNDR Animatable documentation
class GeoAnimator : Animatable() {
    var progress: Double = 0.0
    var featureIndex: Int = 0
}

// Usage in extend loop
fun main() = application {
    program {
        val animator = object : GeoAnimator() {
            var x = 0.0
            var y = 0.0
        }
        
        animator.apply {
            ::x.animate(width.toDouble(), 5000, Easing.CubicInOut)
            ::y.animate(height.toDouble(), 5000, Easing.CubicInOut)
        }
        
        extend {
            animator.updateAnimation()
            drawer.circle(animator.x, animator.y, 100.0)
        }
    }
}
```

### Pattern 2: Dual-Scope Animation (Global + Per-Feature)
**What:** Support both single global animator for all features and independent animators per feature
**When to use:** When user wants unified timeline control OR independent feature animation
**Example:**
```kotlin
// Global animator (one controller for all features)
val globalAnimator = extend(GeoAnimator()) {
    // Animates all features with unified timing
    features.forEach { feature ->
        animateFeaturePosition(feature, 2000, Easing.EaseInOut)
    }
}

// Per-feature animator (independent animations)
val featureAnimators = features.map { feature ->
    object : GeoAnimator() {
        var x = feature.geometry.centroid().x
        var y = feature.geometry.centroid().y
    }.apply {
        ::x.animate(targetX, duration(featureIndex), easing(featureIndex))
        ::y.animate(targetY, duration(featureIndex), easing(featureIndex))
    }
}
```

### Pattern 3: Per-Property Easing Support
**What:** Allow each animated property to have its own easing function
**When to use:** When different properties need different motion characteristics
**Example:**
```kotlin
// Source: CONTEXT.md decision - "per-property easing" requirement
animation.apply {
    // Position uses easeOut for natural deceleration
    ::x.animate(targetX, 2000, Easing.CubicOut)
    ::y.animate(targetY, 2000, Easing.CubicOut)
    
    // Color uses linear for smooth color transition
    ::color.animate(targetColor, 2000, Easing.Linear)
    
    // Size uses easeIn for grow effect
    ::size.animate(targetSize, 2000, Easing.CubicIn)
}
```

### Pattern 4: Timeline-Based and Chain-Based Composition
**What:** Support both `timeline()` syntax for explicit sequencing and `then {}` for fluent chaining
**When to use:** When users prefer different composition styles
**Example:**
```kotlin
// Timeline-based (explicit control)
val timeline = GeoTimeline {
    add(animation1)
    add(animation2, offset = 200)  // Start 200ms after animation1
    add(animation3, offset = 400)  // Start 400ms after timeline start
}

// Chain-based (fluent API)
animate { 
    // Animation 1
}.then {
    // Animation 2
}.then {
    // Animation 3
}
```

### Pattern 5: Stagger Effects (Index-Based and Spatial)
**What:** Apply staggering delays based on feature order (index-based) or geographic distance (spatial)
**When to use:** For ripple effects, wave animations, or sequential entry
**Example:**
```kotlin
// Index-based stagger (by feature order)
val staggeredAnimators = features.mapIndexed { index, feature ->
    val delay = index * 100  // 100ms delay per feature
    animatorFor(feature).apply {
        ::x.animate(targetX, 1000, Easing.CubicInOut)
        // Start animation with staggered delay
    }
}

// Spatial stagger (by distance from geographic origin)
val origin = Vector2(0.0, 0.0)
val spatialStagger = features.map { feature ->
    val distance = haversineDistance(feature.geometry.centroid(), origin)
    val delay = distance.toDouble() * 10  // 10ms delay per km
    animatorFor(feature).apply {
        ::x.animate(targetX, 1000, Easing.CubicInOut)
    }
}
```

### Anti-Patterns to Avoid
- **Implementing custom animation loop:** OpenRNDR's Animatable provides `updateAnimation()`, don't roll your own time tracking
- **Immutable animation state:** Must use `var` properties for mutable state (zero-allocation requirement)
- **Linear interpolation for long distances:** Use Haversine for great-circle paths on Earth for accuracy
- **Creating Animatable in render loop:** Create once, reuse for frame-by-frame updates
- **Easing functions with hard-coded values:** Use Easing enum constants for consistency

## Don't Hand-Roll

### Animation Loop
**Problem:** Per-frame animation updates
**Don't Build:** Manual time tracking, delta time calculations, manually computing easing functions
**Use Instead:** Animatable.updateAnimation() called in extend block
**Why:** OpenRNDR's built-in system handles time tracking, easing application, property updates. Hand-rolled animation is error-prone and loses built-in features.

**Implementation:**
```kotlin
// CORRECT: Use OpenRNDR's updateAnimation()
extend {
    animator.updateAnimation()
    // Now use animator.x, animator.y etc.
}

// WRONG: Manual time tracking
var startTime = System.currentTimeMillis()
extend {
    val elapsed = System.currentTimeMillis() - startTime
    val progress = min(elapsed / duration.toDouble(), 1.0)
    val eased = cubicOut(progress)
    val x = startX + (endX - startX) * eased
    // Error-prone, loses easing flexibility
}
```

### Easing Functions
**Problem:** Acceleration/deceleration curves for natural motion
**Don't Build:** Custom easing functions, hardcoded easing math
**Use Instead:** OpenRNDR's Easing enum (CubicInOut, EaseInOut, etc.) + orx-easing for additional functions
**Why:** Easing is mathematically complex. OpenRNDR provides 13 families with 3 variants each (Linear, Sine, Quadratic, Cubic, Quartic, Quintic, Exponential, Circular, Elastic, Back, Bounce).

**Implementation:**
```kotlin
// CORRECT: Use built-in easing
animation.apply {
    ::x.animate(targetX, 2000, Easing.CubicInOut)
}

// WRONG: Hardcoded easing math
val progress = (elapsed / duration).toDouble()
val x = startX + (endX - startX) * cubicInOut(progress)  // Should use Easing enum
```

### Property References
**Problem:** Type-safe property animation
**Don't Build:** Reflection-based property access, string-based property names
**Use Instead:** Kotlin property references (::x)
**Why:** Property references provide compile-time safety, IDE autocomplete, no runtime string parsing overhead. OpenRNDR's `::x.animate()` syntax is idiomatic and type-safe.

**Implementation:**
```kotlin
// CORRECT: Property references (compile-time safe)
animation.apply {
    ::x.animate(targetX, 1000)
    ::y.animate(targetY, 1000)
}

// WRONG: String-based reflection
animation.animateProperty("x", targetX, 1000)  // Runtime errors, no compile-time safety
```

### Geographic Interpolation
**Problem:** Interpolating positions on Earth's curved surface
**Don't Build:** Simple linear interpolation between lat/lng coordinates
**Use Instead:** Haversine formula for great-circle interpolation
**Why:** Linear interpolation produces artifacts (straight lines across Earth that curve on sphere). Great-circle interpolation follows shortest path on curved surface.

**Key insight:** For short distances (local maps), linear is acceptable. For long distances, great-circle is required. Provide both options with sensible default.

## Common Pitfalls

### Pitfall 1: Mutable State vs Immutable Animation
**What goes wrong:** Using immutable style copies in animation loop causes GC pressure and frame drops
**Why it happens:** Kotlin data classes encourage immutability, but CONTEXT.md requires zero-allocation for 60fps animation
**How to avoid:**
- Create Animatable objects with `var` properties (mutable state)
- `updateAnimation()` handles all value updates implicitly
- Don't create new objects each frame in render loop

**Warning signs:** Performance profiling shows allocation in render loop, GC pauses causing frame drops

### Pitfall 2: Not Calling updateAnimation()
**What goes wrong:** Animation doesn't progress, properties never change values
**Why it happens:** Animatable requires explicit update call each frame to apply easing and progress
**How to avoid:** Always call `animator.updateAnimation()` at the start of extend block before reading animated properties

**Warning signs:** Animation stays at starting position, no motion over time

### Pitfall 3: Wrong Coordinate System for Geo Animation
**What goes wrong:** Animations move in straight lines (screen space) when should follow great-circle (geographic)
**Why it happens:** Defaulting to projected screen coordinates for performance, but geo paths require spherical interpolation
**How to avoid:** Use geographic coordinate mode for accurate great-circle paths, projected screen coordinates for performance (document tradeoff)

**Warning signs:** Long-distance routes appearing as straight lines across map (cutting across oceans, not following curvature)

### Pitfall 4: Easing Mismatch with Animation Duration
**What goes wrong:** Animation completes too quickly or feels "stiff" despite easing
**Why it happens:** Easing functions assume progress 0-1 over duration. If duration too short/long, easing can't express properly
**How to avoid:** Match animation duration to expected motion speed. Test with both linear and easing to calibrate.

**Warning signs:** Animation looks rushed, overshoots target, or feels sluggish

### Pitfall 5: Per-Feature Animator Allocation
**What goes wrong:** Creating separate Animatable for each feature (1000+ features) causes OOM or performance issues
**Why it happens:** Convenience of per-feature independent animation creates object explosion
**How to avoid:** Use global animator for many features, per-feature only for specialized cases. Or reuse Animatable objects and mutate properties instead of creating new ones.

**Warning signs:** Memory usage grows with feature count, GC pauses in animation loop

## Code Examples

### Basic Property Animation with Animatable
```kotlin
// Source: OpenRNDR Guide - Interactive Animations
fun main() = application {
    program {
        val animation = object : Animatable() {
            var x = 0.0
            var y = 360.0
        }
        
        animation.apply {
            ::x.animate(width.toDouble(), 5000)
        }
        
        extend {
            animation.updateAnimation()
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.circle(animation.x, animation.y, 100.0)
        }
    }
}
```

### Geometry Property Animation
```kotlin
// Animate Style properties of geo features
val featureAnimator = object : Animatable() {
    var color = ColorRGBa.WHITE
    var size = 5.0
}

featureAnimator.apply {
    ::color.animate(ColorRGBa.RED, 2000, Easing.CubicInOut)
    ::size.animate(20.0, 2000, Easing.CubicOut)
}

extend {
    featureAnimator.updateAnimation()
    features.forEach { feature ->
        val style = Style {
            fill = featureAnimator.color
            stroke = featureAnimator.color.withAlpha(0.5)
            size = featureAnimator.size
        }
        drawPoint(drawer, feature.geometry.centroid(), style)
    }
}
```

### Path Animation with Interpolation
```kotlin
// Animate geometry along path (great-circle interpolation)
data class PathAnimation(
    val path: List<Vector2>,
    val duration: Long = 3000
) : Animatable() {
    var progress: Double = 0.0
    val targetPoint: Vector2 get() = interpolateGreatCircle(path, progress)
}

val pathAnimation = PathAnimation(waypoints).apply {
    ::progress.animate(1.0, 3000, Easing.CubicInOut)
}

extend {
    pathAnimation.updateAnimation()
    drawer.circle(pathAnimation.targetPoint, 10.0)
}
```

### Staggered Animation (Index-Based)
```kotlin
// Animate features with staggered delays
val features = loadGeoPackage("data.gpkg").features.toList()
val staggeredAnimators = features.mapIndexed { index, feature ->
    object : Animatable() {
        var x = feature.geometry.centroid().x
        var y = feature.geometry.centroid().y
    }.apply {
        val delay = index * 50L  // 50ms stagger per feature
        ::x.animate(x + 100.0, 1000, Easing.CubicInOut)
        ::y.animate(y, 1000, Easing.CubicInOut)
    }
}

extend {
    staggeredAnimators.forEach { it.updateAnimation() }
    // Draw features at animated positions
}
```

### Spatial Stagger (Ripple Effect)
```kotlin
// Distance-based stagger from origin
val origin = Vector3(0.0, 0.0, 0.0)
val features = loadGeoPackage("data.gpkg").features.toList()

val spatialAnimators = features.map { feature ->
    val centroid = feature.geometry.centroid().vector3(z = 0.0)
    val distance = Vector2(centroid.x, centroid.y).distance(Vector2(origin.x, origin.y))
    val delay = (distance * 10).toLong()  // 10ms per unit distance
    
    object : Animatable() {
        var color = ColorRGBa.WHITE
        var size = 5.0
    }.apply {
        ::color.animate(ColorRGBa.RED, 1000, Easing.CubicInOut)
        ::size.animate(20.0, 1000, Easing.CubicInOut)
    }
}
```

### Timeline Composition
```kotlin
// Create timeline with multiple animations
val timeline = GeoTimeline {
    add(firstAnimation)
    add(secondAnimation, offset = 200)  // Start 200ms after firstAnimation
    add(thirdAnimation, offset = 400)  // Start 400ms after timeline start
}

extend {
    timeline.update()
    // Render features based on timeline state
}
```

### Chained Animation (Fluent API)
```kotlin
// Fluent chaining with then()
animate {
    // Step 1: Grow size
    ::size.animate(20.0, 1000, Easing.CubicOut)
}.then {
    // Step 2: Change color
    ::color.animate(ColorRGBa.RED, 500, Easing.Linear)
}.then {
    // Step 3: Move position
    ::x.animate(targetX, 1000, Easing.CubicInOut)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Manual time tracking | Animatable.updateAnimation() | OpenRNDR 0.4+ | Declarative property animation, no math errors |
| Linear interpolation only | Great-circle interpolation (Haversine) | Geo lib best practice | Accurate spherical paths on Earth |
| Centralized animation controller | Dual scope (global + per-feature) | Modern animation frameworks | Flexibility for different animation patterns |
| Single easing | Per-property easing | Anime.js-inspired API | Expressive motion control |
| Manual sequencing | Timeline and chains | Anime.js-inspired API | Cleaner composition of multi-step animations |

**Deprecated/outdated:**
- **Manual delta time calculation:** Replaced by Animatable's per-frame updates
- **String-based property animation:** Replaced by Kotlin property references (::x)
- **Linear interpolation for geo:** Replaced by Haversine for long distances
- **Centralized-only animation controller:** Dual scope provides better flexibility

## Open Questions

### 1. Timeline vs Chain API Syntax (OpenCode's Discretion)
**What we know:** CONTEXT.md requires both timeline-based (`timeline().add()`) and chain-based (`then {}`) composition styles
**What's unclear:** Exact API syntax for Kotlin DSL implementation

**Options:**
1. **Simple builder:** `GeoTimeline { }` function returning composed animator
2. **Class-based:** `GeoTimeline()` class with `add()` method
3. **Extension-based:** `animate {}.then {}` using extension functions

**Recommendation:** Start with simple `GeoTimeline { }` builder for v1, add `then {}` extensions in v2 if needed. Timeline provides explicit control, chains provide convenience.

### 2. Time Control Mechanism (OpenCode's Discretion)
**What we know:** Users may need scrubbing (seeking), pausing, control over playback
**What's unclear:** Whether to expose pause/resume methods, time seeking API

**Options:**
1. **Minimal:** Only play/pause (no seeking)
2. **Full:** pause, resume, seek(time), setSpeed(playbackRate)
3. **OpenRNDR-native:** Leverage OpenRNDR's time control mechanisms if available

**Recommendation:** Provide pause/resume via `Animator.pause()` / `Animator.resume()` for v1. Add seeking in v2 if user feedback indicates need.

### 3. Lifecycle Hook Implementation (OpenCode's Discretion)
**What we know:** Need to call `updateAnimation()` each frame, possibly hook into animation lifecycle (onStart, onComplete)
**What's unclear:** Best pattern for lifecycle callbacks in Kotlin

**Options:**
1. **Callback properties:** `onStart = { }`, `onComplete = { }`
2. **DSL hooks:** `animate { }` with `onStart {}`, `onComplete {}` blocks
3. **Listen callbacks:** `.completed.listen { }` like OpenRNDR examples

**Recommendation:** Use `.completed.listen {}` pattern (consistent with OpenRNDR Animatable examples, docs show this pattern).

### 4. Default Interpolator Implementations (OpenCode's Discretion)
**What we know:** Need interpolators for position (Haversine), color (linear), size (linear)
**What's unclear:** Should interpolators be functions, interfaces, or sealed classes

**Options:**
1. **Function typealias:** `typealias Interpolator<T> = (T, T, Double) -> T`
2. **Interface:** `interface Interpolator<T> { fun interpolate(from: T, to: T, progress: Double): T }`
3. **Sealed class:** `sealed class PathInterpolator { ... }`

**Recommendation:** Interface-based for flexibility, allows user-provided custom interpolators. Default implementations: `HaversineInterpolator`, `LinearInterpolator`.

### 5. Both Projected and Geographic Coordinates
**What we know:** CONTEXT.md requires both projected screen coordinates (default) AND geographic coordinates (option)
**What's unclear:** How to expose this option in API

**Options:**
1. **Boolean flag:** `AnimationConfig(useGeographic = false)`
2. **Separate interpolator types:** `ScreenInterpolator` vs `GeoInterpolator`
3. **Implicit behavior:** Detect from geometry type

**Recommendation:** Boolean flag in config for v1. Document tradeoff: geographic = accurate great-circle, projected = faster (default for performance).

## Sources

### Primary (HIGH confidence)
- OpenRNDR Guide "Basic animations" - Animation loop, percentage calculations, `seconds` variable
- OpenRNDR Guide "Interactive animations" - Animatable class, property references (::x.animate()), updating with `updateAnimation()`, Easing enum (CubicInOut, etc.), `.complete()` for chaining
- OpenRNDR API docs - Property animations, hasAnimations(), cancel(), LinearType support
- OpenRNDR Guide "Extend" - `extend { }` draw loop pattern
- Build.gradle.kts lines 25, 319 - openrndr-animatable included, orx-easing commented

### Secondary (MEDIUM confidence)
- Kotlin Lang Docs "Type-safe builders" - invoke() operator, lambda with receiver, DSL patterns
- OpenRNDR Guide examples - `::latch.completed.listen {}` callback pattern for animation lifecycle
- Haversine formula implementations (GIST, scipython.com) - Great-circle distance math, spherical interpolation

### Tertiary (LOW confidence)
- Dev.to "Kotlin DSLs in 2026" - Scope control, @DslMarker (not needed for v1)
- Anime.js docs - Timeline composition, staggering concepts (adapted from JS, not directly usable)
- Web search for "geographic interpolation" - General best practices, not OpenRNDR-specific

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - verified OpenRNDR Animatable from official guide, orx-easing from build.gradle.kts
- Architecture: MEDIUM - Animatable patterns from official docs, Kotlin DSL patterns from Kotlin lang docs (well-known patterns), Geo patterns from geospatial best practices (haversine)
- Pitfalls: MEDIUM - Zero-allocation from CONTEXT.md, updateAnimation() from OpenRNDR docs, coordinate system from common geospatial knowledge

**Research date:** 2026-02-22
**Valid until:** 30 days for OpenRNDR 0.4.5 (stable releases), 7 days for DSL patterns (syntax decisions change quickly)

---
*Phase: 05-animation*
*Research: OpenRNDR animation, Kotlin DSLs, geospatial interpolation*