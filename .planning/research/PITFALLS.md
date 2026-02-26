# Domain Pitfalls — v1.2.0 API Improvements

**Domain:** Geospatial Visualization Library (Kotlin/OpenRNDR)
**Milestone:** v1.2.0 — Adding API improvements and examples
**Researched:** 2026-02-26
**Confidence:** HIGH (based on codebase analysis + domain expertise)

---

## Critical Pitfalls

Mistakes that cause rewrites or major issues when adding these specific features.

---

### Pitfall 1: GeoSource Summary Functions — Costly Operations on Lazy Sequences

**What goes wrong:**
Calling `countFeatures()` or `totalBoundingBox()` on a lazy Sequence-backed GeoSource iterates through ALL features. For large datasets (12GB GeoPackage), this causes:
- Multi-second delays on first access
- Memory pressure as sequences are consumed
- User confusion ("why is my app frozen?")

**Why it happens:**
- Sequence is lazy — no cached count/bounds
- Developers expect O(1) property access
- `countFeatures()` default implementation iterates everything

**Consequences:**
- App appears frozen on data inspection
- Performance tests fail intermittently (depending on sequence state)
- Users blame the library for "slow rendering" when actually inspection is slow

**Prevention:**
```kotlin
// BAD: Iterates entire sequence
fun countFeatures(): Long = features.count().toLong()

// GOOD: Make cost explicit
fun countFeatures(): Long {
    // Warning: O(n) operation - materializes sequence
    return features.count().toLong()
}

// BETTER: Provide both lazy and eager options
fun countFeaturesLazy(): Long  // May require iteration
fun countFeaturesEager(): Long // From cached metadata
```

**Detection:**
- Test with 100k+ feature dataset
- Profile: does `summary()` take > 100ms?
- Add `@Deprecated("Use countFeaturesEager() for large datasets")` if needed

**Phase to address:** Phase implementing GeoSource inspection API

---

### Pitfall 2: Polygon Interior Rings — Silent Data Loss

**What goes wrong:**
Current `drawPolygon()` only renders the exterior ring. Polygons with holes (lakes, courtyards, donuts) render as solid shapes — holes disappear silently.

**Why it happens:**
- OpenRNDR's `ShapeContour.fromPoints()` creates a single closed contour
- Holes require `Shape` with multiple contours (outline + holes)
- Easy to forget interior rings exist

**Current code (from Geometry.kt):**
```kotlin
// GeoSource.kt line 205-207 — HOLES IGNORED
is Polygon -> {
    val screenPoints = exterior.map { projection.project(it) }
    drawPolygon(drawer, screenPoints, style)
}
```

**Consequences:**
- Lake polygons show as solid blue (no hole)
- Building footprints with courtyards render incorrectly
- Data appears "wrong" but no error is thrown

**Prevention:**
```kotlin
// Use OpenRNDR Shape for polygons with holes
fun drawPolygonWithHoles(
    drawer: Drawer,
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    style: Style
) {
    if (interiors.isEmpty()) {
        drawPolygon(drawer, exterior, style) // Simple case
    } else {
        // Create Shape with multiple contours
        val shape = shape {
            contour { /* exterior */ }
            interiors.forEach { hole ->
                contour { /* hole (reversed winding) */ }
            }
        }
        drawer.shape(shape)
    }
}
```

**Detection:**
- Test with GeoJSON containing Polygon with `holes` array
- Visual test: Swiss cheese polygon should show holes
- Assert: `polygon.interiors.isNotEmpty()` renders differently than exterior-only

**Warning signs:**
- "My lake polygons are rendering solid"
- "GeoJSON validates but looks wrong"
- Coastal polygons showing land where water should be

**Phase to address:** Phase implementing polygon ring rendering

---

### Pitfall 3: API Boilerplate Reduction — Over-Simplification That Removes Control

