# Requirements: openrndr-geo v1.3.0 Performance

**Defined:** 2026-03-05
**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## v1.3.0 Requirements

### Performance — Batch Projection

- [x] **PERF-01**: Library can batch-transform coordinate arrays efficiently instead of per-point projection
- [x] **PERF-02**: Rendering pipeline uses batch projection for all geometry types (Point, LineString, Polygon, Multi*)
- [x] **PERF-03**: Batch projection preserves existing API contracts (no signature changes to public methods)

### Performance — Viewport Caching

- [x] **PERF-04**: Library caches projected geometries for current viewport state
- [x] **PERF-05**: Cache invalidates when viewport changes (zoom, pan, viewport size)
- [x] **PERF-06**: Cache bounded by simple size limit (clear-on-change, not LRU/LFU)
- [x] **PERF-07**: Caching is transparent to existing code (no API changes required)

### Performance — Measurement

- [ ] **PERF-08**: Static camera scenarios show 10x+ improvement over v1.2.0 baseline
- [ ] **PERF-09**: Performance validated with realistic datasets
- [ ] **PERF-10**: All 16 v1.2.0 examples continue to work unchanged (regression test)

## v2+ Requirements (Deferred)

### Performance — Advanced

- **PERF-11**: Lazy vs eager projection strategy (user-controlled memory vs compute tradeoff)
- **PERF-12**: Spatial culling — skip projection for off-screen geometries
- **PERF-13**: Multi-threaded batch projection leveraging all CPU cores
- **PERF-14**: Cache hit/miss metrics for debugging

### Advanced Features (Deferred from v1.3.0)

- **ADV-01**: Clip geometries at projection bounds (vs current clamp)
- **ADV-02**: Configurable bounds handling strategy
- **ADV-03**: Graticule layer improvements for zoomed-in maps

## Out of Scope

| Feature | Reason |
|---------|--------|
| Caffeine/Aedile caching libraries | Overkill for creative coding; simple Map sufficient |
| LRU/LFU eviction algorithms | Web-style optimization not needed for viewport-based geometry |
| Full JMH benchmarking suite | Internal metrics sufficient; no framework weight |
| GPU-based projection compute shaders | Massive complexity, overkill for prototyping |
| Persistent/distributed caching | In-memory only sufficient for creative sessions |
| Real-time adaptive LOD | HIGH complexity, deferred to v2+ |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PERF-01 | Phase 11 | Complete |
| PERF-02 | Phase 11 | Complete |
| PERF-03 | Phase 11 | Complete |
| PERF-04 | Phase 12 | Complete |
| PERF-05 | Phase 12 | Complete |
| PERF-06 | Phase 12 | Complete |
| PERF-07 | Phase 12 | Complete |
| PERF-08 | Phase 13 | Pending |
| PERF-09 | Phase 13 | Pending |
| PERF-10 | Phase 13 | Pending |

**Coverage:**
- v1.3.0 requirements: 10 total
- Mapped to phases: 10/10 ✓
- Unmapped: 0

### Coverage by Phase

| Phase | Requirements | Count |
|-------|--------------|-------|
| Phase 11: Batch Projection | PERF-01, PERF-02, PERF-03 | 3 |
| Phase 12: Viewport Caching | PERF-04, PERF-05, PERF-06, PERF-07 | 4 |
| Phase 13: Integration & Validation | PERF-08, PERF-09, PERF-10 | 3 |

---
*Requirements defined: 2026-03-05*
*Last updated: 2026-03-06 after Phase 12 Plan 01 completion*
