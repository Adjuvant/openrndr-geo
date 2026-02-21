# Technology Stack

**Analysis Date:** 2026-02-21

## Languages

**Primary:**
- Kotlin 2.2.10 - Main application language (all source files in `src/main/kotlin/`)

**Secondary:**
- Gradle (Kotlin DSL) - Build scripts (`build.gradle.kts`, `settings.gradle.kts`)

## Runtime

**Environment:**
- JVM 17 (Java 17 required)
- Uses Temurin distribution in CI

**Package Manager:**
- Gradle 8.14.3 with wrapper
- Lockfile: Not present (dynamic version resolution)

## Frameworks

**Core:**
- OPENRNDR 0.4.5 - Creative coding framework for generative art/graphics
- OpenGL 3 (openrndr-gl3) - Graphics rendering backend

**OPENRNDR Extensions (ORX) 0.4.5:**
- orx-camera - Camera utilities
- orx-color - Color manipulation
- orx-composition - Composition tools
- orx-compositor - Layer composition
- orx-delegate-magic - Property delegation utilities
- orx-envelopes - Envelope/animation curves
- orx-fx - Visual effects/shaders
- orx-gui - GUI elements
- orx-image-fit - Image fitting utilities
- orx-no-clear - Frame persistence
- orx-noise - Noise functions
- orx-olive - Live coding support
- orx-panel - Panel UI
- orx-shade-styles - Shader styling
- orx-shapes - Shape primitives
- orx-svg - SVG support
- orx-text-writer - Text rendering
- orx-video-profiles - Video encoding profiles
- orx-view-box - View box utilities

**Testing:**
- JUnit 4.13.2 - Unit testing

**Build/Dev:**
- Gradle Shadow 9.1.0 - Fat JAR creation
- Badass Runtime Plugin 1.13.1 - Native packaging (jpackage)
- Ben Manes Versions 0.52.0 - Dependency updates

## Key Dependencies

**Critical:**
- kotlinx-coroutines-core 1.10.2 - Async programming
- kotlinx-serialization 1.9.0 - JSON serialization
- kotlin-logging 7.0.13 - Logging facade
- slf4j-api 2.0.17 - Logging abstraction

**Logging Runtime (FULL mode):**
- log4j-slf4j2 2.23.1 - Log4j2 implementation
- log4j-core 2.23.1 - Log4j2 core
- jackson-databind 2.17.2 - JSON/YAML for logging config

**Optional (commented out):**
- jsoup 1.17.1 - HTML parsing
- kotlin-csv-jvm 1.9.3 - CSV handling
- ORSL shader extensions 0.4.5-alpha5 - Shader generation
- ORML 0.4.1 - Machine learning models

## Configuration

**Environment:**
- No environment variables required
- Logging mode configurable in `build.gradle.kts` (NONE, SIMPLE, FULL)

**Build:**
- `build.gradle.kts` - Main build configuration
- `settings.gradle.kts` - Project settings
- `gradle.properties` - Gradle properties (kotlin.code.style=official)
- `gradle/libs.versions.toml` - Centralized version catalog

**Data:**
- `data/fonts/` - Font files (default.otf)
- `data/images/` - Image assets

## Platform Requirements

**Development:**
- JDK 17 or newer
- Supported platforms: Windows, macOS (x64/arm64), Linux (x64/arm64)
- FFmpeg for video features (only on non-arm-v8 architectures)

**Production:**
- Standalone executable via jpackage (requires JDK 17)
- Cross-platform builds supported via `-PtargetPlatform` flag
- Native distribution creates platform-specific zip

**CI/CD:**
- GitHub Actions with matrix builds (ubuntu, windows, macos)
- Releases published on tag push (v1.*)

---

*Stack analysis: 2026-02-21*
