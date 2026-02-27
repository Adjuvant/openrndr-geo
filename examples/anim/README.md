# Animation Examples

This category shows how to create dynamic geographic visualizations with time-based updates and smooth transitions.

## Examples

| # | Example | Description |
|---|---------|-------------|
| 01 | [01-basic-animation.kt](01-basic-animation.kt) | Basic property animation using OPENRNDR Animatable with easing |
| 02 | [02-geo-animator.kt](02-geo-animator.kt) | Animating geo-specific properties (coordinates, styles) |
| 03 | [03-timeline.kt](03-timeline.kt) | Timeline-based animation sequencing with staggered offsets |

## Key Concepts

- **OPENRNDR Animatable**: Base class providing animation infrastructure. Access via `animator()` extension function.

- **::property.animate()**: Tweening syntax using Kotlin property references. Format: `::property.animate(target, duration, easing)`

- **Easing Functions**: Control the rate of change for smooth animations:
  - `CubicInOut`: Smooth start and stop
  - `CubicOut`: Natural deceleration (settling)
  - `CubicIn`: Accelerating start
  - `Linear`: Constant rate

- **Timeline Sequencing**: Multiple animations with different offset delays create coordinated sequences.

- **Geo Property Animation**: Animating geographic coordinates and style properties (size, color) in map visualizations.

## Running Examples

```bash
# Run a specific example
./gradlew run -Popenrndr.application=examples.anim.BasicAnimation

# Or use the main class directly
./gradlew run --main=examples.anim.BasicAnimation
```

## Package

All examples use package `examples.anim`.

## Dependencies

Animation examples use:
- `org.openrndr:openrndr-animatable` - Built-in OPENRNDR animation
- `geo.animation.animator` - GeoAnimator singleton for geo visualizations
