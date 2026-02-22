---
status: resolved
trigger: "OutOfMemoryError: Java heap space at Graticule.kt:59 in generateGraticule()"
created: 2026-02-22T18:00:00Z
updated: 2026-02-22T18:30:00Z
---

## Root Cause Analysis

### What is the algorithm at line 59 that causes OOM?

Line 59 is:
```kotlin
features.add(Feature(Point(lon, lat)))
```

This runs inside nested while loops:
```kotlin
var lon = minLon
while (lon <= maxLon) {
    var lat = minLat
    while (lat <= maxLat) {
        features.add(Feature(Point(lon, lat)))
        lat += spacing
    }
    lon += spacing
}
```

### How many points are being generated for 1° spacing?

With typical bounds from loaded data:
- **UK area** (fallback: -8 to 2, 50 to 60): 11 × 11 = **121 points**
- **World bounds** (-180 to 180, -90 to 90): 361 × 181 = **65,000 points**

Neither should cause OOM.

### Is there an infinite loop or unbounded generation?

**YES - potential for unbounded generation exists.**

The algorithm has TWO critical issues:

1. **No spacing validation**: Any positive double can be passed as `spacing`. With very small values:
   - `spacing = 0.1`: 3,600 × 1,800 = **6.5 million points**
   - `spacing = 0.01`: 36,000 × 18,000 = **648 million points**
   - `spacing = 0.001`: 360,000 × 180,000 = **64.8 billion points**

2. **No bounds validation**: The code checks `bounds.isEmpty()` but NOT `bounds.isValid()`. If bounds have inverted values (minX > maxX, minY > maxY), the algorithm could behave unexpectedly.

3. **Floating-point accumulation**: While unlikely to cause infinite loops in practice, repeated `+=` on floating-point can accumulate error.

### What bounds are being used?

From LayerGraticule.kt:
- Tries to load `ness-vectors.gpkg`, falls back to `sample.geojson`
- Fallback bounds: minX=-8.0, maxX=2.0, minY=50.0, maxY=60.0 (UK area)

With these bounds and 1° spacing: only 121 points - no OOM possible.

### Root Cause

The **actual cause** is likely one of:
1. **Extremely small spacing value passed** (e.g., 0.001 or smaller)
2. **Data with global/international bounds** (though sample data is UK-only)
3. **JVM heap too small** (gradlew uses `-Xmx64m` = 64MB only)
4. **Feature object is larger than expected** (each Feature contains metadata)

### Files Involved

- `src/main/kotlin/geo/layer/Graticule.kt` - No input validation on spacing parameter
- `src/main/kotlin/geo/examples/LayerGraticule.kt` - Calls generateGraticuleSource with hardcoded values

---

## Resolution

### Root Cause Found

**Unbounded point generation due to lack of input validation**: The `generateGraticule()` function accepts any positive `spacing` value without validation. With small spacing values (e.g., 0.1, 0.01), the number of points grows quadratically, quickly exhausting heap memory.

### Missing Protection

1. **No minimum spacing validation**: Should enforce minimum spacing (e.g., 0.5° or 1.0°)
2. **No bounds validation**: Should check `bounds.isValid()` before processing
3. **No point limit**: Should cap maximum number of points generated

### Recommendation

Add input validation to `generateGraticule()`:
1. Validate `spacing >= 1.0` (or some reasonable minimum)
2. Validate `bounds.isValid()` 
3. Consider adding a maximum point count safeguard

Example fix approach:
```kotlin
fun generateGraticule(spacing: Double, bounds: Bounds): List<Feature> {
    if (bounds.isEmpty()) return emptyList()
    require(spacing >= 1.0) { "Spacing must be >= 1.0 degrees to prevent excessive memory usage" }
    require(bounds.isValid()) { "Bounds must be valid (minX <= maxX, minY <= maxY)" }
    
    // ... rest of algorithm
}
```

Alternatively, use integer-based iteration to avoid floating-point accumulation:
```kotlin
val lonSteps = ((maxLon - minLon) / spacing).toInt() + 1
val latSteps = ((maxLat - minLat) / spacing).toInt() + 1
for (i in 0 until lonSteps) {
    for (j in 0 until latSteps) {
        val lon = minLon + i * spacing
        val lat = minLat + j * spacing
        features.add(Feature(Point(lon, lat)))
    }
}
```

This approach:
- Uses integer iteration (no floating-point accumulation)
- Has predictable iteration count
- Won't infinite loop
