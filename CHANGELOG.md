# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [1.3.0] - 2026-03-07

### Performance

This release delivers transformational performance improvements through batch coordinate projection and viewport caching.

- **Static camera:** 1533x average speedup (range: 30.96x - 4870.36x) — target was 10x
- **Pan operations:** 343x average speedup (range: 39.52x - 827.29x)
- **Validated with:** Up to 250,000 features (10k/50k/100k/250k test datasets)
- **All 278 tests passing**

### Phase 11: Batch Projection

Implemented DoubleArray-based coordinate batch transformation for efficient rendering.

- `CoordinateBatch` — transforms coordinate arrays instead of per-point projection
- `OptimizedPoint`, `OptimizedLineString`, `OptimizedPolygon` — specialized geometry subclasses
- Batch transformation utilities with inline functions to eliminate lambda allocation overhead
- Console warnings for unoptimized large datasets (>1000 features)
- Zero API changes — optimization is transparent to existing code

### Phase 12: Viewport Caching

Added simple clear-on-change viewport caching for projected geometries.

- `ViewportState` — immutable cache keys capturing viewport configuration
- `ViewportCache` — `MutableMap`-based cache with clear-on-change semantics
- Geometry dirty flag integration for reactive cache invalidation
- `MAX_CACHE_ENTRIES = 1000` upper bound (no LRU/LFU complexity)
- Transparent caching — no code changes required to benefit

### Phase 13: Integration & Validation

Validated all optimizations against v1.2.0 baseline.

- Performance benchmarks for static camera and pan operations
- Synthetic dataset testing (10,000 to 250,000 features)
- Regression test suite validating all 26 examples from v1.2.0
- Memory usage bounds testing confirms bounded growth
- All v1.2.0 examples work unchanged

### Phase 14: Refactoring & Cleanup

Systematic code cleanup achieving zero technical debt markers.

- **Zero TODOs** — comprehensive sweep removed all TODOs, FIXMEs, XXXs, and HACKs
- **Entry point** — `App.kt` restored as canonical 54-line entry point
- **Template** — `TemplateProgram.kt` as comprehensive usage example
- **API promotion** — `drawDataQuadrant()` helper promoted to `GeoSource.renderQuadrant()` public API
- **Example reorganization** — `08-feature-iteration.kt` moved to `examples/render/`
- **Documentation** — KDoc added clarifying `GeoAnimator` singleton design and `GeoSource` padding semantics

[1.3.0]: https://github.com/thomasrussellmurphy/openrndr-geo/releases/tag/v1.3.0
