# Requirements: openrndr-geo v1.2.0

**Defined:** 2026-02-26
**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## v1 Requirements

### Data Inspection

- [ ] **INSP-01**: User can call `GeoSource.summary()` to get feature count, bounds, CRS, geometry type distribution
- [ ] **INSP-02**: User can see memory footprint estimate in summary output
- [ ] **INSP-03**: User can inspect property keys and sample values from features

### Rendering

- [ ] **REND-07**: User can render Polygon features with interior rings (holes) correctly
- [ ] **REND-08**: User can render MultiPolygon features with interior rings clamped to projection bounds
- [ ] **REND-09**: User sees correct rendering of ocean/whole-world MultiPolygon data

### API Design

- [ ] **API-01**: User can access feature-level iteration with projected coordinates internalized
- [ ] **API-02**: User can choose between simple workflow (beginner) and detailed control (professional)
- [ ] **API-03**: User has escape hatches for advanced/custom rendering patterns
- [ ] **API-04**: API style matches OpenRNDR DSL conventions (not one-line magic)

### Documentation

- [ ] **DOC-01**: User can browse examples organized by category (core_, render_, proj_, anim_, layer_)
- [ ] **DOC-02**: User can run examples with included data files (small GeoJSON/GeoPackage)
- [ ] **DOC-03**: Each example demonstrates ONE feature/concept
- [ ] **DOC-04**: Examples serve as UAT validation for framework features

## v2 Requirements

Deferred to future release.

### Performance

- **PERF-01**: User can batch-project coordinates for animation performance
- **PERF-02**: User can cache projected geometries across frames

### Advanced Features

- **ADV-01**: User can clip geometries at projection bounds (vs current clamp)
- **ADV-02**: User can configure bounds handling strategy (CLAMP, CLIP, SKIP)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Batch projection | Defer to v1.3.0 pending performance benchmarking |
| One-line render APIs | Doesn't match OpenRNDR DSL style; removes control |
| Property type inference | Complexity not justified for creative coding use case |
| Real-time pan/zoom | Deferred to v2 |
| Custom aesthetic primitives | Deferred to v2 |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| INSP-01 | Phase 7 | Pending |
| INSP-02 | Phase 7 | Pending |
| INSP-03 | Phase 7 | Pending |
| REND-07 | Phase 8 | Pending |
| REND-08 | Phase 8 | Pending |
| REND-09 | Phase 8 | Pending |
| API-01 | Phase 9 | Pending |
| API-02 | Phase 9 | Pending |
| API-03 | Phase 9 | Pending |
| API-04 | Phase 9 | Pending |
| DOC-01 | Phase 10 | Pending |
| DOC-02 | Phase 10 | Pending |
| DOC-03 | Phase 10 | Pending |
| DOC-04 | Phase 10 | Pending |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0 ✓

---
*Requirements defined: 2026-02-26*
*Last updated: 2026-02-26 after v1.2.0 planning session*
