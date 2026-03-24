# Phase 18: Code Organization - Research

**Phase:** 18  
**Date:** 2026-03-24  
**Status:** Complete

## Validation Architecture

This phase is a pure file reorganization task. No validation architecture needed — verification is structural:

- Before: 13 files in `geo/` root, 10 necro files in `geo/examples/`
- After: 13 files in `geo.core/`, necro examples merged or deleted
- Compilation confirms import rewrites are correct

## Technical Approach

### ORG-01: Examples Cleanup
- **Audit 10 files** in `src/main/kotlin/geo/examples/`
- **Merge 4** to `examples/` subdirectories (layer_BlendModes, layer_Output, proj_HaversineDemo, render_LiveRendering)
- **Delete 6** as necro/duplicates (covered by existing examples)

### ORG-02: Core/ Subdirectory
- **Move 13 files** to `geo.core/`: Bounds, CachedGeoSource, Feature, GeoJSON, Geometry, GeoPackage, GeoSource, GeoSourceConvenience, GeoStack, loadGeo, project, ProjectionExtensions, SpatialIndex
- **Flat structure** — 13 files not enough to justify subpackages
- **Single commit** for easy revert

### ORG-03: Import Compatibility
- **Package declaration change** in each file: `package geo` → `package geo.core`
- **Import rewrites** across entire codebase using grep + replace
- **Compile verification** to catch any missed references

## Process

1. Move 13 files to `geo.core/`
2. Update package declarations
3. Batch update imports across: `src/main/`, `examples/`
4. Compile and fix any issues

## Dependencies

None — purely structural refactoring. No functional changes.

## Risks

- **Import misses**: Mitigated by compile verification
- **Git history**: Flat structure preserves history for moved files
- **Build breakage**: Phase 15 hard break precedent — same tooling applies

---

*Research complete*