**What goes wrong:**
Convenience APIs that are too simple:
1. **Hide necessary configuration** — Users can't set padding, projection type, or CRS
2. **Assume wrong defaults** — Mercator for polar data, WGS84 for BNG data
3. **Block escape hatches** — No way to access underlying objects

**Example of over-simplification:**
```kotlin
// TOO SIMPLE — hides projection choice
fun Drawer.geoJSON(path: String) {
    // Hardcoded Mercator, hardcoded padding, no CRS option
}

// BETTER — simple default, escape hatch available
fun Drawer.geoJSON(
    path: String,
    projection: GeoProjection? = null,  // Allow override
    style: Style? = null
) {
    // Uses provided projection, or fits automatically
}
```

**Why it happens:**
- "Make it simple" pressure leads to removing parameters
- Demo code works with test data, fails with real data
- Different users need different defaults

**Consequences:**
- Users abandon convenience API, go back to verbose approach
- "The library doesn't support X" (it does, just not in convenience API)
- GitHub issues requesting features that already exist

**Prevention:**
Follow the **Tiered API Pattern** (already in codebase):
```kotlin
// Tier 1: One-liner (sensible defaults)
drawer.geoJSON("world.json")

// Tier 2: Load once, configure
val source = geoSource("data.json")
source.render(drawer, projection, style)

// Tier 3: Full control
val features = GeoJSON.load("data.json")
features.forEach { /* custom logic */ }
```

**Key principle:** Convenience APIs must have **escape hatches** — optional parameters that expose underlying control.

**Phase to address:** Phase implementing boilerplate reduction

---

### Pitfall 4: MultiPolygon Projection — Per-Polygon vs Whole-Geometry Clamping

**What goes wrong:**
Two approaches to coordinate clamping in MultiPolygon, both have issues:

1. **Clamp each polygon independently** — Creates visual artifacts at polygon boundaries
2. **Clamp the whole MultiPolygon** — Requires expensive flatten/reconstruct

**Current code (MultiRenderer.kt):**
```kotlin
fun drawMultiPolygon(...) {
    val polygonsToRender = if (clampToMercatorBounds && projection is ProjectionMercator) {
        multiPolygon.polygons.map { polygon ->
            polygon.exterior.map { coord ->
                Vector2(coord.x, coord.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            }
        }
    } else { ... }
}
```

**Issue:** Only clamps exterior, ignores interior rings (holes).

**Consequences:**
- MultiPolygon with holes renders incorrectly at high latitudes
- Some polygons show, others disappear (depending on clamp logic)
- Inconsistent behavior between simple Polygon and MultiPolygon

**Prevention:**
```kotlin
// Clamp ALL coordinates, including holes
fun MultiPolygon.clampedToMercator(): MultiPolygon {
    return MultiPolygon(
        polygons.map { poly ->
            Polygon(
                exterior = poly.exterior.map { clampCoord(it) },
                interiors = poly.interiors.map { ring ->
                    ring.map { clampCoord(it) }
                }
            )
        }
    )
}
```

**Detection:**
- Test with MultiPolygon spanning > 85° latitude
- Test with MultiPolygon containing holes at high latitude
- Visual comparison: same geometry as Polygon vs MultiPolygon

**Phase to address:** Phase implementing MultiPolygon improvements

---

### Pitfall 5: Batch Coordinate Processing — Allocation Storm

**What goes wrong:**
Naive coordinate transformation allocates intermediate objects per-coordinate:

```kotlin
// BAD: Allocates List + N Vector2s per geometry
fun projectAll(points: List<Vector2>): List<Vector2> {
    return points.map { projection.project(it) }  // New list + new Vector2s
}

// In render loop with 100k points:
// 60 fps × 100k points = 6M allocations/second
// GC pressure causes frame drops
```

**Why it happens:**
- Kotlin's functional style encourages `.map { }` chains
- Each `.map` creates new collection
- Vector2 is immutable (must create new instances)

