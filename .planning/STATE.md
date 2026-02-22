# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-21)

**Core value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.
**Current focus:** Phase 3 - Core Rendering (Coordinate Systems complete)

## Current Position

Phase: 3 of 5 (Core Rendering)
Plan: 5 of 5 in current phase (Gap Closure)
Status: Phase complete - Core Rendering finished, UAT gap closure complete
Last activity: 2026-02-22 — Completed 03-06-PLAN.md (Live Rendering Example - gap closure)

Progress: [██████████] 65%

## Performance Metrics

**Velocity:**
- Total plans completed: 10
- Average duration: 7.5 min
- Total execution time: 1.25 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1. Data Layer | 4/4 ✓ | 46m | 11.5m |
| 2. Coordinate Systems | 3/3 ✓ | 16m | 5.3m |
| 3. Core Rendering | 4/4 ✓ | 24m | 6.0m |
| 4. Layer System | 0/3 | - | - |
| 5. Animation | 0/3 | - | - |

**Recent Trend:**
- 03-06 completed in 1 minute (Live Rendering Example - gap closure)
- Created LiveRendering.kt with oliveProgram hot reload support
- Renders GeoPackage data (ness-vectors.gpkg) with all geometry types
- Demonstrates projection-to-screen transformation pipeline
- Live-coding capabilities enable creative experimentation without restart
- Phase 3 (Core Rendering) UAT gap closure complete
- All UAT issues resolved: unit tests, basic rendering example, live rendering example
- Ready for Phase 4: Layer System

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
| 02-03 | Dual API style (procedural + extension) | Supports both explicit control and fluent chaining |
| 02-03 | Sequence for lazy batch operations | Performance for large datasets with lazy evaluation |
| 02-03 | Default clamp 85.05112878° (Web Mercator limit) | Prevents pole overflow in Mercator projections |
| 02-03 | Off-screen coordinates remain valid | User controls filtering via isOnScreen() helper |
| 03-01 | Mutable Style class for zero-allocation | Required for real-time animation framerates per CONTEXT.md |
| 03-01 | DSL syntax with invoke() operator | Consistent with ProjectionMercator pattern: Style { fill = RED } |
| 03-01 | Shape enum for v1 (Circle, Square, Triangle) | Sealed enum per RESEARCH.md recommendation |
| 03-01 | mergeStyles() helper for user override | User values override defaults on conflicts |
| 03-02 | writeX() vs drawX() naming convention | writeX() = internal direct drawing, drawX() = public API with style merging |
| 03-02 | Reuse functions across package vs duplication | Same package (geo.render) means functions are accessible without re-declaration |
| 03-02 | ColorRGBa.withAlpha() for fill opacity | Native OpenRNDR method, no separate opacity property needed |
| 03-02 | Guard clauses for minimum geometry points | LineString needs 2+, Polygon needs 3+ to render |
| 03-03 | MultiPolygon renders exterior rings only in v1 | Interior rings (holes) require additional complexity, defer to v2 |
| 03-03 | Point.toScreen() bridges projection and rendering | Phase 2 projections integrate with Phase 3 rendering via extension method |
| 03-03 | Multi* functions delegate to base functions | drawMultiPoint calls drawPoint for each point, reuses existing logic |
| 03-05 | Example programs need else branch for exhaustive when | Kotlin compiler requires exhaustive handling of sealed Geometry class |
| 03-04 | Extended beyond plan: 46 tests vs planned 27 | More comprehensive coverage including edge cases and bounding boxes |
| 03-04 | Functional testing approach for visual components | Verify configuration and function calls, not pixel output (requires drawer mocking) |
| 03-06 | Use oliveProgram {} for live-coding examples | Enables hot reload - code changes reflect without restart |
| 03-06 | GeoPackage over GeoJSON for richer example data | More features and geometry types for visual demonstration |

### Pending Todos

1 pending todo(s):

- Simplify CRS handling API - Make BNG coordinates and other CRS easy to use without manual conversion

### Blockers/Concerns

None yet.

## Session Continuity

Last session: 2026-02-22 14:58 UTC
Stopped at: Completed 03-06-PLAN.md (Live Rendering Example), UAT gap closure complete
Resume file: None
