# Phase 1: Data Layer - Context

**Gathered:** 2026-02-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Load and access geo data from multiple formats (GeoJSON and GeoPackage) with efficient querying. Users can load geo files, access features with Point/LineString/Polygon/Multi* geometries, query GeoPackage features by bounding box, and access feature properties. Rendering, styling, and visualization are covered in later phases.

</domain>

<decisions>
## Implementation Decisions

### API Entry Points
- **Loading format**: File paths as strings — e.g., `GeoJSON.load("data/cities.geojson")`
- **API surface**: Top-level functions in dedicated files (`GeoJSON.kt`, `GeoPackage.kt`)
- **GeoPackage querying**: Different method names to indicate behavior (e.g., `load()` vs `loadWithQuery()`)
- **Return types**: Both collection and queryable source available for flexibility

### Feature Access Patterns
- **Primary access**: Both available — property `source.features` for quick access, methods for specific queries
- **Spatial queries**: Infix notation DSL — `source.features within bounds` (Kotlin-native feel)
- **Collection type**: User chooses - `features` (sequence, lazy) vs `listFeatures()` (list, eager)
- **Properties**: Map-like API — `feature.properties["name"]` with direct Map access

### Error Handling Strategy
- **Malformed files**: Throw exception (fail-fast, force user to handle)
- **Invalid geometry**: Skip that feature, log warning (permissive for individual features)
- **Missing properties**: Return Option/Nullable type (explicit null handling)
- **Unknown CRS**: Offer CRS transformation with `autoTransformTo(targetCRS)` option

### Geometry Representation
- **Hierarchy**: Sealed class `Geometry` with `Point`, `LineString`, `Polygon`, etc. subclasses (exhaustive when expression support)
- **Coordinate access**: Named properties — `point.x`, `point.y` (clean, readable)
- **Multi-geometries**: List property — `multiPoint.points` returns `List<Point>` (eager, cacheable)
- **OpenRNDR integration**: Use OpenRNDR types directly (e.g., `Vector2` for points) since drawing will use OpenRNDR
- **Library nature**: OpenRNDR plugin/library, not pure Kotlin — integrates with OpenRNDR's drawing system

### OpenCode's Discretion
- Exact naming conventions for GeoPackage query methods
- Spatial query result ordering
- Progress/logging verbosity for large file operations
- CRS transformation error handling

</decisions>

<specifics>
## Specific Ideas

- "Use OpenRNDR points for later building" — geometry types should integrate seamlessly with OpenRNDR's drawing system
- This is an OpenRNDR plugin/library, not a standalone Kotlin geo library — design accordingly

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 01-data-layer*
*Context gathered: 2026-02-21*