**Consequences:**
- Smooth animation at 1000 points, stuttering at 100k points
- GC logs show frequent young-gen collections
- "Works on my machine" (faster CPU hides the problem)

**Prevention:**
```kotlin
// Option 1: Pre-compute and cache screen coordinates
data class CachedGeometry(
    val original: Geometry,
    val screenCoords: List<Vector2>  // Computed once
)

// Option 2: Mutable buffer with reuse (for hot loops)
class CoordinateBuffer(size: Int) {
    private val buffer = DoubleArray(size * 2)
    fun projectInto(source: List<Vector2>, projection: GeoProjection)
}

// Option 3: Materialize GeoSource once, not per-frame
val source = geoSource("large.json").materialize()  // Pay once
```

**Detection:**
- Profile with Android Studio/JProfiler
- Count allocations per frame
- Test with dataset 10x larger than typical

**Phase to address:** Phase implementing coordinate batch processing

---

### Pitfall 6: Educational Examples — Runnable vs Educational

**What goes wrong:**
Examples that prioritize being concise over being educational:

```kotlin
// BAD: Too concise, hides concepts
fun main() = application {
    program {
        extend { drawer.geoJSON("data.json") }
    }
}
// User learns: "call geoJSON()"
// User doesn't learn: projections, features, styling, geometry types

// BAD: Too complex, overwhelms
fun main() = application {
    program {
        val source = GeoJSON.load("data.json", CRS.WebMercator)
            .filterFeatures { it.properties["population"] > 100000 }
            .materialize()
        val projection = ProjectionFactory.fitBounds(
            source.totalBoundingBox(),
            width.toDouble(),
            height.toDouble(),
            padding = 0.9,
            projection = ProjectionType.MERCATOR
        )
        // ... 50 more lines
    }
}
```

**Why it happens:**
- Developer writes example to test feature, not teach it
- "Working code" != "Educational code"
- No clear progression from simple to complex

**Consequences:**
- Users copy-paste without understanding
- "How do I customize X?" questions on trivial changes
- Library appears more complex than it is

**Prevention:**
Follow **Progressive Disclosure** in examples:
```kotlin
// Example 1: Minimal (show the concept)
// Comments explain WHAT each line does

// Example 2: Common customization (show options)
// Comments explain WHY you'd change each thing

// Example 3: Full control (show escape hatches)
// Comments link to API docs for details
```

**Example file naming convention:**
```
render_BasicRendering.kt      // Minimal: load + render
render_CustomStyle.kt         // Medium: styling options
render_MultiProjection.kt     // Advanced: projection switching
```

**Detection:**
- Can a new user understand the example in < 5 minutes?
- Does the example demonstrate ONE concept clearly?
- Are there comments explaining non-obvious choices?

**Phase to address:** Phase implementing educational examples

---

## Moderate Pitfalls

### Pitfall 7: Inspection API Naming — Confusing "Summary" vs "Stats"

**What goes wrong:**
Inconsistent naming between `summary()`, `stats()`, `info()`, `describe()` — users don't know which to call.

**Prevention:** Pick ONE term and use consistently:
```kotlin
// Choose "inspect" as the term
fun GeoSource.inspect(): GeoSourceInspection
data class GeoSourceInspection(
    val featureCount: Long,
    val bounds: Bounds,
    val geometryTypes: Set<String>,
    val propertyKeys: Set<String>
)
```

**Phase to address:** Phase implementing inspection API

---

### Pitfall 8: Missing Geometry Type in Summary — Incomplete Information

**What goes wrong:**
Summary shows feature count and bounds but NOT geometry types. User loads file, sees 1000 features, assumes all are Polygons — but 999 are Points.

**Prevention:**
```kotlin
data class GeoSourceSummary(
    val featureCount: Long,
    val bounds: Bounds,
    val geometryBreakdown: Map<String, Int>,  // "Point" -> 50, "Polygon" -> 10
    val crs: String
)
```

