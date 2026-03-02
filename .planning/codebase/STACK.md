# Technology Stack

**Analysis Date:** 2026-03-02

## Languages

**Primary:**
- Kotlin 2.3.10 - Main application language (API 2.2, Language 2.2)
- All source files in `src/main/kotlin/`

**Secondary:**
- Gradle (Kotlin DSL) - Build scripts (`build.gradle.kts`, `settings.gradle.kts`, `buildSrc/`)

## Runtime

**Environment:**
- JVM 17 (Java 17 required - Temurin distribution)
- Target/Compatibility: Java 17

**Package Manager:**
- Gradle 9.3.1 with wrapper (`gradle/wrapper/gradle-wrapper.properties`)
- Lockfile: Not present (dynamic version resolution)

## Frameworks

**Core:**
- OPENRNDR 0.5.0-alpha2 - Creative coding framework for generative art/graphics
- OpenGL 3 (`openrndr-gl3`) - Graphics rendering backend (runtime-only)
- GLFW (`openrndr-application-glfw`) - Windowing and input
- OpenAL (`openrndr-openal`) - Audio support

**OPENRNDR Extensions (ORX) 0.5.0-alpha2:**
- Module catalog system via `org.openrndr.extra:orx-module-catalog`
- Basic bundle includes common extensions (camera, color, composition, effects, GUI, noise, etc.)
- orx-olive - Live coding support
- Dialogs support (`openrndr-dialogs`)
- Video support via FFmpeg (`openrndr-ffmpeg`)

**Testing:**
- JUnit 4.13.2 - Unit testing

**Build/Dev:**
- Gradle Shadow 9.3.0 - Fat JAR creation
- Beryx Runtime Plugin 2.0.1 - Native packaging (jpackage)
- Ben Manes Versions 0.53.0 - Dependency update checking

## Key Dependencies

**Critical:**
- kotlinx-coroutines-core 1.10.2 - Async programming
- kotlinx-serialization 1.10.0 - JSON serialization (core + json)
- kotlin-logging 7.0.14 - Logging facade
- slf4j-api 2.0.17 - Logging abstraction
- slf4j-simple 2.0.17 - Simple logging runtime (default)

**Geo/Spatial (NEW):**
- mil.nga.geopackage:geopackage:6.6.5 - GeoPackage format support (OGC standard)
- org.locationtech.proj4j:proj4j:1.4.1 - CRS transformations
- org.locationtech.proj4j:proj4j-epsg:1.4.1 - EPSG database support

**Optional (commented out in libs.versions.toml):**
- jsoup 1.17.1 - HTML parsing
- kotlin-csv-jvm 1.9.3 - CSV handling
- ORSL shader extensions 0.4.5-alpha5 - Shader generation
- ORML 0.4.1 - Machine learning models

**Logging Runtime (FULL mode - not default):**
- log4j-slf4j2 2.23.1 - Log4j2 implementation
- log4j-core 2.23.1 - Log4j2 core
- jackson-databind 2.17.2 - JSON/YAML for logging config
- jackson-dataformat-yaml 2.17.2 - YAML support

## Configuration

**Environment:**
- No environment variables required at runtime
- Logging mode: SIMPLE by default (configurable via `build.gradle.kts`)
- Application main class: `AppKt` (configurable in `gradle.properties`)

**Build:**
- `build.gradle.kts` - Main build configuration
- `settings.gradle.kts` - Project settings with module catalogs for OpenRNDR/ORX
- `gradle.properties` - Project metadata (name=openrndr-geo, group=org.operational-play, version=1.2.0)
- `gradle/libs.versions.toml` - Centralized version catalog
- `buildSrc/src/main/kotlin/conventions/` - Convention plugins:
  - `kotlin-jvm.gradle.kts` - Kotlin/JVM configuration
  - `openrndr-tasks.gradle.kts` - Custom OpenRNDR Gradle tasks
  - `distribute-application.gradle.kts` - Shadow JAR and jpackage config

**Data:**
- `data/fonts/` - Font files (default.otf)
- `data/images/` - Image assets

## Platform Requirements

**Development:**
- JDK 17 or newer
- Supported platforms: Windows, macOS (x64/arm64), Linux (x64/arm64)
- FFmpeg for video features (automatically excluded on ARM architectures via shadow minimize)

**Production:**
- Standalone executable via jpackage (requires JDK 17)
- Cross-platform builds supported
- Native distribution creates platform-specific zip

**CI/CD:**
- GitHub Actions with matrix builds (ubuntu, windows, macos)
- Releases published on tag push (v1.*, v1.*.*)

---

*Stack analysis: 2026-03-02*
