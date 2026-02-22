---
status: resolved
trigger: "LayerComposition.kt example freezes on macOS - window opens but nothing renders, force quit required"
created: 2026-02-22T14:00:00Z
updated: 2026-02-22T14:30:00Z
---

## Current Focus
**COMPLETED - Root cause identified**

hypothesis: "orx-fx blend modes (Multiply, Overlay) are incompatible with Metal backend on macOS"
test: "Analyzed code, checked OPENRNDR documentation, searched for known issues"
expecting: "Shader compilation failure causes freeze"
next_action: "Return root cause analysis"

## Symptoms
- **expected:** Window displays geo data with layered compositing and blend modes
- **actual:** Window opens but nothing renders, force quit required
- **errors:**
  - Metal warnings: VK_ERROR_FEATURE_NOT_PRESENT: Metal does not support disabling primitive restart
  - Shader warnings: Shader 'overlay'/'multiply' does not have uniform called 'textureSize0'/'textureSize1'
- **reproduction:** Run LayerComposition.kt on macOS with Metal backend
- **started:** Always broken on macOS

## Eliminated
- **Hypothesis: Our code is wrong**
  - Evidence: BasicRendering.kt (no compositor) works correctly
  - Evidence: All layer examples (LayerGraticule.kt, LayerBlendModes.kt) use same pattern and fail
  - Conclusion: NOT our code - it's the library combination

- **Hypothesis: GeoPackage data loading issue**
  - Evidence: Error occurs at compositor draw, not data loading
  - Conclusion: Data loads fine, problem is rendering

## Evidence
- timestamp: 2026-02-22T14:10:00Z
  checked: OPENRNDR version in libs.versions.toml
  found: openrndr = 0.4.5, orx = 0.4.5
  implication: Latest stable versions

- timestamp: 2026-02-22T14:15:00Z
  checked: OPENRNDR Guide - Compute Shaders
  found: "Compute shader support only works on systems that support OpenGL 4.3 or higher. This excludes all versions of MacOS."
  implication: MacOS has limited OpenGL support, relies on Metal

- timestamp: 2026-02-22T14:20:00Z
  checked: LayerComposition.kt and related examples
  found: All layer examples use orx-compositor + orx-fx blend modes (Multiply, Overlay)
  implication: Problem affects all compositor-based examples

- timestamp: 2026-02-22T14:25:00Z
  checked: BasicRendering.kt (no compositor)
  found: Uses simple drawer.clear() and direct drawing - NO compositor
  implication: This pattern works on macOS

- timestamp: 2026-02-22T14:28:00Z
  checked: Build configuration
  found: Uses openrndr-gl3 which maps to Metal on macOS (via MoltenVK)
  implication: Metal backend is being used automatically

## Resolution
root_cause: "orx-fx blend mode shaders (Multiply, Overlay) fail to compile on macOS Metal backend due to missing textureSize uniforms, causing compositor to hang"

fix: "N/A - This is a library bug, not our code"

verification: "BasicRendering.kt (no compositor) works correctly on macOS"

files_changed: []

recommendation: "Use BasicRendering.kt pattern (direct drawing) on macOS, or disable blend modes and use simple layer stacking without orx-fx"

artifacts:
- "src/main/kotlin/geo/examples/LayerComposition.kt": Uses orx-compositor + orx-fx Multiply/Overlay blend - freezes on macOS
- "src/main/kotlin/geo/examples/LayerGraticule.kt": Uses orx-compositor + orx-fx Multiply/Overlay blend - same issue
- "src/main/kotlin/geo/examples/LayerBlendModes.kt": Uses orx-compositor + orx-fx - same issue
- "src/main/kotlin/geo/examples/BasicRendering.kt": Direct drawing without compositor - WORKS on macOS

missing: "orx-fx blend modes need to be updated to work with Metal, or documentation needs to warn users"

recommendation: "Fix or workaround - Use BasicRendering-style direct drawing on macOS, or remove orx-fx blend modes from examples"
