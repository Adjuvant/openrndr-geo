# Phase 20: Layer Features - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Fix and enhance graticule layer for zoomed-in maps. The graticule provides geographic reference for map visualizations. This phase improves the API ergonomics and visual quality when viewing at different zoom levels.

Requirements: LAYER-01

</domain>

<decisions>
## Implementation Decisions

### Grid Line Generation
- Generate **LineString features directly**, not points
- Return as **MultiLineString**: all horizontal lines in one geometry, all vertical lines in another
- Package in a **GeoLayer with `latLines` and `lngLines` properties** (each is a GeoSource)
- Rationale: Clean API — `drawer.geo(graticule.latLines)` just works. Consistent with how other geo data renders.

### Adaptive Spacing
- **Auto-adapt based on viewport**: system automatically selects appropriate spacing based on visible geographic bounds
- Use **power-of-10 grid**: 1°, 10°, 30°, 90° based on visible degrees
- **1° minimum floor**: don't go below 1° spacing even when very zoomed in
- Rationale: Standard cartographic convention, prevents visual clutter

### Labeling
- **Include label generation** as part of the graticule layer
- Return label positions + text (e.g., "45°N", "120°W")
- User handles rendering with `drawer.text()`
- **Degrees with direction format**: standard cartographic notation (45°N, 120°W, 0°)
- **Custom CRS accommodation**: labels must work with CRS like BNG that uses meters, not just degrees/latitude
- **Simple toggle via parameter flag**: `includeLabels(true)` or `includeLabels` property
- **Default: OFF** (to reduce clutter by default)
- Latitude labels appear at **left edge of viewport**
- Longitude labels appear at **bottom edge of viewport**

### Density Management
- **Auto-thinning**: when lines would be too dense (< ~20px apart), automatically increase spacing
- **Clip to viewport**: only draw lines within the visible bounds, clean edges
- **Handle antimeridian gracefully**: split lines at ±180° longitude (same approach as Phase 16 Polygon handling)
- Rationale: Keep visualization clean at all zoom levels

### OpenCode's Discretion
- Exact pixel threshold for auto-thinning (20px suggested, may need tuning)
- Label font size and styling
- How to handle BNG/meter-based CRS labels specifically (research needed)
- Implementation of line clipping algorithm

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `Graticule.kt` (src/main/kotlin/geo/layer/Graticule.kt) — existing graticule generation logic, will be replaced/modified
- `GeoLayer.kt` — layer composition wrapper, pattern to follow
- Phase 16 antimeridian splitting logic in `Geometry.kt` or normalization utilities — can reference for splitting approach
- `ScreenTransform.kt` — batch transformation functions for coordinate operations

### Established Patterns
- GeoLayer DSL pattern: `GeoLayer { source = ...; style = ... }`
- Line rendering via `drawer.geo(source)` already works for LineString features
- Multi-geometry handled via MultiLineString in existing rendering path

### Integration Points
- Modify `generateGraticuleSource()` function signature
- New functions: `generateGraticuleLayer(bounds, includeLabels, ...)` returning GeoLayer
- Layer API remains compatible with existing `drawer.geo()` rendering

</code_context>

<specifics>
## Specific Ideas

- "Labels should work with BNG (British National Grid) which uses meters, not degrees"
- "Default should be off — less clutter for users who don't want labels"
- "Power-of-10 grid spacing: 1°, 10°, 30°, 90°"
- "Handle antimeridian like we did for polygons in Phase 16"
</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 20-layer-features*
*Context gathered: 2026-03-24*
