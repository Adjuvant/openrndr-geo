# Phase 16: Rendering Improvements - Research

**Researched:** 2026-03-08  
**Domain:** Geographic polygon rendering, OPENRNDR Shape API, polygon topology  
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Winding order handling**
- Normalize at load time, not render time
- After projecting lat/lon to screen pixels (Y inverted), the visual winding convention is reversed from geographic:
  - Exterior rings → `.clockwise` in screen space
  - Interior rings (holes) → `.counterClockwise` in screen space
- Produces canonical `Shape` objects that work reliably downstream
- Rationale: OPENRNDR's `Shape` fill depends on relative winding; deferring gains nothing and scatters defensive logic

**Antimeridian/dateline crossing**
- Split at the antimeridian combined with clip and close at load time
- Algorithm for rings crossing ±180° longitude:
  1. Walk coordinate pairs; detect crossings via `abs(lon₁ − lon₂) > 180`
  2. Interpolate new vertices at exactly +180 / -180 on the boundary
  3. Split into left ring (≤ +180) and right ring (≥ -180)
  4. Close each partial ring along the boundary edge at appropriate latitudes
  5. Emit as separate polygons in the MultiPolygon
- Run normal winding normalization pass on each resulting polygon
- Alternative: Use `ogr2ogr -wrapdateline` for pre-processing if preferred
- Rationale: Flat projected viewport needs disambiguated geometry; splitting composes cleanly with winding normalization, bounding-box queries, and spatial indexing

**Interior ring validation**
- Validate but don't repair — check problems, log warnings, render original data
- Specific validation rules:
  1. Drop degenerate rings (< 3 distinct vertices or near-zero signed area) — log warning and remove
  2. Check hole-inside-exterior — test if hole's first vertex lies inside exterior bounding box (fast) and optionally inside exterior contour; log warning if outside
  3. Skip self-intersection checks — too expensive; rely on visual artifacts being obvious
  4. Skip overlapping hole checks — too expensive; non-zero winding handles overlap acceptably
- All warnings go to logger (not stdout), tagged with feature ID/index for traceability
- Do NOT pull in JTS/GEOS for automatic repair — too heavy for creative coding context
- Rationale: Creative coding tool prioritizes visibility over correction; crashing on bad data is unacceptable, but silent repair risks mystery bugs

**MultiPolygon rendering**
- Single Shape with non-zero winding rule, not independent per-polygon rendering
- Assemble all contours (all exteriors + all holes) into one `Shape`:
  - Each exterior becomes a `.clockwise` `ShapeContour`
  - Each interior becomes a `.counterClockwise` `ShapeContour`
  - Pass combined list to `drawer.shape(Shape(contours))`
- Non-zero winding produces correct results:
  - Same-winding contours reinforce (adjacent ocean polygons merge seamlessly)
  - Opposite-winding contours subtract (holes punch through correctly)
  - No overdraw at shared boundaries, no seams with transparency
- Apply to both standard and optimized render paths
- Rationale: GPU rasterizer handles overlap via stencil operations — essentially free vs expensive boolean geometry; independent rendering causes z-fighting, overdraw, and alpha seams

### OpenCode's Discretion
- Exact logger implementation and warning format
- Whether to implement point-in-polygon test for hole-inside-exterior (vs just bbox check)
- Performance thresholds for when to skip expensive validation
- Whether to cache normalized/split geometries or compute on each load

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| RENDER-01 | MultiPolygon rendering for ocean/whole-world data — fix winding order and coordinate handling | OPENRNDR Shape API with `.clockwise`/`.counterClockwise` properties; non-zero winding rule; antimeridian split algorithm |
| RENDER-02 | Polygon interior/exterior ring handling — proper hole support and ring classification | ShapeContour with winding detection; Shape with multiple contours; hole-inside-exterior validation patterns |
</phase_requirements>

## Summary

Phase 16 addresses two critical rendering issues with geographic polygon data: (1) MultiPolygons spanning the antimeridian (±180° longitude) render incorrectly due to coordinate discontinuity, and (2) polygon winding order inconsistencies cause fill anomalies and hole rendering failures.

