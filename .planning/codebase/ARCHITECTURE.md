# Architecture

**Analysis Date:** 2026-02-21

## Pattern Overview

**Overall:** OPENRNDR Creative Coding Application

**Key Characteristics:**
- Declarative DSL-based program configuration using Kotlin lambdas
- Immediate-mode rendering with frame-by-frame draw callbacks
- Extension-based architecture via ORX (OPENRNDR eXtensions)
- Live-coding support through oliveProgram for hot-reloading code changes
- Cross-platform native deployment via jpackage

## Layers

**Application Layer:**
- Purpose: Program entry points and configuration
- Location: `src/main/kotlin/*.kt`
- Contains: Top-level Kotlin files with `fun main() = application { }`
- Depends on: OPENRNDR core, ORX extensions
- Used by: JVM runtime

**Configuration Layer:**
- Purpose: Build, dependency, and runtime configuration
- Location: `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`
- Contains: Gradle build logic, version catalog, feature toggles
- Depends on: Gradle plugins
- Used by: Build system

**Resource Layer:**
- Purpose: Static assets and runtime configuration
- Location: `data/`, `src/main/resources/`
- Contains: Images, fonts, logging configuration
- Depends on: None
- Used by: Application layer

## Data Flow

**Program Initialization:**

1. `fun main() = application { }` - Entry point defines application
2. `configure { }` - Window dimensions, title, and display settings
3. `program { }` or `oliveProgram { }` - Program logic setup
4. Resource loading (`loadImage`, `loadFont`) - Called once during setup
5. `extend { }` - Drawing callback registered for each frame

**Render Loop:**

1. Framework calls `extend { }` block each frame
2. `drawer` object provides immediate-mode rendering API
3. Drawing commands (circles, images, text) execute in order
4. Frame buffer presents to screen

**State Management:**
- Per-frame: `seconds` double provides elapsed time for animation
- Global: Variables declared in `program { }` scope persist across frames
- Drawer state: Fill color, stroke, transforms modified per-frame

## Key Abstractions

**Application Block:**
- Purpose: Root DSL builder that creates the OPENRNDR window and context
- Examples: `src/main/kotlin/TemplateProgram.kt`, `src/main/kotlin/TemplateLiveProgram.kt`
- Pattern: `application { configure { } program { } }`

**Program Block:**
- Purpose: Setup phase for loading resources and registering draw callbacks
- Examples: All program files use `program { }` or `oliveProgram { }`
- Pattern: Variables declared here are accessible in `extend { }`

**Extend Block:**
- Purpose: Frame-by-frame rendering callback
- Examples: All programs use `extend { drawer.circle(...) }`
- Pattern: Called once per frame at display refresh rate

**Drawer:**
- Purpose: Immediate-mode 2D rendering API
- Examples: `drawer.circle()`, `drawer.image()`, `drawer.text()`
- Pattern: Stateful - fill/stroke/font affect subsequent commands

## Entry Points

**Standard Program:**
- Location: `src/main/kotlin/TemplateProgram.kt`
- Triggers: `./gradlew run` or `./gradlew run -Popenrndr.application=TemplateProgramKt`
- Responsibilities: Window config, resource loading, render loop

**Live-Coding Program:**
- Location: `src/main/kotlin/TemplateLiveProgram.kt`
- Triggers: `./gradlew run -Popenrndr.application=TemplateLiveProgramKt`
- Responsibilities: Same as standard, plus hot-reload capability via `oliveProgram`

**Custom Programs:**
- Location: `src/main/kotlin/*.kt` (flat structure, no package by default)
- Triggers: `./gradlew run -Popenrndr.application=CustomProgramKt`
- Responsibilities: User-defined creative coding programs

## Error Handling

**Strategy:** JVM exception propagation with logging

**Patterns:**
- Log4j2 configured via `src/main/resources/log4j2.yaml`
- Console output to STDERR with color-coded levels
- File logging to `application.log`
- Logging level controlled by `applicationLogging` in `build.gradle.kts` (NONE, SIMPLE, FULL)

## Cross-Cutting Concerns

**Logging:** 
- SLF4J facade with Log4j2 backend
- Kotlin-logging library for idiomatic logging
- Config: `src/main/resources/log4j2.yaml`

**Build Configuration:**
- Gradle version catalog at `gradle/libs.versions.toml`
- Feature toggles via `orxFeatures`, `ormlFeatures`, `openrndrFeatures` sets
- Platform detection auto-selects natives

**Cross-Platform Deployment:**
- jpackage creates native executables
- macOS: `.app` bundle with Resources directory
- Windows/Linux: Standard executables
- Data directory bundled with distribution

**Extension System:**
- ORX features enabled by adding to `orxFeatures` set in `build.gradle.kts`
- Examples: `orx-camera`, `orx-gui`, `orx-noise`, `orx-shapes`, `orx-svg`
- ORML machine learning features available via `ormlFeatures`
- ORSL shader features available via commented dependencies

---

*Architecture analysis: 2026-02-21*
