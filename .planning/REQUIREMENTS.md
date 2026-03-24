# Requirements: openrndr-geo

**Defined:** 2026-03-07
**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## v1.4.0 Requirements

Requirements for the Developer Experience milestone. Each requirement maps to a roadmap phase.

### API Ergonomics (Phase 15)

- [x] **API-01**: Single-import API — `import geo.*` gets everything needed for basic workflows
- [x] **API-02**: Reduced boilerplate — 3-line rendering workflow (load → project → render)
- [x] **API-03**: RawProjection UX improvements — better samples and documentation for advanced workflows

### Rendering Improvements (Phase 16)

- [x] **RENDER-01**: MultiPolygon rendering for ocean/whole-world data — fix winding order and coordinate handling
- [x] **RENDER-02**: Polygon interior/exterior ring handling — proper hole support and ring classification

### Performance Fixes (Phase 17)

- [x] **PERF-11**: ViewportCache integration for OptimizedGeoSource — extend caching to optimized rendering path

### Code Organization (Phase 18)

- [x] **ORG-01**: Clean up necro examples — remove dead example code from src/main/kotlin/geo/examples
- [x] **ORG-02**: Move geo root files to core/ subdirectory — better package structure
- [ ] **ORG-03**: Organize file contents — logical grouping for better code navigation

### Documentation Fixes (Phase 19)

- [x] **DOCS-01**: Fix README run commands and data paths — ensure all examples run correctly

### Layer Features (Phase 20)

- [ ] **LAYER-01**: Graticule layer for zoomed-in maps — adaptive grid spacing and proper labeling

## v2 Requirements (Deferred)

Features acknowledged but not in current milestone scope.

### Advanced Features

- **ADV-01**: Clip geometries at projection bounds (vs current clamp)
- **ADV-02**: Configurable bounds handling strategy
- **ADV-03**: Real-time pan/zoom interaction

### Aesthetic Features

- **AEST-01**: Custom aesthetic primitives (hatching, stippling)
- **AEST-02**: Temporal animation (time-series playback)

### Documentation

- **DOC-GUIDE**: Comprehensive function exploration guide organized by visual intent

## Out of Scope

| Feature | Reason |
|---------|--------|
| 3D rendering | Architecture should not preclude, but deferred to v2 |
| Interactive pan/zoom | Batch-style rendering sufficient for v1 creative coding |
| Production deployment tooling | Focus is prototyping/exploration, not end-user apps |
| Mobile/web targets | Kotlin/JVM desktop only for v1 |
| GIS-style analysis | Visualization library, not GIS tool |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| API-01 | Phase 15 | Complete |
| API-02 | Phase 15 | Complete |
| API-03 | Phase 15 | Complete |
| RENDER-01 | Phase 16 | Complete |
| RENDER-02 | Phase 16 | Complete |
| PERF-11 | Phase 17 | Complete |
| ORG-01 | Phase 18 | Complete |
| ORG-02 | Phase 18 | Complete |
| ORG-03 | Phase 18 | Pending |
| DOCS-01 | Phase 19 | Complete |
| LAYER-01 | Phase 20 | Pending |

**Coverage:**
- v1.4.0 requirements: 11 total
- Mapped to phases: 11
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-07*
*Last updated: 2026-03-07 after milestone v1.4.0 start*
