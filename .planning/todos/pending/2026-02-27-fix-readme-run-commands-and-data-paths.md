---
created: 2026-02-27T21:33:11.455Z
title: Fix README run commands and data paths
area: docs
files:
  - examples/README.md:39
  - examples/proj/README.md
  - examples/anim/README.md
  - examples/layer/README.md
---

## Problem

Phase 10 verification found 2 minor documentation issues in README files:

1. **Incorrect run commands**: proj/README.md, anim/README.md, and layer/README.md show class names with "Kt" suffix (e.g., `MercatorKt`) but should NOT have it due to `@file:JvmName` annotations. The correct class names are without suffix (e.g., `Mercator`).

2. **Incorrect data path**: examples/README.md line 39 says `data/geo/` but should be `examples/data/geo/`

These are purely documentation bugs - the actual code and functionality work correctly. Users may be confused if they copy-paste run commands from README files.

## Solution

Fix the README files:

1. In examples/proj/README.md, examples/anim/README.md, examples/layer/README.md:
   - Remove "Kt" suffix from run command examples
   - Example: `./gradlew run -Popenrndr.application=examples.proj.Mercator` (not `MercatorKt`)

2. In examples/README.md line 39:
   - Change `data/geo/` to `examples/data/geo/`

This is a quick fix - no research needed.
