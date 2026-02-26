# Research Summary — v1.2.0 API Improvements

**Project:** openrndr-geo
**Domain:** Geospatial visualization library (Kotlin/OpenRNDR)
**Milestone:** v1.2.0 — API improvements and examples
**Researched:** 2026-02-26
**Confidence:** HIGH

---

## Executive Summary

v1.2.0 is an **API polish milestone** focused on filling gaps in the existing library rather than adding new capabilities. The research reveals that **no new dependencies are required** — all features leverage existing OpenRNDR 0.4.5 APIs (especially `Shape`/`ShapeContour` for polygon holes). The three priority features are: (1) `GeoSource.summary()` for data inspection, (2) polygon interior ring rendering (currently a TODO), and (3) batch coordinate projection for animation performance.

The main risk is **performance surprises from lazy sequences** — `summary()` operations are O(n) and can freeze apps on large GeoPackage datasets. Mitigation: document costs clearly and provide both lazy/eager variants. Secondary risk is **silent data loss** when polygon holes are ignored — current code renders only exterior rings, making lakes appear solid. This requires careful implementation using OpenRNDR's `Shape` API with multiple contours.

---

## Key Findings

### Recommended Stack

**No new dependencies needed.** Existing stack supports all v1.2.0 features:

| Technology | Purpose | v1.2.0 Use |
|------------|---------|------------|
| OpenRNDR 0.4.5 | Graphics/rendering | `Shape` API for polygon holes |
| kotlinx-serialization | JSON parsing | Already used for GeoJSON |
| proj4j | CRS transforms | No changes needed |
| Kotlin stdlib | Extension functions | `summary()`, convenience APIs |

**Key API to leverage:** `org.openrndr.shape.Shape` with multiple `ShapeContour` objects — exterior clockwise, holes counter-clockwise.

### Feature Breakdown

**Priority 1 — Table Stakes (must have):**

| Feature | Why | Complexity |
|---------|-----|------------|
| `GeoSource.summary()` | Users need to understand data before rendering; standard in Turf.js, GeoPandas | Low |
| Polygon interior ring rendering | GeoJSON RFC 7946 mandates holes; current TODO leaves feature incomplete | Medium |
| MultiPolygon bounds handling | Large datasets exceed Mercator limits; current implementation ignores interior rings | Low |

**Priority 2 — Differentiators (should have):**

| Feature | Value | Complexity |
|---------|-------|------------|
| Batch coordinate projection | Cache projected coords for animation; most libs project per-frame | Medium |
| Rendering boilerplate reduction | One-liner rendering with smart defaults | Low-Medium |

**Defer to v2+:**
- Geometry clipping (vs clamping) — complex, rarely needed
- Full GeoJSON validation — slow, most data is valid
- Property-based styling DSL — better handled by user code

### Architecture Integration

All v1.2.0 features integrate into **existing modules** — no new packages required:

```
geo/
├── GeoSource.kt           ← Add summary()
├── GeoSourceSummary.kt    ← NEW data class
├── Geometry.kt            ← Enhance clampToMercator() for interiors
├── GeoSourceConvenience.kt ← Add convenience functions
│
├── render/
│   ├── PolygonRenderer.kt ← Modify for interior rings
│   └── MultiRenderer.kt   ← Use enhanced clamping
│
├── projection/
│   └── ProjectionExtensions.kt ← Add batch projection
│
└── examples/              ← Add 5 new demo files
```

**Integration pattern:** Extend existing APIs rather than create parallel paths. Use default parameters for backward compatibility.

### Critical Pitfalls

| Pitfall | Impact | Prevention |
|---------|--------|------------|
| **Lazy sequence O(n) operations** | `summary()` freezes app on 12GB GeoPackage | Document cost; provide eager variant |
| **Silent polygon hole loss** | Lakes render solid, no error thrown | Use `Shape` with multiple contours; test with holes |
| **Over-simplified convenience API** | Users can't customize projection/CRS | Keep escape hatches via optional parameters |
| **MultiPolygon clamp ignores interiors** | Holes render incorrectly at high latitudes | Clamp all rings, not just exterior |
| **Allocation storm in render loop** | GC pressure causes frame drops | Pre-project and cache screen coordinates |

---

## Implications for Roadmap

### Suggested Phase Structure

#### Phase 1: Data Inspection API
**Rationale:** Foundation feature with no dependencies; enables debugging workflows.
**Delivers:** `GeoSource.summary()` + `GeoSourceSummary` data class
**Files:** `GeoSource.kt`, new `GeoSourceSummary.kt`
**Avoids:** Pitfall #1 (document O(n) cost, provide lazy/eager variants)
**Example:** `api_SummaryDemo.kt`

