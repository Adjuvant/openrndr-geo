# Roadmap: v1.3.0 Performance

**Milestone:** v1.3.0 Performance  
**Goal:** Optimize rendering performance through batch projection and simple viewport caching.  
**Target:** 10x+ improvement for static camera scenarios.  
**Achieved:** 1533x average speedup (Phase 13-01)  
**Last Updated:** 2026-03-07 (Phase 14 complete - v1.3.0 ready for release)

## Phases

### v1.3.0 Performance (Complete)

- [x] **Phase 11: Batch Projection** - Transform coordinate arrays efficiently (2/2 plans complete)
- [x] **Phase 12: Viewport Caching** - Simple cache with clear-on-change semantics (completed 2026-03-06)
- [x] **Phase 13: Integration & Validation** - Verify all v1.2.0 examples work unchanged (completed 2026-03-07)
- [x] **Phase 14: Refactoring and Cleanup** - Clear all TODOs and technical debt (6/6 plans complete - Phase finished)

### v1.4.0 Developer Experience (Planned)

- [ ] **Phase 15: API Ergonomics** - Reduce boilerplate for common rendering workflows
- [ ] **Phase 16: Rendering Improvements** - Fix MultiPolygon and polygon ring handling
- [ ] **Phase 17: Performance Fixes** - ViewportCache integration for optimized rendering
- [ ] **Phase 18: Code Organization** - File structure and navigation improvements
- [ ] **Phase 19: Documentation Fixes** - README corrections and updates

## Progress

### v1.3.0 Performance

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 11. Batch Projection | 2/2 | Complete    | 2026-03-05 |
| 12. Viewport Caching | 3/3 | Complete   | 2026-03-06 |
| 13. Integration & Validation | 2/2 | Complete    | 2026-03-07 |
| 14. Refactoring & Cleanup | 6/6 | Complete    | 2026-03-07 |

**Coverage:** 14/14 requirements mapped ✓

### v1.4.0 Developer Experience

| Phase | Plans Complete | Status | Target |
|-------|----------------|--------|--------|
| 15. API Ergonomics | 0/2 | Not Started | TBD |
| 16. Rendering Improvements | 0/2 | Not Started | TBD |
| 17. Performance Fixes | 0/1 | Not Started | TBD |
| 18. Code Organization | 0/2 | Not Started | TBD |
| 19. Documentation Fixes | 0/1 | Not Started | TBD |

## Phase Details

### Phase 11: Batch Projection

**Goal:** Library can batch-transform coordinate arrays efficiently and integrate batch projection into the rendering pipeline while preserving existing API contracts.

**Depends on:** Phase 10 (v1.2.0 completion)

**Requirements:** PERF-01, PERF-02, PERF-03

**Success Criteria** (what must be TRUE):
1. Coordinate arrays are transformed using batch operations instead of per-point projection
2. Rendering pipeline uses batch projection for all geometry types (Point, LineString, Polygon, Multi*)
3. All existing public API signatures remain unchanged (backward compatible)
4. Batch projection is transparent to existing code (no changes required to use)
5. Internal timing confirms measurable improvement over per-point projection

**Plans:** 2/2 plans complete

- [x] `11-01-PLAN.md` — Core batch infrastructure (CoordinateBatch, optimized geometries) — Completed 2026-03-05
- [x] `11-02-PLAN.md` — API integration & validation (opt-in parameter, warnings, benchmarks) — Completed 2026-03-05

---

### Phase 12: Viewport Caching

**Goal:** Library caches projected geometries for the current viewport state with simple clear-on-change semantics.

**Depends on:** Phase 11 (Batch Projection)

**Requirements:** PERF-04, PERF-05, PERF-06, PERF-07

**Success Criteria** (what must be TRUE):
1. Projected geometries are cached for current viewport state
2. Cache invalidates entirely when viewport changes (zoom, pan, viewport size)
3. Cache bounded by simple size limit (no LRU/LFU complexity)
4. Caching is transparent to existing code (no API changes required)
5. Simple Kotlin `MutableMap` implementation (no external caching libraries)

**Plans:** 3/3 plans complete

- [x] `12-01-PLAN.md` — Core cache infrastructure (ViewportState, CacheKey, ViewportCache) — Completed 2026-03-06
- [x] `12-02-PLAN.md` — Geometry dirty flag integration (reactive invalidation) — Completed 2026-03-06

---

