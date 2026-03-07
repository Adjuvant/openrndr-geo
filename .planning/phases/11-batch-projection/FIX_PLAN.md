# Fix Plan: Phase 11 Gap Closure

## Issue
Test 1 failed with ClassCastException when using optimize=true with GeoStack.

## Root Cause
OptimizedGeoSource.toFeature() attempts to cast internal optimized geometry (OptimizedLineString) to Geometry sealed class. This is impossible because:
- Optimized geometries are in package `geo.internal.geometry`
- Sealed class Geometry is in package `geo`
- Kotlin sealed classes can only be extended within the same package

## Files to Modify

### 1. OptimizedGeoSource.kt
**Changes:**
- Remove `toFeature()` method (lines 58-64) - it's fundamentally broken
- Add `computeBoundingBox()` method that calculates bounds directly from optimized geometries
- Remove the `features` property override that calls toFeature()

### 2. GeoStack.kt
**Changes:**
- Update `totalBoundingBox()` to check for OptimizedGeoSource and use computeBoundingBox()
- Update `features` property to filter out OptimizedGeoSource or throw informative error
- Ensure render() method continues to work (already handles OptimizedGeoSource correctly)

## Testing
After fix, Test 1 should pass:
- loadGeoJSON with optimize=true works
- GeoStack can render optimized sources
- No ClassCastException

## Estimated Effort
Small fix (~20 lines changed) with high impact.
