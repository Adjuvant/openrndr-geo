# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.
**Current focus:** Phase 1 - Data Layer

## Current Position

Phase: 2 of 5 (Coordinate Systems)
Plan: 2 of 3 in current phase
Status: In progress - Public projections complete
Last activity: 2026-02-21 — Completed 02-02-PLAN.md (Public Projection Implementations)

Progress: [████░░░░░░] 28%

## Performance Metrics

**Velocity:**
- Total plans completed: 6
- Average duration: 10.2 min
- Total execution time: 1.02 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1. Data Layer | 4/4 ✓ | 46m | 11.5m |
| 2. Coordinate Systems | 2/3 | 14m | 7m |
| 3. Core Rendering | 0/3 | - | - |
| 4. Layer System | 0/3 | - | - |
| 5. Animation | 0/3 | - | - |

**Recent Trend:**
- 02-02 completed in 8 minutes (public projections)
- 4 tasks committed atomically
- DSL builder pattern established with invoke() operator
- BNG projection with CRS transformation using proj4j

## Accumulated Context

### Decisions

| Plan | Decision | Rationale |
|------|----------|-----------|
| 01-01 | Use OpenRNDR Vector2 for points | Integrates with drawing operations |
| 01-01 | Sealed class for Geometry | Enables exhaustive when expressions for renderers |
| 01-01 | Lazy bounding boxes | Expensive calculation, computed once and cached |
| 01-01 | NaN for empty Bounds | Type-safe empty state handling |
| 01-01 | Sequence<Feature> for GeoSource | Memory-efficient lazy iteration |
| 01-01 | Reified generics for propertyAs<T>() | Type-safe property access without Class<T> |
| 01-02 | Use kotlinx.serialization | Type-safe JSON parsing, Kotlin-native |
| 01-02 | Permissive parsing (skip malformed) | Better UX for real-world data |
| 01-02 | Support single Feature input | GeoJSON spec allows Feature, not just FeatureCollection |
| 01-02 | String-to-Double coordinate parsing | Better compatibility with kotlinx.serialization version |
| 01-03 | Quadtree MAX_CAPACITY=16 | Balances memory and query performance |
| 01-03 | Cursor-style iteration for GeoPackage | ResultSet API uses moveToNext/getRow pattern |
| 01-03 | CRS from FeatureDao.getSrs() | More reliable than SpatialReferenceSystemDao |
| 01-04 | Convenience-first API design | Make common case easy (direct features), keep advanced case possible (Source objects) |
| 01-04 | Thin wrapper pattern for convenience functions | Delegate to load().features - no code duplication |
| 01-04 | Document tradeoffs in KDoc | Help users choose right API for their use case |
| 02-01 | Use proj4j for CRS transformations | Proven Java library with EPSG code support (27700 for BNG, 4326 for WGS84) |
| 02-01 | Interface-based projection abstraction | Allows mixing coordinate systems (lat/lng + BNG) in single visualization |
| 02-01 | Internal package for complex math | Separates complexity from public API, follows isolation pattern |
| 02-01 | Throw ProjectionOverflowException with clamp recommendation | CONTEXT.md decision: explicit error handling for Mercator poles |
| 02-01 | Normalize longitudes automatically | CONTEXT.md decision: prevent coordinate wrapping issues |
| 02-02 | DSL syntax with invoke() operator | Enables clean configuration: ProjectionMercator { width = 800 } |
| 02-02 | ProjectionBNG uses Helmert transformation (~3-5m) | Simpler than OSTN15 (~1cm) which requires grid interpolation |
| 02-02 | Companion object static utilities for BNG | latLngToBNG and bngToLatLng available without instance |
| 02-02 | Factory object with default parameters | Convenient presets: ProjectionFactory.mercator(width, height) |

### Pending Todos

None - all Phase 1 items complete.

### Blockers/Concerns

None yet.

## Session Continuity

Last session: 2026-02-21 16:18 UTC
Stopped at: Completed 02-02-SUMMARY.md, ready for 02-03
Resume file: None
