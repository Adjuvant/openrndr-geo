---
created: 2026-02-26T17:21
title: Integrate Spatial K for GeoJSON loading
area: api
files:
  - src/main/kotlin/geo/GeoSource.kt
---

## Problem

Need to evaluate using Spatial K [https://maplibre.org/spatial-k/](https://maplibre.org/spatial-k/) as the GeoJSON loading backend. This library offers additional spatial analysis features beyond just loading. Key decision needed: should we implement a facade pattern to abstract the GeoJSON loading functionality? This would provide flexibility to swap implementations in the future if a better tool emerges, while keeping the turf-based parts of the API unchanged.

## Solution

TBD - Research required:
1. Evaluate Spatial K's GeoJSON loading capabilities vs current implementation
2. Assess additional features it brings (spatial operations, coordinate transforms)
3. Design facade pattern around GeoJSON loading if beneficial
4. Ensure turf-based operations remain decoupled and stable

## Context

Spatial K appears to be a comprehensive Kotlin library for spatial data. Integration needs careful consideration of the API surface - we want to leverage its power without creating tight coupling that limits future options. The facade pattern discussion is crucial for long-term maintainability.
