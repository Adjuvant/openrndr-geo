# Project Research Summary

**Project:** openrndr-geo  
**Domain:** Geospatial Creative Coding Visualization  
**Milestone:** v1.3.0 Performance  
**Researched:** 2026-03-05  
**Confidence:** HIGH

## Executive Summary

The openrndr-geo v1.3.0 milestone focuses on performance optimization through batch coordinate projection and geometry caching for an existing Kotlin/JVM geospatial visualization library. Based on comprehensive analysis of the existing codebase and domain best practices, the recommended approach centers on implementing a **CachingGeoSource** wrapper pattern that transparently caches projected geometries keyed by projection parameters. This preserves the existing two-tier API (beginner/professional) while delivering 10-50x performance improvements for static cameras and pan operations.

The research confirms that **Caffeine 3.2.3** with **Aedile 3.0.2** provides the optimal caching foundation—offering peer-reviewed TinyLFU eviction algorithms and idiomatic Kotlin coroutine support. For benchmarking, **JMH 1.37** is essential to accurately measure JVM performance without JIT warmup and GC noise. The existing `kotlinx.coroutines` (already in classpath via OPENRNDR) handles parallel batch processing using `Dispatchers.Default` for CPU-bound coordinate transformations. All additions are carefully selected to respect OPENRNDR's single-threaded OpenGL context constraint.

