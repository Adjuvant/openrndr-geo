# Render Examples

This category demonstrates how to render geographic features on screen using OPENRNDR drawing primitives.

## Examples

| File | Concept | Data Used |
|------|---------|-----------|
| 01-points.kt | Point rendering | populated_places.geojson |
| 02-linestrings.kt | LineString rendering | rivers_lakes.geojson |
| 03-polygons.kt | Polygon rendering | sample.geojson |
| 04-multipolygons.kt | MultiPolygon rendering | ocean.geojson |
| 05-style-dsl.kt | Style DSL configuration | sample.geojson |
| 06-quick-geo.kt | Quick geo rendering | populated_places.geojson |
| 07-geostack-render.kt | GeoStack multi-dataset rendering | sample.geojson, rivers_lakes.geojson, populated_places.geojson |

## Key Concepts

- `Style { }` DSL for styling configuration
- Projection transforms coordinates to screen space
- Geometry-specific draw functions: `drawPoint`, `drawLineString`, `drawPolygon`, `drawMultiPolygon`
- Coordinate Reference System (CRS) handling via projections
- `geoStack()` for multi-dataset composition
- `GeoStack.render()` for unified rendering with auto-fit
