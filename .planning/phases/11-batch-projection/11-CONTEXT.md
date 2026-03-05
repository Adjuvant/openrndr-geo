# Phase 11: Batch Projection - Context

**Gathered:** 2026-03-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Library can batch-transform coordinate arrays efficiently and integrate batch projection into the rendering pipeline while preserving existing API contracts. Focus on optimization during data loading and CRS transformation, not per-render.

</domain>

<decisions>
## Implementation Decisions

### Batch strategy
- Use standard JVM optimizations (OpenCode discretion on specifics)
- Pre-allocate output arrays for performance (reuse where possible)
- Uniform approach: treat all geometries as coordinate arrays (no special handling per type)
- Batch by coordinate count for optimal performance
- Chunk sizes determined by JVM best practices (not fixed thresholds)

### API surface
- **Opt-in optimization flag**: Users pass `optimize = true` parameter (e.g., `loadGeoJSON(path, optimize = true)`)
- **Helpful console warnings**: When data exceeds thresholds, warn with exact parameters needed and how to enable batching
- **Optimization points**: Data loading and CRS transformation time (not per-render)
- Existing API signatures remain unchanged (backward compatible)
- No new wrapper types or parallel APIs

### Integration approach
- Integration happens at load and project time, not in the render pipeline
- Internal migration: all coordinate transformations use batching internally (no dual implementations)
- Minor signature additions only: optional `optimize` parameter added to relevant functions
- If internal migration becomes messy, acceptable to rewind via git
- All existing code works unchanged without modifications

### Performance validation
- Micro-benchmarks using JMH-style approach for raw projection performance
- Compare against per-point baseline
- Static camera scenarios also useful for measurement
- Goal: "as fast as we can get without being awkward to use" (10x target is arbitrary, focus on practical gains)
- Both structured metrics (for programmatic analysis) and console output (for development feedback)
- Validation approach: OpenCode discretion on practical CI/testing integration

### OpenCode's Discretion
- Exact batch size thresholds and JVM optimization parameters
- Specific console warning message formatting
- How to structure the `optimize` parameter across different functions
- Micro-benchmark implementation details and test scenarios
- Validation approach and CI integration strategy

</decisions>

<specifics>
## Specific Ideas

- Console warnings should be helpful: "Large geometry detected (5,000 points). Consider using optimize=true for better performance"
- API example: `loadGeoJSON("data.geojson", optimize = true)` or `project(geometry, optimize = true)`
- Optimization should kick in during data loading and CRS transformation, not during the frequent render calls
- Migrate everything internally rather than maintaining dual implementations
- If the internal migration becomes problematic, git rewind is acceptable

</specifics>

<deferred>
## Deferred Ideas

- None — discussion stayed within phase scope

</deferred>

---

*Phase: 11-batch-projection*
*Context gathered: 2026-03-05*
