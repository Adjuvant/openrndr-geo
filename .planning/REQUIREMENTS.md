# Requirements: openrndr-geo v1.3.0 Performance

**Defined:** 2026-03-05
**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## v1.3.0 Requirements

### Performance — Batch Projection

- [ ] **PERF-01**: Library can batch-transform coordinate arrays efficiently instead of per-point projection
- [ ] **PERF-02**: Rendering pipeline uses batch projection for all geometry types (Point, LineString, Polygon, Multi*)
- [ ] **PERF-03**: Batch projection preserves existing API contracts (no signature changes to public methods)

### Performance — Geometry Caching

- [ ] **PERF-04**: Library caches projected geometries keyed by projection parameters
- [ ] **PERF-05**: Cache automatically invalidates when projection state changes (zoom, pan, viewport size)
- [ ] **PERF-06**: Cache uses bounded LRU eviction to prevent unbounded memory growth
- [ ] **PERF-07**: Caching is transparent to existing code (no API changes required)
- [ ] **PERF-08**: Cache hit/miss metrics available for debugging (internal, not public API)

### Performance — State Detection

- [ ] **PERF-09**: Library detects when projection parameters change
- [ ] **PERF-10**: Projection state can be hashed for efficient cache key comparison
- [ ] **PERF-11**: State detection handles animation/tweening without excessive invalidation

### Performance — Measurement

- [ ] **PERF-12**: Internal frame time tracking confirms 10x+ improvement for static camera scenarios
- [ ] **PERF-13**: Performance tests validate caching effectiveness with realistic datasets
- [ ] **PERF-14**: All 16 v1.2.0 examples continue to work unchanged (regression test)

## v2+ Requirements (Deferred)

### Performance — Advanced

- **PERF-15**: Lazy vs eager projection strategy (user-controlled memory vs compute tradeoff)
- **PERF-16**: Spatial culling — skip projection for off-screen geometries
- **PERF-17**: Multi-threaded batch projection leveraging all CPU cores

### Advanced Features (Deferred from v1.3.0)

- **ADV-01**: Clip geometries at projection bounds (vs current clamp)
- **ADV-02**: Configurable bounds handling strategy
- **ADV-03**: Graticule layer improvements for zoomed-in maps

## Out of Scope

| Feature | Reason |
|---------|--------|
| Full JMH benchmarking suite | Creative coding library doesn't need enterprise-grade benchmarking; internal metrics sufficient |
| GPU-based projection compute shaders | Massive complexity, overkill for prototyping use case |
| Persistent/distributed caching | In-memory only sufficient for creative sessions |
| Real-time adaptive LOD | HIGH complexity, deferred to v2+ |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PERF-01 | Phase 11 | Pending |
| PERF-02 | Phase 11 | Pending |
| PERF-03 | Phase 11 | Pending |
| PERF-04 | Phase 11 | Pending |
| PERF-05 | Phase 11 | Pending |
| PERF-06 | Phase 11 | Pending |
| PERF-07 | Phase 11 | Pending |
| PERF-08 | Phase 11 | Pending |
| PERF-09 | Phase 11 | Pending |
| PERF-10 | Phase 11 | Pending |
| PERF-11 | Phase 11 | Pending |
| PERF-12 | Phase 11 | Pending |
| PERF-13 | Phase 11 | Pending |
| PERF-14 | Phase 11 | Pending |

**Coverage:**
- v1.3.0 requirements: 14 total
- Mapped to phases: 0 (pending roadmap creation)
- Unmapped: 14 (will be mapped during roadmap creation)

---
*Requirements defined: 2026-03-05*
*Last updated: 2026-03-05 after research synthesis*
