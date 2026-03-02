# Codebase Concerns

**Analysis Date:** 2026-03-02

## Tech Debt

**Outdated Testing Framework:**
- Issue: Still using JUnit 4.13.2 when JUnit 5 (Jupiter) is the modern standard
- Files: `gradle/libs.versions.toml` (line 15)
- Impact: Missing modern testing features (parameterized tests, nested tests, better assertions)
- Fix approach: Migrate to JUnit 5 with `junit-jupiter` artifacts

**Commented-Out Configuration:**
- Issue: Gradle configuration cache is disabled via comments
- Files: `gradle.properties` (lines 7-9)
- Impact: Slower build times, missing performance optimization
- Fix approach: Investigate why cache is disabled, fix underlying issues, enable it

**No Package Declarations in Templates:**
- Issue: Root-level source files lack package declarations, using default package
- Files: `src/main/kotlin/TemplateProgram.kt`, `src/main/kotlin/TemplateLiveProgram.kt`, `src/main/kotlin/App.kt`
- Impact: Poor code organization, potential naming conflicts, unprofessional structure
- Fix approach: Add appropriate package declarations (e.g., `package org.openrndr.geo`)

**Examples in Wrong Location:**
- Issue: Example files are in `src/main/kotlin/geo/examples/` but should be in a separate examples module or under `src/test/`
- Files: `src/main/kotlin/geo/examples/*.kt`
- Impact: Production artifact bloat, examples compiled into library
- Fix approach: Move examples to dedicated examples module or separate source set

**Scattered TODO Comments:**
- Issue: Multiple TODO comments indicate incomplete features or unclear APIs
- Files: 
  - `src/main/kotlin/App.kt` (lines 77, 84): Chain ordering unexplained
  - `src/main/kotlin/geo/animation/GeoAnimator.kt` (line 114): Singleton pattern concerns
  - `src/main/kotlin/geo/GeoSource.kt` (line 185): Padding units unclear
  - `src/main/kotlin/geo/GeoJSON.kt` (line 312): BoundingBox naming inconsistency
- Impact: Technical debt, unclear code paths
- Fix approach: Create issues for each TODO, resolve or document decisions

## Known Bugs

**macOS Layer Composition Freeze (Platform Limitation):**
- Issue: orx-fx blend mode shaders (Multiply, Overlay) fail on macOS Metal backend
- Files: `src/main/kotlin/geo/examples/layer_Composition.kt`, `layer_BlendModes.kt`, `layer_Graticule.kt`
- Symptoms: Window opens but nothing renders, force quit required
- Root cause: Shader compilation failure due to missing textureSize uniforms on Metal
- Current mitigation: Use direct drawing without compositor (BasicRendering.kt pattern works)
- Recommendations: Avoid compositor + blend modes on macOS; document platform limitation

**Graticule Unbounded Generation (Partially Fixed):**
- Issue: `generateGraticule()` lacks input validation on spacing parameter
- Files: `src/main/kotlin/geo/layer/Graticule.kt` (line 44)
- Symptoms: OOM errors with small spacing values (0.01, 0.001)
- Root cause: Quadratic point generation without limits
- Current mitigation: Status marked as "resolved" in debug docs but code lacks validation
- Recommendations: Add minimum spacing validation (1.0 degree) and maximum point count limit

## Security Considerations

**Log4j2 Dependency:**
- Risk: Log4j has historically had critical vulnerabilities (Log4Shell CVE-2021-44228)
- Files: `gradle/libs.versions.toml` (log4j version 2.23.1)
- Current mitigation: Version 2.23.1 is patched against known major vulnerabilities
- Recommendations: Monitor for future CVEs, consider updating regularly

**Log File Exposure:**
- Risk: `application.log` may capture sensitive information during development
- Files: `src/main/resources/log4j2.yaml`
- Current mitigation: File is gitignored
- Recommendations: Ensure production deployments properly secure log files

**No Input Validation:**
- Risk: File paths are hardcoded without validation
- Files: `src/main/kotlin/App.kt` (lines 41-48), `src/main/kotlin/TemplateProgram.kt`
- Current mitigation: Uses hardcoded demo paths with try-catch
- Recommendations: Add file existence checks and graceful error handling

## Performance Bottlenecks

**Geometry Transformations:**
- Problem: CRS transformations can be expensive for large datasets
- Files: `src/main/kotlin/geo/projection/CRSTransformer.kt`, `src/main/kotlin/geo/GeoSource.kt` (transform methods)
- Cause: Each coordinate requires proj4j transformation
- Improvement path: Cache transformation results, use lazy evaluation more aggressively

**Memory Usage with Large GeoJSON:**
- Problem: Large datasets can cause heap exhaustion
- Files: `src/main/kotlin/geo/GeoJSON.kt` (loading all features)
- Current mitigation: Lazy sequences used in some paths
- Recommendations: Add streaming parsing for very large files

