---
phase: 17-performance-fixes
plan: 03
name: 17-03 geometry caching fix for open contours
summary: |
  Fixed the caching of LineString and Point geometries to use open contours (closed=false) in Drawer.geo caching lambda.
  Added unit tests to confirm open contours behavior for lines and points.

results:
  - Fixed cache lambda in DrawerGeoExtensions.kt to use closed=false
  - Added DrawerGeoExtensionsCacheTest.kt with tests for open contours

verification: |
  - Run `./gradlew test` to confirm all tests pass including new cache tests
  - Visual confirmation of line and point rendering shows no unintended closure

duration: TODO

files:
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
  - src/test/kotlin/geo/render/DrawerGeoExtensionsCacheTest.kt

decisions: []
deviations: []

---

# Phase 17 Plan 03: Geometry caching fix for open contours Summary

This plan fixes a critical bug where LineString and Point geometries were incorrectly cached as closed contours. The change ensures open contours by passing `closed = false` in the cache lambda within Drawer.geo.

## Changes made

- Changed DrawerGeoExtensions.kt line 207 from `closed = true` to `closed = false` for non-polygon geometries.
- Created new unit tests in DrawerGeoExtensionsCacheTest.kt verifying open contour behavior for LineString and Point geometries.

## Verification

- All tests pass including new cache tests.
- Manual visual rendering checks to verify lines and points are no longer closed.

## Conclusion

The bug causing closed contours in cached LineString and Point geometries is resolved, ensuring accurate rendering.