The solution leverages OPENRNDR's Shape API with its built-in winding detection (`.clockwise` and `.counterClockwise` properties) and non-zero winding fill rule. The key insight is that OPENRNDR uses the relative winding direction of contours within a Shape to determine fill behavior — clockwise contours add to the fill, counter-clockwise contours subtract (create holes).

**Primary recommendation:** Implement load-time geometry normalization that (a) splits antimeridian-crossing rings into separate polygons with boundary closure, and (b) enforces consistent winding order (exterior clockwise, interior counter-clockwise in screen space). Render MultiPolygons as a single Shape with all contours combined for seamless rendering without overdraw.

## Standard Stack

### Core (Already in Project)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OPENRNDR | 0.4.5 | Shape, ShapeContour, Drawer | Industry standard for creative coding in Kotlin/JVM |
| org.openrndr.shape.Shape | 0.4.5 | Multi-contour polygon rendering | Native support for holes via winding rules |
| org.openrndr.shape.ShapeContour | 0.4.5 | Individual ring representation | `.clockwise` / `.counterClockwise` properties |

### Key OPENRNDR APIs

**ShapeContour.winding** — Calculates winding direction (CLOCKWISE/COUNTER_CLOCKWISE) using the shoelace formula:
```kotlin
val winding: Winding by lazy {
    var sum = 0.0
    segments.forEach { s ->
        (listOf(s.start) + s.control + listOf(s.end)).zipWithNext { a, b ->
            sum += (b.x - a.x) * (b.y + a.y)
        }
    }
    // ... polarity handling
}
```

**ShapeContour.clockwise / .counterClockwise** — Returns contour with guaranteed winding:
```kotlin
val clockwise: ShapeContour get() = if (winding == Winding.CLOCKWISE) this else this.reversed
val counterClockwise: ShapeContour get() = if (winding == Winding.COUNTER_CLOCKWISE) this else this.reversed
```

**Shape with multiple contours** — Non-zero winding fill rule:
```kotlin
val shape = Shape(listOf(exteriorContour, holeContour1, holeContour2))
drawer.shape(shape)  // Holes subtract from fill automatically
```

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| mu.KotlinLogging | (via OPENRNDR) | Structured logging | Validation warnings tagged with feature ID |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| OPENRNDR Shape API | Custom triangulation/ear-clipping | OPENRNDR handles complex polygons, winding, edge cases natively |
| JTS/GEOS geometry repair | Validate-but-don't-repair | JTS is too heavy (~2MB) for creative coding context |
| Runtime winding fix | Load-time normalization | Load-time produces canonical data; defers nothing |

## Architecture Patterns

### Recommended Project Structure
```
src/main/kotlin/geo/render/
├── PolygonRenderer.kt              # Existing: writePolygon, writePolygonWithHoles
├── MultiRenderer.kt                # Existing: drawMultiPolygon (to be modified)
├── geometry/                       # NEW: Geometry normalization
│   ├── AntimeridianSplitter.kt     # Split rings at ±180° longitude
│   ├── WindingNormalizer.kt        # Enforce clockwise/counter-clockwise
│   └── RingValidator.kt            # Degenerate ring detection, hole-inside-exterior
└── internal/
    └── optimized/                  # Update for combined Shape rendering
```

### Pattern 1: Antimeridian-Aware Rendering
**What:** Detect rings that cross the antimeridian, split them at the boundary with interpolated vertices.

**When to use:** All geographic data that may include coordinates near ±180° longitude (ocean data, whole-world datasets).

**Example:**
```kotlin
// From 16-CONTEXT.md
fun splitAtAntimeridian(ring: List<Vector2>): List<List<Vector2>> {
    val result = mutableListOf<MutableList<Vector2>>()
    var currentRing = mutableListOf<Vector2>()
    
    for (i in ring.indices) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]
        
        currentRing.add(current)
        
        // Detect crossing: longitude jump > 180°
        if (abs(next.x - current.x) > 180.0) {
            // Interpolate boundary crossing
            val crossingLat = interpolateLatitude(current, next)
            val boundaryLon = if (current.x > 0) 180.0 else -180.0
            
            currentRing.add(Vector2(boundaryLon, crossingLat))
            result.add(currentRing)
            
            // Start new ring from other side of boundary
            currentRing = mutableListOf(Vector2(-boundaryLon, crossingLat))
        }
    }
    
    if (currentRing.isNotEmpty()) {
        result.add(currentRing)
    }
    
    return result
}
```

