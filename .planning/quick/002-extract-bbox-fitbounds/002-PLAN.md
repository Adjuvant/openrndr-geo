# Quick Task 002: Extract bbox from GeoJSON, add fitBounds

## Description
Fix `anim_LineStringColor.kt` to use API features available to cut down bloat of scale setting from GeoJSON loaded in file.

## Changes

### 1. GeoJSON.kt - Parse top-level bbox
- Add `bbox` field to `GeoJSONFeatureCollection` data class
- Parse bbox array into `Bounds` object when present
- Store in `GeoJSONSource` and expose via `boundingBox()` method

### 2. ProjectionFactory.kt - Add fitBounds method
- New `fitBounds(bounds, width, height, padding)` method
- Calculates center and scale automatically from Bounds
- Returns configured Equirectangular projection

### 3. anim_LineStringColor.kt - Simplify
- Replace 30+ lines of manual scale calculation with single `fitBounds()` call
- Use `geojson.boundingBox()` to leverage pre-computed bbox from file