#### Phase 2: Polygon Ring Rendering
**Rationale:** Closes feature gap (current TODO); required before MultiPolygon improvements.
**Delivers:** Interior ring rendering via OpenRNDR `Shape` API
**Files:** `PolygonRenderer.kt`, `Geometry.kt`
**Avoids:** Pitfall #2 (use Shape with multiple contours, test with holes)
**Example:** `render_PolygonHoles.kt`

#### Phase 3: MultiPolygon Bounds Enhancement
**Rationale:** Builds on Phase 2; applies ring handling to MultiPolygon clamping.
**Delivers:** Interior ring clamping in `clampToMercator()`
**Files:** `Geometry.kt`, `MultiRenderer.kt`
**Avoids:** Pitfall #4 (clamp all rings, not just exterior)
**Example:** `render_OceanData.kt`

#### Phase 4: Convenience API Layer
**Rationale:** Polish layer that depends on core features working correctly.
**Delivers:** One-line rendering shortcuts, style presets
**Files:** `GeoSourceConvenience.kt`, `DrawerGeoExtensions.kt`
**Avoids:** Pitfall #3 (keep escape hatches via optional parameters)
**Example:** `api_BoilerplateFree.kt`

#### Phase 5: Batch Projection (Optional)
**Rationale:** Performance optimization for animation use cases; can defer if time-constrained.
**Delivers:** Pre-projected geometry cache, `projectOnce()` extension
**Files:** New `ScreenSpace.kt` or extend `ProjectionExtensions.kt`
**Avoids:** Pitfall #5 (cache screen coordinates, avoid per-frame allocation)
**Example:** `perf_BatchProjection.kt`

#### Phase 6: Documentation & Examples
**Rationale:** Final phase to document all features with runnable examples.
**Delivers:** 5+ new example files, updated README
**Avoids:** Pitfall #6 (progressive disclosure, one concept per example)

### Phase Ordering Rationale

1. **Phase 1 → Phase 2:** Summary API has no dependencies; polygon rings are independent but benefit from inspection for debugging
2. **Phase 2 → Phase 3:** MultiPolygon clamp needs ring handling from Phase 2
3. **Phase 4 depends on 1-3:** Convenience API wraps core features
4. **Phase 5 is optional:** Performance optimization can ship in v1.2.1 if needed
5. **Phase 6 is last:** Examples demonstrate all completed features

### Research Flags

**Phases needing deeper research during planning:**
- **Phase 5 (Batch Projection):** Performance optimization needs benchmarking; exact API shape depends on profiling results

**Phases with standard patterns (skip research-phase):**
- **Phase 1 (Inspection):** Straightforward aggregation, well-documented patterns
- **Phase 2 (Polygon Rings):** OpenRNDR Shape API is well-documented
- **Phase 6 (Examples):** Follow existing naming convention

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Verified via OpenRNDR API docs, existing build.gradle.kts |
| Features | HIGH | Based on codebase analysis, GeoJSON RFC 7946, industry patterns |
| Architecture | HIGH | Direct codebase analysis of existing integration points |
| Pitfalls | HIGH | Derived from actual TODO comments and known issues |

**Overall confidence:** HIGH

### Gaps to Address

| Gap | Resolution |
|-----|------------|
| Exact batch projection API shape | Profile during Phase 5 implementation; may need `ProjectedGeoSource` wrapper or simpler extension |
| Performance thresholds for lazy vs eager | Test with 100k+ feature datasets during Phase 1 |
| Sample data for examples | Generate minimal test GeoJSON or include in `data/` directory |

---

## Open Questions for User

1. **Batch projection priority:** Is Phase 5 (performance optimization) required for v1.2.0, or can it defer to v1.2.1?

2. **Summary API naming:** Prefer `summary()`, `inspect()`, or `describe()`? (Research suggests `summary()` matches GeoPandas conventions)

3. **Example data strategy:** Should examples include minimal sample data in repo, or generate programmatically?

4. **Convenience API scope:** Which shortcuts are most valuable?
   - `source.renderFit(drawer)` — auto-fit projection
   - `Style.presets.redOutline` — common styles
   - `geometry.draw(drawer, projection)` — method on geometry

5. **Documentation depth:** Update README only, or add API guide document?

---

## Sources

### Primary (HIGH confidence)
- OpenRNDR Shape API: https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape/
- OpenRNDR ShapeContour: https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape-contour/
- GeoJSON RFC 7946: https://tools.ietf.org/html/rfc7946
- Existing codebase: `src/main/kotlin/geo/**/*.kt`

### Secondary (MEDIUM confidence)
- Shapely documentation: https://shapely.readthedocs.io/
- Turf.js meta functions: https://turfjs.org/
- openrndr-examples: https://github.com/openrndr/openrndr-examples

### Tertiary (domain expertise)
- GIS StackExchange: polygon hole rendering issues
- Creative coding patterns: Processing, p5.js example conventions

---

*Research completed: 2026-02-26*
*Ready for roadmap: yes*