### Phase 13: Integration & Validation

**Goal:** All optimizations are validated against v1.2.0 baseline and all 16 existing examples work unchanged.

**Depends on:** Phase 11 (Batch Projection), Phase 12 (Viewport Caching)

**Requirements:** PERF-08, PERF-09, PERF-10

**Success Criteria** (what must be TRUE):
1. ✓ Static camera scenarios show 10x+ improvement over v1.2.0 baseline — **ACHIEVED: 1533x average**
2. ✓ Performance validated with realistic datasets (100k+ features) — **ACHIEVED: Tested up to 250k**
3. All 16 v1.2.0 examples continue to work unchanged (regression test passed)
4. Memory usage remains bounded during extended creative sessions
5. ✓ Pan operations show measurable improvement from batch projection — **ACHIEVED: 343x average**

**Plans:** 2/2 plans complete

- [x] `13-01-PLAN.md` — Performance benchmarking infrastructure and tests (PERF-08, PERF-09) — Completed 2026-03-07
- [x] `13-02-PLAN.md` — Regression testing all 16 v1.2.0 examples (PERF-10) — Completed 2026-03-07

**Wave Structure:**
- Wave 1: Plan 13-01 (performance benchmarks - can run independently) ✓
- Wave 2: Plan 13-02 (regression tests - depends on benchmarks) ✓

---

## Dependency Graph

```
v1.3.0 Performance:
Phase 11: Batch Projection
    ↓
Phase 12: Viewport Caching
    ↓
Phase 13: Integration & Validation
    ↓
Phase 14: Refactoring & Cleanup

v1.4.0 Developer Experience:
                    ↓ (after v1.3.0 release)
        ┌───────────────────────────────┐
        │                               │
Phase 15: API Ergonomics      Phase 16: Rendering
        │                               │
Phase 17: Performance Fixes     Phase 20: Layer Features
        │
Phase 18: Code Organization
        │
Phase 19: Documentation
```

## Coverage Matrix

| Requirement | Phase | Category |
|-------------|-------|----------|
| PERF-01 | 11 | Batch Projection |
| PERF-02 | 11 | Batch Projection |
| PERF-03 | 11 | Batch Projection |
| PERF-04 | 12 | Viewport Caching |
| PERF-05 | 12 | Viewport Caching |
| PERF-06 | 12 | Viewport Caching |
| PERF-07 | 12 | Viewport Caching |
| PERF-08 | 13 | Measurement |
| PERF-09 | 13 | Measurement |
| PERF-10 | 13 | Validation |
| CLEANUP-01 | 14 | Entry Point Cleanup |
| CLEANUP-02 | 14 | Code Refactoring |
| CLEANUP-03 | 14 | API Improvements |
| CLEANUP-04 | 14 | Final Verification |
| API-01 | 15 | Import Structure |
| API-02 | 15 | Streamlined Rendering |
| API-03 | 15 | Conventions over Configuration |
| API-04 | 15 | Backward Compatibility |
| RENDER-01 | 16 | MultiPolygon Handling |
| RENDER-02 | 16 | Polygon Ring Handling |
| PERF-11 | 17 | OptimizedGeoSource Cache |
| ORG-01 | 18 | Example Cleanup |
| ORG-02 | 18 | Directory Structure |
| ORG-03 | 18 | File Organization |
| DOCS-01 | 19 | README Corrections |
| LAYER-01 | 20 | Graticule Improvements |

**Coverage:** 29/29 requirements mapped ✓

## Success Criteria Summary

| Phase | Criteria | Focus |
|-------|----------|-------|
| 11 | 5 | Batch transformation infrastructure |
| 12 | 5 | Simple viewport-based caching |
| 13 | 5 | Validation & regression testing |
| 14 | 8 | Code cleanup & TODO resolution |
| 15 | 6 | API ergonomics & boilerplate reduction |
| 16 | 4 | MultiPolygon & polygon ring fixes |
| 17 | 4 | OptimizedGeoSource performance |
| 18 | 5 | Code organization & structure |
| 19 | 3 | Documentation corrections |
| 20 | 4 | Graticule layer enhancements |

## Notes

**Phase Ordering Rationale:**
1. **Batch Projection (11)** must come first — prerequisite for caching
2. **Viewport Caching (12)** builds on batch projection — simple clear-on-change semantics
3. **Integration (13)** validates everything — ensures no regressions and meets performance targets