## Fragile Areas

**GeoAnimator Singleton Pattern:**
- Files: `src/main/kotlin/geo/animation/GeoAnimator.kt` (line 114, 115-124)
- Why fragile: Comment notes uncertainty about singleton design; limits to single animation at a time
- Safe modification: Consider instance-based API alongside singleton
- Test coverage: Limited - only basic lifecycle tests exist

**Hardcoded Resource Paths:**
- Files: `src/main/kotlin/App.kt` (lines 41-48), `src/main/kotlin/TemplateProgram.kt`
- Why fragile: Direct string paths without existence checks, will crash if files missing
- Safe modification: Wrap file loading in try-catch or use resource existence validation
- Test coverage: None - no tests for file loading with missing files

**Demo Asset Dependencies:**
- Files: `data/geo/*.geojson`, `data/geo/*.gpkg`, `data/images/*.png`, `data/fonts/*.otf`
- Why fragile: Examples and App.kt depend on these specific files existing
- Safe modification: Ensure any new projects either keep these files or update source code
- Test coverage: None for missing asset handling

**CRS Transformation Edge Cases:**
- Files: `src/main/kotlin/geo/projection/CRSTransformer.kt`
- Why fragile: Uncommon CRS combinations may fail silently or produce incorrect results
- Safe modification: Add validation for known-good CRS combinations
- Test coverage: Basic tests exist but edge cases not fully covered

## Scaling Limits

**Single-Program Architecture:**
- Current capacity: Single main class per run
- Limit: Cannot easily run multiple OPENRNDR programs simultaneously
- Scaling path: Create separate modules or use programmatic configuration switching

**GeoAnimator Singleton:**
- Current capacity: One global animation controller
- Limit: Cannot animate multiple independent things with different timing
- Scaling path: Support instance-based animator creation

**No Modularization:**
- Current capacity: All code in single module
- Limit: Will become unwieldy as project grows
- Scaling path: Consider splitting into core, render, animation modules

## Dependencies at Risk

**JUnit 4:**
- Risk: End-of-life framework, no new features
- Impact: Missing modern testing capabilities, eventual deprecation
- Migration plan: Update to JUnit 5 (`org.junit.jupiter:junit-jupiter:5.x.x`)

**OpenRNDR 0.5.0-alpha2:**
- Risk: Alpha version may have instability or breaking changes before final release
- Impact: API changes may require code updates
- Migration plan: Monitor for stable release, test thoroughly on updates

**Proj4j 1.4.1:**
- Risk: Older library with potential compatibility issues
- Impact: CRS transformation accuracy or performance issues
- Migration plan: Evaluate modern alternatives (GeoTools, custom implementations)

**Kotlin 2.3.10:**
- Risk: Kotlin updates frequently, may need updates for compatibility
- Impact: May miss language improvements and bug fixes
- Migration plan: Regular version updates as part of maintenance

## Missing Critical Features

**Limited Error Handling:**
- Problem: File loading and parsing lacks comprehensive error handling
- Files: `src/main/kotlin/geo/GeoJSON.kt`, `src/main/kotlin/geo/GeoPackage.kt`
- Blocks: Graceful degradation for corrupt/malformed data

**Incomplete Animation API:**
- Problem: GeoAnimator is a placeholder with TODO for actual tweening
- Files: `src/main/kotlin/geo/animation/GeoAnimator.kt`
- Blocks: Production-ready animations (FeatureAnimator exists but needs integration)

## Test Coverage Gaps

**File Loading Error Paths:**
- What's not tested: Missing files, malformed data, permission errors
- Files: `src/main/kotlin/geo/GeoJSON.kt`, `src/main/kotlin/geo/GeoPackage.kt`
- Risk: Silent failures or crashes in production
- Priority: Medium

**CRS Edge Cases:**
- What's not tested: Exotic CRS combinations, invalid CRS codes, transformation failures
- Files: `src/main/kotlin/geo/projection/CRSTransformer.kt`
- Risk: Silent data corruption or runtime exceptions
- Priority: Medium

**Rendering Edge Cases:**
- What's not tested: Empty geometries, null styles, extreme coordinates
- Files: `src/main/kotlin/geo/render/*.kt`
- Risk: Visual glitches or crashes
- Priority: Medium

**Animation Edge Cases:**
- What's not tested: Rapid start/stop, concurrent animations
- Files: `src/main/kotlin/geo/animation/*.kt`
- Risk: State corruption, memory leaks
- Priority: Low

**Test Count:**
- Current: 24 test files, ~4200 lines of test code
- Coverage: Core functionality covered, but edge cases and error paths need work

---

*Concerns audit: 2026-03-02*