### Pattern 2: Winding Normalization
**What:** After projecting to screen space (where Y is inverted), enforce consistent winding:
- Exterior rings → `.clockwise`
- Interior rings (holes) → `.counterClockwise`

**When to use:** All polygon rendering to ensure consistent fill behavior.

**Example:**
```kotlin
// From 16-CONTEXT.md
fun normalizeWinding(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>
): Pair<List<Vector2>, List<List<Vector2>>> {
    // Project to screen space first
    val screenExterior = exterior.map { projection.project(it) }
    val screenInteriors = interiors.map { ring -> ring.map { projection.project(it) } }
    
    // Create contours and enforce winding
    val extContour = ShapeContour.fromPoints(screenExterior, closed = true)
    val normalizedExterior = if (extContour.winding == Winding.CLOCKWISE) 
        screenExterior 
    else 
        screenExterior.reversed()
    
    val normalizedInteriors = screenInteriors.map { ring ->
        val contour = ShapeContour.fromPoints(ring, closed = true)
        if (contour.winding == Winding.COUNTER_CLOCKWISE) 
            ring 
        else 
            ring.reversed()
    }
    
    return normalizedExterior to normalizedInteriors
}
```

### Pattern 3: Single Shape MultiPolygon Rendering
**What:** Combine all MultiPolygon contours into one Shape for seamless rendering.

**When to use:** Rendering MultiPolygons, especially ocean/whole-world data with adjacent polygons.

**Example:**
```kotlin
// From 16-CONTEXT.md
drawer.shape(Shape(
    polygons.flatMap { poly ->
        val ext = ShapeContour.fromPoints(
            poly.exterior.map { projection.project(it) },
            closed = true
        ).clockwise
        
        val holes = poly.interiors.map { ring ->
            ShapeContour.fromPoints(
                ring.map { projection.project(it) },
                closed = true
            ).counterClockwise
        }
        
        listOf(ext) + holes
    }
))
```

### Anti-Patterns to Avoid
- **Per-polygon independent rendering:** Causes overdraw, z-fighting, and seams at shared boundaries with transparency
- **Runtime winding detection:** Scatter defensive checks everywhere instead of canonical data at load time
- **Heavy geometry libraries (JTS/GEOS):** Overkill for creative coding; adds 2MB+ dependency
- **Silent data repair:** Hides data quality issues; validate-but-warn instead

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Polygon triangulation | Custom ear-clipping | OPENRNDR `drawer.shape(Shape)` | Handles complex polygons, winding, edge cases natively |
| Winding calculation | Shoelace formula implementation | `ShapeContour.winding` property | Already implemented, tested, handles polarity correctly |
| Fill rule implementation | Custom stencil buffer code | OPENRNDR non-zero winding rule | GPU-accelerated, handles overlapping holes correctly |
| Point-in-polygon test | Ray-casting algorithm | Bounding box + optional contour test | Simpler cases covered; full PIP only when needed |
| Geometry repair | JTS/GEOS integration | Validate-but-don't-repair | Creative coding prioritizes visibility over correctness |

**Key insight:** OPENRNDR's Shape API already handles the complex geometry topology. The work is in preprocessing geographic data (antimeridian splitting, winding normalization) to feed into this API correctly.

## Common Pitfalls

### Pitfall 1: Y-Axis Polarity Confusion
**What goes wrong:** After geographic projection, Y increases downward (screen coordinates). The winding direction flips from geographic convention (exterior CCW in GeoJSON) to screen convention (exterior CW for proper fill).

**Why it happens:** GeoJSON specifies exterior rings counter-clockwise, but that's in geographic space (Y up = north). Screen space has Y down, which reverses winding.

**How to avoid:** Always normalize winding AFTER projecting to screen space. Exterior → `.clockwise`, Interior → `.counterClockwise`.

