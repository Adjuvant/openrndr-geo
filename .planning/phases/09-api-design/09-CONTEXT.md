# Phase 9: API Design - Context

**Gathered:** 2026-02-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Users have intuitive API matching OpenRNDR conventions. Feature-level iteration with projected coordinates internalized, two-tier API (beginner + professional), escape hatches for advanced users, and DSL style matching OpenRNDR patterns.

Requirements: API-01, API-02, API-03, API-04

</domain>

<decisions>
## Implementation Decisions

### Feature Iteration
- Lambda-based iteration: `features.forEach { feature -> ... }`
- Hybrid projection: internalized as base case (screen coordinates), both geo and screen coordinates available
- Each iteration provides: feature + projected geometry (ready to render)
- Chainable operations: `filter`, `map`, `forEach` — leverage feature properties (e.g., height from dataset-specific naming conventions)

### Two-tier API
- Primary entry: `drawer.geo(source)` — generic, works for any GeoSource (GeoJSON, GeoPackage), render feature parity
- Same function with additional parameters for detailed control: `drawer.geo(source, projection = ..., style = ...)`
- Detailed API includes: projection control + styling + per-feature overrides
- Sensible defaults: minimal, black background, thin white lines

### Escape Hatches
- Direct geometry access via `feature.geometry`
- Naming convention: "write" prefix for internals, "draw" for user API
- Raw projection type for bypassing standard projections (not `project = false` flag)
- Both style options: style function per feature `style = { feature -> Style(...) }` AND style map by type `{ "Polygon" to polygonStyle }`

### OpenRNDR Conventions
- Builder with configuration blocks: `drawer.geo(source) { projection = ...; style = ... }`
- Extension functions on Drawer as primary entry point (matches `drawer.circle()`, `drawer.rectangle()`)
- Factory functions / DSL for configuration objects: `Mercator()`, `Style { stroke = ColorRGBa.RED }`
- Explicit, composable design — each piece does one thing, power from combining

### OpenCode's Discretion
- Exact syntax for chainable operations (filter/map/forEach)
- How raw projection type is configured
- Implementation details of style function vs style map precedence

</decisions>

<specifics>
## Specific Ideas

- "drawer.geo that can match what it needs to use extensions for under the hood" — generic entry point, GeoSource-agnostic
- "write" = internals, "draw" = user API — naming convention to separate concerns
- Properties access for height, population, etc. — dataset-specific naming conventions handled by user in filter/map

</specifics>

<deferred>
## Deferred Ideas

### Per-feature Callback (v1.3)
Deferred to v1.3 — needs more working through. Ideas to capture:
- Layer-based styling/ordering integration
- Conditional rendering per feature (skip or customize based on properties)
- Inline lambda in config block: `drawer.geo(source) { feature -> if (feature.properties["pop"] > 1000) drawCustom(feature) }`

Current v1.2 approach: Feature filtering before render is adequate for advanced users.

</deferred>

---

*Phase: 09-api-design*
*Context gathered: 2026-02-27*
