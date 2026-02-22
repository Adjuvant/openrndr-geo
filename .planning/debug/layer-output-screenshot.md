---
status: resolved
trigger: "told executer to use native DSL openrndr screenshot capability, this method is hard-coding it into example"
created: 2026-02-22T15:00:00Z
updated: 2026-02-22T15:06:00Z
---

## Current Focus

hypothesis: LayerOutput.kt implements screenshot manually using renderTarget/isolatedWithTarget/saveToFile instead of using native OpenRNDR Screenshots extension
test: Compare current implementation to OpenRNDR Screenshots DSL documentation
expecting: Find that current implementation is 20+ lines of manual code vs 1 line native DSL
next_action: Document root cause and recommendation

## Symptoms

expected: Use OpenRNDR's native `extend(Screenshots())` DSL for screenshot capture
actual: Hardcoded manual screenshot implementation using renderTarget, isolatedWithTarget, and colorBuffer.saveToFile
errors: None - this is a code quality issue
reproduction: Open LayerOutput.kt and see lines 254-259 and 280-284 with manual screenshot code
started: When LayerOutput.kt was created

## Evidence

- timestamp: 2026-02-22T15:01:00Z
  checked: LayerOutput.kt lines 254-263 (auto-capture at frame 100)
  found: |
    Manual implementation:
    ```kotlin
    val target = renderTarget(width, height) { colorBuffer() }
    drawer.isolatedWithTarget(target) {
        composite.draw(this)
    }
    target.colorBuffer(0).saveToFile(file)
    ```
  implication: 5 lines of manual buffer management vs 1 line native DSL

- timestamp: 2026-02-22T15:02:00Z
  checked: LayerOutput.kt lines 280-284 (manual keyboard capture)
  found: |
    Same manual implementation repeated for spacebar key capture
  implication: Code duplication - same manual pattern appears twice

- timestamp: 2026-02-22T15:03:00Z
  checked: OpenRNDR Guide - Screenshots extension
  found: |
    Native DSL is simply:
    ```kotlin
    extend(Screenshots())
    ```
    
    With configuration options:
    ```kotlin
    extend(Screenshots()) {
        key = "s"           // custom key trigger
        folder = "output"   // custom folder
        async = false       // synchronous save
    }
    ```
  implication: Native DSL handles all the manual buffer management automatically

- timestamp: 2026-02-22T15:05:00Z
  checked: build.gradle.kts line 320
  found: implementation(openrndr("extensions")) - Screenshots is included in openrndr-extensions
  implication: Screenshots extension is available - no additional dependencies needed

- timestamp: 2026-02-22T15:06:00Z
  checked: OpenRNDR source code reference in guide
  found: org.openrndr.extensions.Screenshots (from guide source code link)
  implication: Correct import path confirmed

## Eliminated

## Resolution

root_cause: |
  LayerOutput.kt implements screenshot functionality manually using OpenRNDR's low-level renderTarget API 
  (renderTarget + isolatedWithTarget + saveToFile) instead of using the native Screenshots extension.
  
  The manual implementation:
  - Is 10+ lines of code (duplicated in two places: auto-capture and keyboard capture)
  - Requires manual renderTarget creation
  - Requires manual isolatedWithTarget call  
  - Requires manual colorBuffer extraction and save
  - Creates screenshots directory manually (unnecessary with Screenshots extension)
  
  The native DSL:
  - Is 1 line of code: extend(Screenshots())
  - Handles all buffer management internally
  - Provides configurable key binding (default: spacebar)
  - Provides configurable folder (default: screenshots/)
  - Automatically creates the screenshots directory
  - Is the officially recommended OpenRNDR approach

artifacts: |
  - src/main/kotlin/geo/examples/LayerOutput.kt:249-263 (auto-capture - manual renderTarget)
  - src/main/kotlin/geo/examples/LayerOutput.kt:274-287 (manual keyboard capture - duplicate)
  - src/main/kotlin/geo/examples/LayerOutput.kt:146-151 (manual directory creation - unnecessary)
  - Missing import: org.openrndr.extensions.Screenshots

missing: |
  The correct implementation should use:
  ```kotlin
  extend(Screenshots())  // Single line replaces 20+ lines of manual code
  ```
  
  Optional configuration:
  ```kotlin
  extend(Screenshots()) {
      key = "s"           // custom key trigger (default: space)
      folder = "output"   // custom folder (default: screenshots/)
      async = false       // synchronous save
  }
  ```

recommendation: |
  Fix LayerOutput.kt to use native OpenRNDR Screenshots extension:
  
  1. ADD import: org.openrndr.extensions.Screenshots
  2. REMOVE lines 146-151 (manual directory creation - Screenshots handles this)
  3. REMOVE lines 249-263 (manual auto-capture at frame 100)
  4. REMOVE lines 268-288 (manual keyboard handler - Screenshots provides this)
  5. ADD after program { }: extend(Screenshots())
  6. UPDATE comments to reflect native DSL usage
  
  This is an EXAMPLE file issue - demonstrates proper OpenRNDR patterns vs hardcoded implementation