**Warning signs:** Polygons that should be filled appear empty, or holes appear filled.

### Pitfall 2: Antimeridian Coordinate Jump
**What goes wrong:** A line segment from longitude +179.9° to -179.9° (crossing the antimeridian) renders as a line spanning 359.8° across the entire world instead of the 0.2° it should be.

**Why it happens:** Raw coordinate interpolation doesn't account for the ±180° wraparound.

**How to avoid:** Detect crossings via `abs(lon₁ − lon₂) > 180`, interpolate new vertices at exactly ±180°, split into separate rings, and close along the boundary edge.

**Warning signs:** Ocean data or whole-world datasets show world-spanning lines or triangular artifacts.

### Pitfall 3: MultiPolygon Overdraw and Seams
**What goes wrong:** Rendering MultiPolygon polygons independently causes overlapping fill at shared boundaries and visible seams with semi-transparent styles.

**Why it happens:** Each polygon is drawn separately, causing double-fill at boundaries and z-fighting artifacts.

**How to avoid:** Assemble all polygons into a single Shape with combined contours. Non-zero winding handles the overlap correctly (same winding = reinforce, opposite = subtract).

**Warning signs:** Semi-transparent MultiPolygons show darker lines at boundaries; performance degradation with many overlapping polygons.

### Pitfall 4: Degenerate Interior Rings
**What goes wrong:** Interior rings with < 3 vertices or near-zero area cause rendering artifacts or crashes.

**Why it happens:** Source data quality issues — self-intersecting rings, duplicate points, or geometric degeneracy.

**How to avoid:** Validate rings at load time: drop rings with < 3 distinct vertices or area < epsilon. Log warnings for traceability.

**Warning signs:** Random holes appear in polygons; unexpected fill patterns.

### Pitfall 5: Holes Outside Exterior
**What goes wrong:** Interior rings that fall completely outside the exterior ring cause unexpected fill behavior.

**Why it happens:** Data quality issues or projection artifacts.

**How to avoid:** Fast check: hole's first vertex should be inside exterior's bounding box. Optional: point-in-polygon test for strict validation.

**Warning signs:** Holes appear in wrong locations or don't render at all.

## Code Examples

### Antimeridian Detection and Splitting
```kotlin
/**
 * Detects if a ring crosses the antimeridian (±180° longitude).
 * Uses the heuristic: if any adjacent pair has a longitude jump > 180°, it's a crossing.
 */
fun crossesAntimeridian(ring: List<Vector2>): Boolean {
    if (ring.size < 2) return false
    
    for (i in ring.indices) {
        val current = ring[i]
        val next = ring[(i + 1) % ring.size]
        if (kotlin.math.abs(next.x - current.x) > 180.0) {
            return true
        }
    }
    return false
}

/**
 * Interpolates latitude at antimeridian crossing.
 * Linear interpolation between two points crossing the ±180° boundary.
 */
fun interpolateAntimeridianCrossing(p1: Vector2, p2: Vector2): Double {
    // Calculate how far along the segment the crossing occurs
    val deltaLon = if (p1.x > 0) {
        (180.0 - p1.x) + (p2.x + 180.0)  // Crossing from + to -
    } else {
        (-180.0 - p1.x) + (p2.x - 180.0)  // Crossing from - to +
    }
    
    val t = if (p1.x > 0) {
        (180.0 - p1.x) / deltaLon
    } else {
        (-180.0 - p1.x) / deltaLon
    }
    
    return p1.y + t * (p2.y - p1.y)
}
```

### Winding-Aware Shape Creation
```kotlin
/**
 * Creates a Shape from polygon data with proper winding for holes.
 * Assumes coordinates are already in screen space.
 */
fun createShapeWithHoles(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>
): org.openrndr.shape.Shape {
    val contours = mutableListOf<ShapeContour>()
    
    // Exterior: enforce clockwise (positive fill)
    val extContour = ShapeContour.fromPoints(exterior, closed = true)
    contours.add(extContour.clockwise)
    
    // Interiors: enforce counter-clockwise (negative fill = holes)
    interiors.forEach { ring ->
        if (ring.size >= 3) {
            val holeContour = ShapeContour.fromPoints(ring, closed = true)
            contours.add(holeContour.counterClockwise)
        }
    }
    
    return org.openrndr.shape.Shape(contours)
}
```

