# Phase 16: Rendering Improvements - Context

**Gathered:** 2026-03-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Fix MultiPolygon rendering for ocean/whole-world data and improve polygon interior/exterior ring handling.

This phase addresses:
- MultiPolygons spanning the antimeridian (±180° longitude) render incorrectly — coordinates jump from +180 to -180 creating world-spanning artifacts
- Polygon ring winding order is inconsistent in source data, causing fill anomalies
- Interior ring validation is absent — degenerate or misplaced holes may cause rendering issues
- MultiPolygon rendering iterates polygons independently, causing overdraw and seams at shared boundaries

Requirements: RENDER-01, RENDER-02

</domain>

<decisions>
## Implementation Decisions

### Winding order handling
- **Normalize at load time**, not render time
- After projecting lat/lon to screen pixels (Y inverted), the visual winding convention is reversed from geographic:
  - Exterior rings → `.clockwise` in screen space
  - Interior rings (holes) → `.counterClockwise` in screen space
- Produces canonical `Shape` objects that work reliably downstream
- Rationale: OPENRNDR's `Shape` fill depends on relative winding; deferring gains nothing and scatters defensive logic

### Antimeridian/dateline crossing
- **Split at the antimeridian** combined with **clip and close** at load time
- Algorithm for rings crossing ±180° longitude:
  1. Walk coordinate pairs; detect crossings via `abs(lon₁ − lon₂) > 180`
  2. Interpolate new vertices at exactly +180 / -180 on the boundary
  3. Split into left ring (≤ +180) and right ring (≥ -180)
  4. Close each partial ring along the boundary edge at appropriate latitudes
  5. Emit as separate polygons in the MultiPolygon
- Run normal winding normalization pass on each resulting polygon
- Alternative: Use `ogr2ogr -wrapdateline` for pre-processing if preferred
- Rationale: Flat projected viewport needs disambiguated geometry; splitting composes cleanly with winding normalization, bounding-box queries, and spatial indexing

### Interior ring validation
- **Validate but don't repair** — check problems, log warnings, render original data
- Specific validation rules:
  1. **Drop degenerate rings** (< 3 distinct vertices or near-zero signed area) — log warning and remove
  2. **Check hole-inside-exterior** — test if hole's first vertex lies inside exterior bounding box (fast) and optionally inside exterior contour; log warning if outside
  3. **Skip self-intersection checks** — too expensive; rely on visual artifacts being obvious
  4. **Skip overlapping hole checks** — too expensive; non-zero winding handles overlap acceptably
- All warnings go to logger (not stdout), tagged with feature ID/index for traceability
- Do NOT pull in JTS/GEOS for automatic repair — too heavy for creative coding context
- Rationale: Creative coding tool prioritizes visibility over correction; crashing on bad data is unacceptable, but silent repair risks mystery bugs

### MultiPolygon rendering
- **Single Shape with non-zero winding rule**, not independent per-polygon rendering
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

</decisions>

<specifics>
## Specific Ideas

### Test data
- `data/geo/ocean.geojson` — contains MultiPolygons crossing antimeridian with -180.0 and +180.0 coordinates
- Use for validating antimeridian split algorithm

### Reference implementations
- Turf.js `@turf/rewind` and `@turf/bbox-clip` — similar splitting approach
- GDAL `ogr2ogr -wrapdateline` — pre-processing alternative

### Rendering pattern
Current code (to be changed):
```kotlin
polygons.forEach { poly ->
    val screenPoints = poly.exterior.map { projection.project(it) }
    drawPolygon(drawer, screenPoints, style)
}
```

Target code:
```kotlin
val contours = polygons.flatMap { poly ->
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
drawer.shape(Shape(contours))
```

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 16-rendering-improvements*
*Context gathered: 2026-03-08*
