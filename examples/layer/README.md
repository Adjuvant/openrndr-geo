# Layer Examples

This category demonstrates how to build complex maps by composing multiple layers with different styles and data sources.

## Examples

| # | Example | Description |
|---|---------|-------------|
| 01 | [01-graticule.kt](01-graticule.kt) | Drawing latitude/longitude reference grid (graticule) |
| 02 | [02-composition.kt](02-composition.kt) | Layer composition with orx-compositor and blend modes |

## Key Concepts

- **Graticule**: A geographic reference grid of latitude and longitude lines. Generated using `generateGraticuleSource(spacing, bounds)`.

- **Layer Composition**: Using orx-compositor to stack multiple layers with different data sources and render effects.

- **Blend Modes**: Control how layers combine visually:
  - `Multiply`: Darkens the result (good for dark backgrounds)
  - `Overlay`: Increases contrast while preserving detail
  - `Screen`: Lightens the result (good for highlights)
  - `Add`: Additive blend (creates glow effects)

- **Z-Ordering**: In orx-compositor, layers defined later in the `compose { }` block are drawn on top of earlier layers.

- **ColorBuffer**: Offscreen rendering for complex layer effects and optimizations.

## Running Examples

```bash
# Run a specific example
./gradlew run -Popenrndr.application=examples.layer.Graticule

# Or use the main class directly
./gradlew run --main=examples.layer.Graticule
```

## Package

All examples use package `examples.layer`.

## Dependencies

Layer examples use:
- `org.openrndr.extra:orx-compositor` - Layer composition
- `org.openrndr.extra:orx-fx` - Blend mode effects
- `geo.layer.generateGraticuleSource()` - Graticule generation
