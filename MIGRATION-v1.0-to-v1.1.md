# Migration Guide: v1.0.0 to v1.1.0

This guide covers the changes and new features in openrndr-geo v1.1.0.

## Overview

Version 1.1.0 introduces significant API improvements to reduce boilerplate and simplify common workflows:

- **Three-tier API** - One-liner to full control
- **CRS simplification** - Strongly-typed enum and geoStack()
- **Improved fitBounds** - Better Mercator handling

## Breaking Changes

None! v1.1.0 is fully backward compatible with v1.0.0.

## New Features

### 1. Three-Tier API

#### Tier 1: Absolute Simplest
```kotlin
// NEW in v1.1.0 - One-liner rendering
extend {
    drawer.geoJSON("world.json")  // Auto-load, auto-fit, auto-render
}
```

#### Tier 2: Load Once, Draw Many
```kotlin
// NEW in v1.1.0 - Source-based API
val source = geoSource("data.json")
extend {
    source.render(drawer)  // Auto-fits to viewport
}
```

#### Tier 3: Full Control (Existing API preserved)
```kotlin
// v1.0.0 style - still works
val features = GeoJSON.load("data.json")
val projection = ProjectionMercator { width = 800; height = 600 }
extend {
    features.forEach { drawer.draw(it.geometry, projection) }
}
```

### 2. CRS Simplification

#### Strongly-Typed CRS Enum (NEW)
```kotlin
import geo.crs.CRS

// Instead of raw strings
val transformed = source.transform(to = CRS.WebMercator)
val transformed = source.transform(to = CRS.WGS84)
val transformed = source.transform(to = CRS.BritishNationalGrid)
```

#### CRS Parsing
```kotlin
// Flexible parsing from various formats
CRS.fromString("EPSG:4326")  // Full EPSG
CRS.fromString("4326")        // Just the number
CRS.fromString("WGS84")       // Name
CRS.fromEPSG(4326)            // From integer
```

### 3. GeoStack for Multi-Dataset Overlays

```kotlin
// NEW in v1.1.0 - Multi-dataset with auto-CRS unification
val coastline = geoSource("coastline.json")
val cities = geoSource("cities.json")
val rivers = geoSource("rivers.json")

// Auto-unifies to first source's CRS
val map = geoStack(coastline, cities, rivers)
map.render(drawer)
```

### 4. Improved fitBounds

```kotlin
// Better Mercator handling with padding
val projection = ProjectionFactory.fitBounds(
    bounds = dataBounds,
    width = 800.0,
    height = 600.0,
    padding = 0.9,  // 90% of viewport (NEW)
    projection = ProjectionType.MERCATOR
)
```

### 5. GeoSource Enhancements

```kotlin
// Render with auto-fit
source.render(drawer)

// Transform with strongly-typed CRS
source.transform(to = CRS.WebMercator)

// Materialize for repeated access
val cached = source.materialize()
```

## Migration Checklist

To migrate from v1.0.0 to v1.1.0:

- [ ] **Optional**: Replace raw CRS strings with `CRS.WGS84`, `CRS.WebMercator`, etc.
- [ ] **Optional**: Use `drawer.geoJSON("file.json")` for simple workflows
- [ ] **Optional**: Use `geoStack()` for multi-dataset overlays
- [ ] **No changes required**: Existing v1.0.0 code continues to work

## API Comparison

| Feature | v1.0.0 | v1.1.0 |
|---------|--------|--------|
| Load + render | Multiple lines | `drawer.geoJSON()` |
| CRS transform | `source.autoTransformTo("EPSG:3857")` | `source.transform(to = CRS.WebMercator)` |
| Multi-dataset | Manual iteration | `geoStack(s1, s2).render()` |
| fitBounds | Basic | With padding parameter |

## Performance Notes

v1.1.0 includes optimizations:

- **Lazy evaluation**: Sequences for memory efficiency
- **Identity optimization**: Skip transform when CRS matches
- **Materialize**: Force eager evaluation for repeated rendering

```kotlin
// For repeated renders, materialize for performance
val cached = source.materialize()
extend {
    cached.render(drawer)  // No re-computation
}
```

---

*Generated for openrndr-geo v1.1.0*
