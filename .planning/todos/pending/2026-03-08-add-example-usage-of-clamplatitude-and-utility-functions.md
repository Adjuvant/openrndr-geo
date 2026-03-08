---
created: 2026-03-08T02:53:11Z
title: Add example usage of clampLatitude and utility functions
area: docs
files:
  - src/main/kotlin/geo/projection/UtilityFunctions.kt:13-15
  - examples/render/04-multipolygons.kt:36-38
  - examples/proj/
---

## Problem

The ocean data example (04-multipolygons.kt) loads ocean.geojson but may fail to render properly because the ocean data contains coordinates with latitudes beyond the Mercator valid range. The UtilityFunctions.kt file contains `clampLatitude()` which should be used to handle this, but there's no example showing how to use it.

Additionally, the `examples/proj/` directory only has 3 examples and doesn't demonstrate the utility functions available in UtilityFunctions.kt:
- `clampLatitude()` - Clamp latitude to valid Mercator range
- `normalizeLongitude()` - Normalize longitude to [-180, 180]
- `normalizeCoordinate()` - Normalize both coordinates
- `isOnScreen()` - Check if screen coordinate is visible
- `isValidCoordinate()` - Check if coordinate is within valid geographic range
- `isBNGValid()` - Check if BNG coordinates are within UK grid

## Solution

1. **Update 04-multipolygons.kt**: Add example usage of `clampLatitude()` to show how to handle ocean data that may have coordinates outside valid Mercator bounds. Show how to pre-process data before projection.

2. **Create new example in examples/proj/**: Add 04-utility-functions.kt demonstrating:
   - Using clampLatitude with different max values
   - Normalizing coordinates that cross the antimeridian
   - Validating coordinates before projection
   - Checking if projected coordinates are on screen

## Context

From user request: "need example usage of clampLatitute from UtilityFunctions.kt good place would be in examples/render/04-multipolygons.kt as the ocean never loads due to latitute going over max."

## Impact

Users will understand how to handle edge cases with coordinate validation and clamping, especially for global datasets like ocean data that may contain coordinates outside standard Mercator bounds.
