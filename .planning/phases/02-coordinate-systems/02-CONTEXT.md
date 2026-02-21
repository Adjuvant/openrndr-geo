# Phase 2: Coordinate Systems - Context

**Gathered:** 2026-02-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Transform coordinates between geographic systems and projections with screen mapping. Users can transform coordinates to screen space for rendering, apply projections like Mercator and Equirectangular, and work with British National Grid (BNG) coordinates with OSTN15 accuracy.

</domain>

<decisions>
## Implementation Decisions

### API invocation style
- Support both procedural functions (`toScreen(lat, lng)`) and extension methods (`latLng.toScreen()`)
- Provide both overload styles: separate parameters and object parameter
- Provide batch operations for performance: `toScreen(points: Sequence<Vector2>)`
- Use OpenRNDR's Vector2 class directly for screen coordinates (integrates with drawing)

### Error handling behavior
- Throw exceptions for invalid inputs (fail fast)
- Throw specific ProjectionOverflowException for projection limits (Mercator 90° poles)
- Recommend clamp method in error messages for handling poles: `clampLatitude(lat, max = 89.999)`
- Throw AccuracyWarningException when outside OSTN15 valid grid area (rich feedback about transformation limits)
- Validate early — latitude in [-90, 90], longitude in [-180, 180]

### Edge case policies
- Normalize individual coordinates to standard range [-180, 180] automatically
- Throw ProjectionUnrepresentableException when latitude = ±90 (Mercator can't handle poles)
- Off-screen coordinates are valid — return Vector2 values (user checks bounds)
- Provide `isOnScreen(point: Vector2, bounds: Rectangle): Boolean` helper for visibility checks

### Projection configuration
- Presets for common use + builders for customization
- Provide both `fitWorld()` for automatic mapping and manual scale/zoom control
- Create ProjectionMercator and ProjectionBNG presets for direct coordinate access
- Use `interface GeoProjection` abstraction for mixing coordinate systems in visualizations
- Throw exception for invalid BNG coordinates outside UK (OSTN15 grid limit)

### OpenCode's Discretion
- Exact implementation of projection builder syntax
- Visibility helper function signature details
- Coordinate system abstraction internal structure

</decisions>

<specifics>
## Specific Ideas

- UK mapping system (EPSG:27700) will be used frequently — BNG coordinates in meters, not lat/lng
- Need to support mixing coordinate sources (lat/lng and BNG) in a single visualization
- Common rendering systems (D3.js, Leaflet, OpenLayers) normalize coordinates automatically

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 02-coordinate-systems*
*Context gathered: 2026-02-21*