**Key Constraints:**
- NO Caffeine/Aedile dependencies — simple Kotlin `MutableMap` only
- NO LRU/LFU algorithms — web-style optimization not needed for creative coding
- Clear entire cache on viewport change (not per-entry eviction)
- All 16 v1.2.0 examples must work unchanged

**Simplification Rationale:**
Creative coding use case differs from web applications:
- User explores with pan/zoom, then observes static view
- When camera moves, **everything** becomes stale (not LRU-worthy)
- Simple clear-on-change is sufficient and more predictable
- Bounded by "one viewport worth of data" — no complex eviction needed

### Phase 14: Refactoring and Cleanup, Clearing TODOs

**Goal:** Address all accumulated TODOs, FIXMEs, and technical debt in the codebase following v1.3.0 performance work. Clean up code hygiene through systematic refactoring in a sensible order.

**Depends on:** Phase 13

**Requirements:** CLEANUP-01, CLEANUP-02, CLEANUP-03, CLEANUP-04

**Success Criteria** (what must be TRUE):
1. Zero TODOs remain in the entire Kotlin codebase
2. App.kt and TemplateProgram.kt redundancy resolved
3. GeoAnimator singleton design decision documented
4. GeoSource padding semantics clarified
5. drawDataQuadrant helper promoted to public API
6. render_BasicRendering.kt renamed to reflect actual content
7. Build passes with no errors
8. All tests pass

**Plans:** 6/6 plans complete

- [x] `14-01-PLAN.md` — Directory structure: Resolve App.kt/TemplateProgram.kt redundancy (Wave 1) — Completed 2026-03-07
- [x] `14-02-PLAN.md` — Method/code refactoring: GeoAnimator singleton & GeoSource padding (Wave 2) — Completed 2026-03-07
- [x] `14-03-PLAN.md` — Feature fixes: Promote helper to API, fix example naming (Wave 3) — Completed 2026-03-07
- [x] `14-04-PLAN.md` — AOB: Final TODO sweep, build/test verification (Wave 4) — Completed 2026-03-07
- [x] `14-05-PLAN.md` — **GAP CLOSURE**: Restore App.kt as canonical entry point (Wave 1) — Completed 2026-03-07 — App.kt created as clean 54-line entry point
- [x] `14-06-PLAN.md` — **GAP CLOSURE**: Move example to examples/render/ with numbered naming (Wave 1) — Completed 2026-03-07 — Example moved to examples/render/08-feature-iteration.kt

**Wave Structure:**
- Wave 1: Plan 14-01 (structural changes - directory/entry points) ✓
- Wave 2: Plan 14-02 (code refactoring - singleton & semantics) ✓
- Wave 3: Plan 14-03 (feature fixes - API promotion, renaming) ✓
- Wave 4: Plan 14-04 (verification - final sweep, tests) ✓
- **Gap Closure Wave**: Plans 14-05, 14-06 (UAT issue resolution) — Can run in parallel

---

### Phase 15: API Ergonomics — Reduce Boilerplate

**Goal:** Reduce API boilerplate for the most common creative coding workflow: load data → create projection → render. Make the library more expressive and reduce friction for new users.

**Depends on:** Phase 14 (v1.3.0 completion)

**Requirements:** API-01, API-02, API-03, API-04

**Success Criteria** (what must be TRUE):
1. Single-import API works (`import geo.*` gets commonly needed classes)
2. Streamlined rendering workflow with minimal setup code
3. Conventions over configuration for common use cases
4. Existing API remains backward compatible
5. Examples demonstrate both minimal and full-control approaches
6. Users can render data in 3 lines or less for simple cases

**Source:** Todo 2026-02-25-reduce-rendering-boilerplate.md

**Current vs Target API:**

```kotlin
// CURRENT (boilerplate-heavy):
val data = geoSource("file.geojson")
val projection = ProjectionFactory.fitBounds(data.totalBoundingBox(), width, height)
drawer.geo(data) { this.projection = projection }

// TARGET (minimal):
geoSource("file.geojson").renderTo(drawer) // auto-fits to viewport
```

**Ideas to Explore:**
- Wildcard export of commonly used classes
- `GeoSource.renderTo()` convenience method with auto-projection
- Simplified import structure (single data monolith)
- Convention-based defaults for projection, styling, etc.
- DSL improvements for common patterns

**Plans:** 0/2 plans (To be planned)

