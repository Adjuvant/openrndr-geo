---
status: diagnosed
phase: 1
source:
  - 01-01-SUMMARY.md
  - 01-02-SUMMARY.md
  - 01-03-SUMMARY.md
started: 2026-02-21T14:00:00Z
updated: 2026-02-21T15:10:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Load GeoJSON file
expected: |
  When you load a GeoJSON file (using GeoJSON.load() or GeoJSON.loadString()), you should be able to:
  - Read the file without errors
  - Get a Sequence<Feature> containing all features
  - See different geometry types (Point, LineString, Polygon, MultiPoint, etc.) in the features
  - The library should handle malformed features gracefully (skip them with warning, not crash)
result: issue
reported: "Initializer type mismatch: expected 'Sequence<Feature>', actual 'GeoJSONSource'. in GeoJSON.load, needs to be GeoJSONSource"
severity: major

### 2. Access Feature Properties
expected: |
  When you have a Feature from GeoJSON or GeoPackage, you should be able to:
  - Access properties as a Map<String, Any>
  - Get specific types using feature.propertyAs<T>() (e.g., propertyAs<String>("name"), propertyAs<Double>("value"))
  - Use convenience methods like feature.stringProperty(), feature.doubleProperty()
  - Get null (not crash) if property doesn't exist or type mismatch
result: pass

### 3. Load GeoPackage file
expected: |
  When you load a GeoPackage file (using GeoPackage.load()), you should be able to:
  - Read the file without errors
  - Get a GeoPackageSource with Sequence<Feature> streaming all features
  - Features should include geometry (Point, LineString, Polygon, Multi*) parsed correctly
  - CRS information should be retrieved automatically
result: pass

### 4. Query features by region (bounding box)
expected: |
  When you have a GeoPackageSource, you should be able to:
  - Call featuresInBounds(bounds) to get only features in that region
  - Use DSL: features.within(bounds) for spatial filtering
  - Get efficient results (backed by quadtree index, not full scan)
  - Empty bounds returns empty sequence (no crash)
result: pass

## Summary

total: 4
passed: 3
issues: 1
pending: 0
skipped: 0

## Gaps

- truth: "GeoJSON.load() returns Sequence<Feature> for easy access to features"
  status: failed
  reason: "User reported: Initializer type mismatch: expected 'Sequence<Feature>', actual 'GeoJSONSource'. in GeoJSON.load, needs to be GeoJSONSource"
  severity: major
  test: 1
  root_cause: "API follows Source-based factory pattern (not a bug). GeoJSON.load() returns GeoJSONSource which extends GeoSource, providing features AND additional functionality (CRS tracking, spatial queries). User expected direct Sequence<Feature> but actual API returns Source objects with .features property. Both GeoJSON and GeoPackage follow this pattern consistently."
  artifacts:
    - path: "src/main/kotlin/geo/GeoJSON.kt"
      issue: "API returns GeoJSONSource, users expect direct Sequence<Feature>"
  missing:
    - "Add convenience function GeoJSON.features(path) returning Sequence<Feature> directly"
  debug_session: ""