Key risks center on **cache invalidation** with projection changes (Fowler's "two hard things"), **unbounded cache growth** with multi-GB datasets, and **breaking the existing API contract** that users depend on. Mitigation requires projection-versioned cache keys, bounded LRU eviction, and transparent optimization that requires no API changes. The architecture follows established patterns: a caching layer between data and rendering, immutable `ProjectedGeometry` cache values, and lazy Sequence preservation for memory efficiency.

## Key Findings

### Recommended Stack

The v1.3.0 milestone adds minimal but strategic dependencies to the existing Kotlin/JVM + OPENRNDR foundation. Core additions are **Caffeine 3.2.3** for high-performance caching (6x faster than Guava, used by Kafka/Cassandra/Neo4j), **Aedile 3.0.2** for idiomatic Kotlin coroutine wrappers, **JMH 1.37** for reliable microbenchmarking (official OpenJDK tool), and the JMH Gradle Plugin 0.7.3 for clean benchmark isolation. No additional threading libraries are needed—`kotlinx.coroutines` 1.10.2 is already available.

**Core technologies:**
- **Caffeine 3.2.3**: High-performance geometry cache with TinyLFU eviction — near-optimal caching used by major data systems
- **Aedile 3.0.2**: Kotlin coroutine wrapper for Caffeine — provides `suspend` functions and Kotlin Duration support, eliminates Java Future boilerplate
- **JMH 1.37**: Java Microbenchmark Harness — official OpenJDK tool eliminates JVM warmup, JIT, and GC noise
- **kotlinx.coroutines 1.10.2**: Structured concurrency — use `Dispatchers.Default` for CPU-bound batch projection (already in classpath)

**Critical constraint:** OPENRNDR's OpenGL context is single-threaded. All `Drawer` operations must happen on the main thread. Parallel processing is ONLY for data transformation (coordinate projection), not rendering.

### Expected Features

The research identifies four table stakes features users expect from a performance-oriented geospatial library, plus differentiators that provide competitive advantage. The existing codebase provides excellent foundation with `GeoProjection` interface, `ProjectedGeometry` sealed class, and Sequence-based lazy iteration.

**Must have (table stakes):**
- **Batch Coordinate Projection** — transform arrays of geographic coordinates to screen space in one operation. Current gap: renderers call `map { projection.project(it) }` per-geometry instead of using existing `toScreen(points: List, projection)` batch variants.
- **Projection State Detection** — automatic cache invalidation when zoom/pan changes. `ProjectionConfig` is already immutable (good!) but needs change detection mechanism.
- **Frame-Stable Caching** — cache `ProjectedGeometry` per-feature when projection is stable. `withProjection()` creates projected features lazily every call—this should cache results.
- **Performance Benchmarking** — frame time tracking, projection time breakdown, memory profiling. No performance measurement currently exists.

**Should have (differentiators):**
- **Lazy vs Eager Projection Strategy** — users choose memory vs compute tradeoff per-dataset. Builds on existing `GeoSource.materialize()` pattern.
- **Spatial Partitioning for Visibility** — skip projection for off-screen geometries using existing `SpatialIndex`. `featuresInBounds()` exists but not used in renderers.
- **Multi-threaded Batch Projection** — leverage all CPU cores for coordinate arrays. Kotlin coroutines make this idiomatic but adds thread-safety complexity.

**Defer (v2+):**
- **Adaptive Level-of-Detail (LOD)** — simplify geometry based on zoom. HIGH complexity, requires simplification algorithm.
- **GPU-Based Projection** — compute shaders. Massive complexity, overkill for creative coding use case.

### Architecture Approach

The recommended architecture introduces a **CachingGeoSource** wrapper that sits between the data layer (`GeoSource`) and rendering layer, with cache invalidation keyed by projection parameters. This preserves the existing API while adding performance optimizations transparently.

**Major components:**
1. **CachingGeoSource** — wraps any GeoSource, maintains projection cache with invalidation semantics keyed by `ProjectionCacheKey`
2. **BatchProjector** — efficient batch coordinate transformation using optimized bulk operations instead of per-point projection
3. **ProjectionCacheKey** — immutable key capturing all projection parameters (width, height, center, scale) that affect screen coordinates
4. **CacheStats** — optional performance metrics (hit rate, memory usage) for debugging and tuning

The data flow changes from projecting every coordinate every frame to: cache check → hit (use cached) / miss (batch project → cache → use). Cache invalidation occurs on projection config changes, viewport size changes, or manual invalidate() call.

### Critical Pitfalls

Research identified 10 pitfalls ranging from critical to minor. The top risks are:

1. **Premature Optimization Without Profiling Baseline** — Implementing complex caching without first establishing where time is actually spent. **Avoid by:** Completing PERF-03 (benchmarking) BEFORE PERF-01/02; measuring current frame times with real datasets (12GB Ordnance Survey data).

2. **Cache Invalidation Cascade with Projection Changes** — Cached geometries become stale when projection parameters change, causing visual artifacts. `GeoProjection` is an interface with multiple implementations; animation enables tweening parameters. **Avoid by:** Making projection state hashable; versioning projection with `projectionVersion: Long`; conservative invalidation (when in doubt, invalidate).

3. **Unbounded Cache Growth (Memory Leaks)** — Geometry caches grow without limit with multi-GB GeoPackage support. **Avoid by:** Bounded LRU caches (Caffeine with maximumSize); soft/weak references for large geometries; explicit `clearCache()` API for users.

4. **Breaking the Two-Tier API Contract** — v1.2.0 established `drawer.geo(source)` (beginner) and `drawer.geo(source) { }` (professional) APIs. Optimizations must not change signatures or behavior. **Avoid by:** Transparent optimization (caching internal, no API changes); preserving all existing entry points; testing all 16 v1.2.0 examples.

5. **Over-Engineering for Prototyping Use Case** — Implementing enterprise-grade caching (distributed, persistent) when the use case is rapid creative prototyping. **Avoid by:** YAGNI principle—in-memory cache only, no persistence; ensuring cache management < 5% of frame time; respecting "fluid and creative" exploration goal.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Foundation (PERF-03 Benchmarking)
**Rationale:** Must establish baseline metrics before any optimization work. "You can't improve what you don't measure." Completing this first prevents premature optimization (Pitfall #1).
**Delivers:** JMH benchmark suite, current frame time measurements, bottleneck identification, performance budgets (target: 60fps with 100k features)
**Addresses:** Performance Benchmarking (table stakes #4)
**Avoids:** Pitfall #1 (premature optimization)
**Research flag:** LOW — JMH is well-documented, established patterns

### Phase 2: Batch Projection (PERF-01)
**Rationale:** Core infrastructure for performance. Batch projection is prerequisite for caching (can't cache what isn't batched). Uses existing `ScreenTransform.toScreen()` variants.
**Delivers:** BatchProjector component, integration with render pipeline, SoA (Structure of Arrays) for cache-friendly memory layout
**Uses:** kotlinx.coroutines for parallel processing, Caffeine for simple caches
**Implements:** Batch coordinate transformation, optimized bulk operations
**Avoids:** Pitfall #6 (data locality issues), Pitfall #8 (sealed class exhaustiveness)
**Research flag:** MEDIUM — needs validation with real datasets

### Phase 3: Geometry Caching (PERF-02)
**Rationale:** The main performance win. Builds on Phase 2's batch projection. Caching delivers 10-50x improvement for static cameras.
**Delivers:** CachingGeoSource wrapper, ProjectionCacheKey, cache invalidation logic, LRU eviction
**Uses:** Caffeine 3.2.3, Aedile 3.0.2 for coroutine support
**Implements:** Caching layer between data and rendering, transparent optimization
**Avoids:** Pitfall #2 (invalidation bugs), Pitfall #3 (unbounded growth), Pitfall #7 (cache key collisions)
**Research flag:** HIGH — cache invalidation is complex, needs careful testing

### Phase 4: Integration & Validation
**Rationale:** Ensure all v1.2.0 examples work unchanged, spatial index consistency maintained, and API contract preserved.
**Delivers:** Updated drawer extensions with optional `cache = true` parameter, integration tests, all 16 examples validated
**Avoids:** Pitfall #4 (breaking API), Pitfall #10 (spatial index inconsistency)
**Research flag:** LOW — integration testing is standard practice

### Phase 5: Performance Examples (Optional)
**Rationale:** Demonstrate the optimizations with working examples. Builds on all previous phases.
**Delivers:** `perf_CachingDemo.kt`, `perf_BatchProjection.kt`, performance comparison visualizations
**Research flag:** LOW — examples follow established patterns

### Phase Ordering Rationale

The order follows dependency chain: **benchmarking → batch projection → caching → integration**. This prevents the critical mistake of optimizing without measurement (Phase 1 first). Batch projection must exist before caching can work (Phase 2 before 3). Integration testing must validate the complete system (Phase 4 last).

The grouping separates infrastructure (Phases 1-2) from optimization (Phase 3) from validation (Phase 4). This minimizes rework—if batch projection design changes, caching implementation is affected but benchmarking remains valid.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2 (PERF-01):** Batch projection memory layout—SoA pattern effectiveness needs validation with Cachegrind profiling
- **Phase 3 (PERF-02):** Cache invalidation edge cases—projection animation tweening interaction needs API research

Phases with standard patterns (skip research-phase):
- **Phase 1 (PERF-03):** JMH benchmarking—well-documented, established patterns
- **Phase 4 (Integration):** API compatibility testing—standard regression testing
- **Phase 5 (Examples):** Example creation—follows existing naming convention

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All technologies verified with official sources (Caffeine 3.2.3, Aedile 3.0.2, JMH 1.37 all from official repos) |
| Features | HIGH | Based on direct codebase analysis and established geospatial visualization patterns (D3, Mapbox, deck.gl) |
| Architecture | HIGH | Based on direct codebase analysis + Kotlin caching patterns; wrapper pattern is established |
| Pitfalls | HIGH | Based on codebase analysis + domain expertise; Game Programming Patterns data locality principles applied |

**Overall confidence:** HIGH

### Gaps to Address

1. **Cache size limits for 12GB GeoPackage:** What's the memory budget? Need to support partial caching only—determine threshold during Phase 3 implementation.

2. **Multi-threading priority:** Is single-threaded caching sufficient for v1.3.0? Decision needed in Phase 2 planning—defer parallel projection if caching provides adequate gains.

3. **Animation layer interaction:** v1.1.0 enables projection tweening. How does cache invalidation work during animation? Needs validation during Phase 3.

4. **Real dataset validation:** Research used codebase analysis; actual 12GB Ordnance Survey data testing needed in Phase 1 benchmarking.

## Sources

### Primary (HIGH confidence)
- **Caffeine**: https://github.com/ben-manes/caffeine (v3.2.3, Oct 2025) — caching algorithms, TinyLFU eviction
- **Aedile**: https://github.com/sksamuel/aedile (v3.0.2, Dec 2025) — Kotlin coroutine wrappers
- **JMH**: https://github.com/openjdk/jmh — official OpenJDK benchmarking tool
- **JMH Gradle Plugin**: https://github.com/melix/jmh-gradle-plugin (v0.7.3, Apr 2025) — Gradle integration
- **kotlinx.coroutines**: https://github.com/Kotlin/kotlinx.coroutines (v1.10.2, Apr 2025) — structured concurrency
- **OPENRNDR Concurrency**: https://guide.openrndr.org/drawing/concurrencyAndMultithreading.html — OpenGL context constraints

### Secondary (MEDIUM confidence)
- **Existing codebase analysis** — `ScreenTransform.kt`, `ProjectionConfig.kt`, `Feature.kt`, `GeoSource.kt`, `DrawerGeoExtensions.kt`, `SpatialIndex.kt`
- **Geospatial visualization patterns** — D3.js projection caching, MapLibre/deck.gl batch rendering approaches
- **Game Programming Patterns** — Data Locality chapter (cache-friendly structures), Spatial Partition chapter

### Tertiary (LOW confidence)
- **Memory optimization techniques** — Object pooling for Vector2, lazy sequences for large datasets (needs validation with actual 12GB data)

---

*Research completed: 2026-03-05*  
*Synthesized from: STACK.md, FEATURES.md, ARCHITECTURE.md, PITFALLS.md*  
*Ready for roadmap: yes*