- [ ] `15-01-PLAN.md` — Import structure and wildcard exports
- [ ] `15-02-PLAN.md` — Streamlined rendering API and conventions

**Wave Structure:**
- Wave 1: Plan 15-01 (import structure - foundation)
- Wave 2: Plan 15-02 (rendering API - builds on imports)

---

### Phase 16: Rendering Improvements

**Goal:** Fix MultiPolygon rendering for ocean/whole-world data and improve polygon interior/exterior ring handling.

**Depends on:** Phase 14 (v1.3.0 completion)

**Requirements:** RENDER-01, RENDER-02

**Success Criteria** (what must be TRUE):
1. MultiPolygon features spanning projection limits render correctly
2. Polygon interior rings (holes) handled properly
3. Ocean data (oceans.geojson) renders without artifacts
4. Winding order and ring classification work correctly

**Source:** Todos 2026-02-25-fix-multipolygon-ocean-data.md, 2026-02-26-improve-polygon-interior-exterior-ring-handling.md

**Plans:** 0/2 plans (To be planned)

- [ ] `16-01-PLAN.md` — MultiPolygon projection limit handling
- [ ] `16-02-PLAN.md` — Polygon ring handling with OpenRNDR shapes

---

### Phase 17: Performance Fixes

**Goal:** Extend ViewportCache to OptimizedGeoSource rendering path.

**Depends on:** Phase 12 (Viewport Caching), Phase 14 (v1.3.0 completion)

**Requirements:** PERF-11

**Success Criteria** (what must be TRUE):
1. OptimizedGeoSource uses ViewportCache for screen coordinates
2. Examples with `optimize=true` achieve 60+ FPS
3. Batch projection benefits available for optimized geometries
4. No performance regression for standard rendering path

**Source:** Todo 2026-03-07-extend-viewportcache-to-optimizedgeosource-rendering.md

**Critical:** This fixes a gap where optimized rendering bypasses all Phase 11-12 performance work.

**Plans:** 0/1 plans (To be planned)

- [ ] `17-01-PLAN.md` — Integrate ViewportCache with OptimizedGeoSource

---

### Phase 18: Code Organization

**Goal:** Improve project structure through file organization and cleanup.

**Depends on:** Phase 14 (v1.3.0 completion)

**Requirements:** ORG-01, ORG-02, ORG-03

**Success Criteria** (what must be TRUE):
1. Necro example files cleaned up (src/main/kotlin/geo/examples/)
2. Core domain files organized in geo/core/ subdirectory
3. File contents follow consistent organization patterns
4. No functionality changes (pure refactoring)

**Source:** Todos 2026-03-07-clean-up-necro-examples.md, 2026-03-07-move-geo-root-files.md, 2026-03-07-organize-file-contents.md

**Plans:** 0/2 plans (To be planned)

- [ ] `18-01-PLAN.md` — Clean up necro examples and directory structure
- [ ] `18-02-PLAN.md` — Reorganize file contents for navigation

---

### Phase 19: Documentation Fixes

**Goal:** Fix README run commands and data paths.

**Depends on:** Phase 14 (v1.3.0 completion)

**Requirements:** DOCS-01

**Success Criteria** (what must be TRUE):
1. Run commands in README files use correct class names (no "Kt" suffix)
2. Data paths reference correct locations (examples/data/geo/)
3. Users can copy-paste commands without modification

**Source:** Todo 2026-02-27-fix-readme-run-commands-and-data-paths.md

**Quick Fix:** No research needed, can be quick task or short phase.

**Plans:** 0/1 plans (To be planned)

- [ ] `19-01-PLAN.md` — README corrections

---

### Phase 20: Layer Features

**Goal:** Fix and enhance graticule layer for zoomed-in maps.

**Depends on:** Phase 14 (v1.3.0 completion)

**Requirements:** LAYER-01

**Success Criteria** (what must be TRUE):
1. Graticule layer works for local/regional (non-global) views
2. Variable graticule density based on zoom level
3. Viewport-based graticule generation (only visible area)
4. Configurable interval parameters

**Source:** Todo 2026-02-27-graticule-layer-for-zoomed-in-maps.md

**Note:** Requires research on how other libraries handle graticules for zoomed-in views.

**Plans:** 0/1 plans (To be planned)

- [ ] `20-01-PLAN.md` — Graticule layer improvements

---

---
*Created: 2026-03-05 for v1.3.0 Performance milestone*