**Phase to address:** Phase implementing inspection API

---

### Pitfall 9: Convenience API CRS Assumptions — Wrong Defaults

**What goes wrong:**
`drawer.geoJSON(path)` assumes WGS84. If file is in British National Grid, coordinates are interpreted as lat/lng and projection fails.

**Prevention:**
- Read CRS from GeoJSON `crs` property (if present)
- Provide explicit `crs` parameter in convenience API
- Log warning when CRS is assumed

**Phase to address:** Phase implementing boilerplate reduction

---

## Minor Pitfalls

### Pitfall 10: Example Data Not Included — "File not found" Errors

**What goes wrong:**
Examples reference `"data/sample.geojson"` but file isn't in repo. Users clone, run example, get FileNotFoundException.

**Prevention:**
- Include minimal sample data in `data/` directory
- Or generate sample data programmatically in example
- Document where to get larger test datasets

**Phase to address:** Phase implementing educational examples

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| GeoSource inspection | O(n) operations on lazy sequences | Provide both lazy and eager variants; document cost |
| Polygon ring rendering | Interior rings ignored | Use Shape for multi-contour; test with holes |
| API boilerplate reduction | Over-simplification removes control | Tiered API with escape hatches |
| MultiPolygon projection | Inconsistent clamping between Polygon/MultiPolygon | Unified clamp function for all geometry types |
| Coordinate batch processing | Allocation storm in render loop | Pre-compute, cache, or use mutable buffers |
| Educational examples | Too concise OR too complex | Progressive disclosure; one concept per example |

---

## Integration Pitfalls

| Integration Point | Common Mistake | Correct Approach |
|-------------------|----------------|------------------|
| OpenRNDR Shape for holes | Single ShapeContour | Multiple contours in Shape builder |
| CRS in convenience API | Assume WGS84 | Read from file, allow override |
| Projection fitting | Fit every frame | Fit once, cache projection |
| Geometry transformation | Forget interior rings | Recursive transform of all coordinates |

---

## Prevention Checklist by Feature

### GeoSource Inspection API
- [ ] Document O(n) cost of lazy operations
- [ ] Provide eager variants for large datasets
- [ ] Include geometry type breakdown in summary
- [ ] Test with 100k+ feature dataset

### Polygon Ring Rendering
- [ ] Render interior rings (holes), not just exterior
- [ ] Use OpenRNDR Shape for multi-contour
- [ ] Test with GeoJSON containing holes
- [ ] Verify winding order (right-hand rule)

### API Boilerplate Reduction
- [ ] Keep Tier 3 (full control) API unchanged
- [ ] Add optional parameters for escape hatches
- [ ] Document what defaults are assumed
- [ ] Test with non-default CRS/projection

### MultiPolygon Handling
- [ ] Clamp interior rings, not just exterior
- [ ] Consistent behavior with single Polygon
- [ ] Test with high-latitude MultiPolygons
- [ ] Test with MultiPolygons containing holes

### Coordinate Batch Processing
- [ ] Profile allocations per frame
- [ ] Provide caching mechanism
- [ ] Test with 100k+ coordinate dataset
- [ ] Document performance tradeoffs

### Educational Examples
- [ ] One concept per example file
- [ ] Comments explain non-obvious code
- [ ] Include runnable sample data
- [ ] Progressive complexity across examples

---

## Sources

- **OpenRNDR Shape/ShapeContour**: Official guide on curves and shapes — Shape for holes
- **GIS StackExchange**: Polygon holes in GeoJSON — winding order, rendering issues
- **Codebase Analysis**: Geometry.kt, GeoSource.kt, MultiRenderer.kt, PolygonRenderer.kt
- **Existing PITFALLS.md**: v1.0 CRS, memory, validation pitfalls (still relevant)

---

*Pitfalls research for: openrndr-geo v1.2.0 API Improvements*
*Researched: 2026-02-26*