### MultiPolygon as Single Shape
```kotlin
/**
 * Renders a MultiPolygon as a single Shape for seamless rendering.
 * Combines all polygons and their holes into one Shape with non-zero winding.
 */
fun drawMultiPolygonSeamless(
    drawer: Drawer,
    multiPolygon: MultiPolygon,
    projection: GeoProjection,
    style: Style
) {
    val allContours = mutableListOf<ShapeContour>()
    
    multiPolygon.polygons.forEach { polygon ->
        // Project and normalize exterior
        val screenExterior = polygon.exterior.map { projection.project(it) }
        val extContour = ShapeContour.fromPoints(screenExterior, closed = true).clockwise
        allContours.add(extContour)
        
        // Project and normalize holes
        polygon.interiors.forEach { ring ->
            if (ring.size >= 3) {
                val screenRing = ring.map { projection.project(it) }
                val holeContour = ShapeContour.fromPoints(screenRing, closed = true).counterClockwise
                allContours.add(holeContour)
            }
        }
    }
    
    // Single draw call with all contours
    drawer.shape(org.openrndr.shape.Shape(allContours))
}
```

### Validation Without Repair
```kotlin
/**
 * Validates interior rings without attempting repair.
 * Logs warnings for data quality issues but renders original data.
 */
fun validateInteriorRings(
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    featureId: String? = null
): List<List<Vector2>> {
    val validRings = mutableListOf<List<Vector2>>()
    val exteriorBounds = calculateBounds(exterior)
    
    interiors.forEachIndexed { index, ring ->
        // Check 1: Minimum vertex count
        if (ring.size < 3) {
            logger.warn { 
                "Degenerate interior ring (feature=${featureId}, ring=$index): " +
                "only ${ring.size} vertices, minimum is 3"
            }
            return@forEachIndexed
        }
        
        // Check 2: Non-zero area
        if (calculateArea(ring).absoluteValue < 1e-10) {
            logger.warn {
                "Degenerate interior ring (feature=${featureId}, ring=$index): " +
                "near-zero area"
            }
            return@forEachIndexed
        }
        
        // Check 3: Hole inside exterior bounds (fast check)
        val firstPoint = ring[0]
        if (!exteriorBounds.contains(firstPoint.x, firstPoint.y)) {
            logger.warn {
                "Interior ring outside exterior bounds (feature=${featureId}, ring=$index): " +
                "first vertex (${firstPoint.x}, ${firstPoint.y}) outside exterior bbox"
            }
            // Still render it — don't repair, just warn
        }
        
        validRings.add(ring)
    }
    
    return validRings
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Per-polygon rendering with `drawPolygon()` | Single Shape with combined contours | Phase 16 | Eliminates overdraw and seams; proper hole support |
| Runtime winding checks | Load-time normalization | Phase 16 | Canonical data, scattered defensive logic removed |
| Coordinate clamping only | Antimeridian splitting | Phase 16 | Fixes world-spanning artifacts in ocean data |
| JTS/GEOS for validation | Lightweight validate-but-warn | Phase 16 | No heavy dependencies, creative-coding appropriate |

**Deprecated/outdated:**
- None — this is new capability being added

## Open Questions

1. **Logger implementation choice**
   - What we know: mu.KotlinLogging is available via OPENRNDR
   - What's unclear: Whether to create a new logger instance per validator or use a shared one
   - Recommendation: Shared logger for the `geo.render` package, tagged with feature IDs

2. **Point-in-polygon test necessity**
   - What we know: Bounding box check is fast but coarse
   - What's unclear: Whether strict point-in-polygon validation is worth the cost
   - Recommendation: Start with bbox check only; add PIP if real data shows false negatives

3. **Geometry caching for normalized results**
   - What we know: Normalization happens at load time per 16-CONTEXT.md
   - What's unclear: Whether to cache split/normalized geometries for repeated loads
   - Recommendation: Don't cache initially; add if profiling shows load-time bottlenecks

4. **Test data coverage**
   - What we know: `data/geo/ocean.geojson` has antimeridian-crossing MultiPolygons
   - What's unclear: Whether we need synthetic test data for edge cases (exactly ±180°, poles)
   - Recommendation: Use ocean.geojson + create synthetic cases for unit tests

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 (existing project standard) |
| Config file | None — tests use standard JUnit annotations |
| Quick run command | `./gradlew test --tests "geo.render.*"` |
| Full suite command | `./gradlew test` |
| Estimated runtime | ~30 seconds (full suite includes regression tests) |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| RENDER-01 | Antimeridian crossing detection | unit | `./gradlew test --tests "geo.geometry.AntimeridianSplitterTest"` | ❌ Wave 0 gap |
| RENDER-01 | Ring splitting and interpolation | unit | `./gradlew test --tests "geo.geometry.AntimeridianSplitterTest"` | ❌ Wave 0 gap |
| RENDER-01 | MultiPolygon as single Shape | integration | `./gradlew test --tests "geo.render.MultiPolygonRenderingTest"` | ❌ Wave 0 gap |
| RENDER-02 | Winding normalization | unit | `./gradlew test --tests "geo.geometry.WindingNormalizerTest"` | ❌ Wave 0 gap |
| RENDER-02 | Exterior/interior ring classification | unit | `./gradlew test --tests "geo.geometry.RingValidatorTest"` | ❌ Wave 0 gap |
| RENDER-02 | Degenerate ring detection | unit | `./gradlew test --tests "geo.geometry.RingValidatorTest"` | ❌ Wave 0 gap |
| RENDER-02 | Hole-inside-exterior validation | unit | `./gradlew test --tests "geo.geometry.RingValidatorTest"` | ❌ Wave 0 gap |
| RENDER-01, 02 | Ocean data rendering | visual regression | Run example with ocean.geojson | ❌ Manual verification |

### Nyquist Sampling Rate
- **Minimum sample interval:** After every committed task → run: `./gradlew test --tests "geo.render.*"`
- **Full suite trigger:** Before merging final task of any plan wave
- **Phase-complete gate:** Full suite green before `/gsd-verify-work` runs
- **Estimated feedback latency per task:** ~15 seconds (fast render tests)

### Wave 0 Gaps (must be created before implementation)
- [ ] `src/test/kotlin/geo/geometry/AntimeridianSplitterTest.kt` — covers RENDER-01 antimeridian detection/splitting
- [ ] `src/test/kotlin/geo/geometry/WindingNormalizerTest.kt` — covers RENDER-02 winding normalization
- [ ] `src/test/kotlin/geo/geometry/RingValidatorTest.kt` — covers RENDER-02 validation rules
- [ ] `src/test/kotlin/geo/render/MultiPolygonRenderingTest.kt` — covers RENDER-01 seamless MultiPolygon rendering
- [ ] `src/main/kotlin/geo/render/geometry/` directory — create package for normalization utilities

## Sources

### Primary (HIGH confidence)
- OPENRNDR Shape API source code (github.com/openrndr/openrndr) — Shape, ShapeContour, Winding
- 16-CONTEXT.md — Locked decisions from discuss phase
- REQUIREMENTS.md — RENDER-01, RENDER-02 requirements
- src/main/kotlin/geo/render/PolygonRenderer.kt — Current hole rendering implementation
- src/main/kotlin/geo/render/MultiRenderer.kt — Current MultiPolygon rendering

### Secondary (MEDIUM confidence)
- Turf.js @turf/rewind, @turf/bbox-clip — Reference algorithms for antimeridian handling
- GDAL ogr2ogr -wrapdateline — Alternative preprocessing approach
- GeoJSON specification — Winding order conventions (RFC 7946)

### Tertiary (LOW confidence)
- None — all key findings verified with source code

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — OPENRNDR API verified with source code
- Architecture: HIGH — Decisions locked in CONTEXT.md
- Pitfalls: HIGH — Based on observed rendering artifacts in ocean data

**Research date:** 2026-03-08
**Valid until:** 30 days (stable OPENRNDR 0.4.5 release)
