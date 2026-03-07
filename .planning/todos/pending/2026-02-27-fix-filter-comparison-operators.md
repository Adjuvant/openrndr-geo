---
created: 2026-02-27T14:50:25.690Z
title: Fix filter comparison operators for property()
area: api
status: scheduled
roadmap_phase: 15
files:
  - src/main/kotlin/geo/Feature.kt:property()
---

## Problem

When using `GeoSource.filter { it.property("population") > 1000 }`, the comparison fails with "Unresolved reference 'compareTo'" because `property()` returns `Any?` which doesn't support comparison operators.

Users want to filter features by numeric property values using standard comparison operators.

## Solution

Option 1: Add `propertyAsComparable<T : Comparable<T>>` method that returns T? instead of Any?
Option 2: Make filter predicate receive typed property accessors directly
Option 3: Document workaround: use `feature.doubleProperty("key") > value` instead

Related: This was noted in original Phase 9 planning as needing typed property accessors.

## Roadmap

**Status:** Scheduled for Phase 15 (API Ergonomics) — v1.4.0 Developer Experience milestone

**ROADMAP.md:** See Phase 15: API Ergonomics — Reduce Boilerplate
**Requirements:** API-02
