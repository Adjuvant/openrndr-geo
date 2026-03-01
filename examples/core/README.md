# Core Examples

This category covers fundamental operations for working with geographic data: loading files, inspecting features, and understanding data structures.

## Examples

| File | Concept | Data Used |
|------|---------|-----------|
| 01-load-geojson.kt | GeoJSON loading | sample.geojson |
| 02-load-geopackage.kt | GeoPackage loading | ness-vectors.gpkg |
| 03-print-summary.kt | Data inspection | sample.geojson, ness-vectors.gpkg |
| 04-geostack.kt | Multi-dataset composition | sample.geojson, populated_places.geojson, rivers_lakes.geojson |

## Key Concepts

- `GeoJSON.load()` and `GeoPackage.load()` for data loading
- `printSummary()` for runtime inspection
- `geoStack()` for combining multiple GeoSources
- Automatic CRS unification across sources
- `totalBoundingBox()` for combined bounds
- Console output (no window) for core examples
