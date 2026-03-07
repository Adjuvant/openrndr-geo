---
created: 2026-02-21T14:52
title: Create comprehensive GeoPackage test file with all geometry types
area: testing
files:
  - data/geo/ness-vectors.gpkg
---

## Problem

During UAT for Phase 1 (Data Layer), GeoPackage loading test only verified LineString features because the test data file (ness-vectors.gpkg) contains only LineString features.

Cannot verify that the GeoPackage loader correctly handles all required geometry types:
- Point
- LineString
- Polygon (with exterior and interior rings)
- MultiPoint
- MultiLineString
- MultiPolygon

This limits test coverage and confidence in the data layer implementation.

## Solution

Create a comprehensive GeoPackage test file containing feature examples for all geometry types:

**Required features:**
1. Multiple Point features with different properties
2. LineString features of varying complexity
3. Polygon features with exterior rings only
4. Polygon features with exterior and interior rings
5. MultiPoint features
6. MultiLineString features
7. MultiPolygon features

**Approach options:**
- Use QGIS to manually create test GeoPackage with representative features
- Use geopackage-java library to programmatically create test file
- Find public domain GeoPackage with mixed geometry types and extract subset

Place in `data/geo/` directory alongside existing `ness-vectors.gpkg`.