# Codebase Structure

**Analysis Date:** 2026-02-21

## Directory Layout

```
openrndr-geo/
├── .github/                # CI/CD workflows
│   └── workflows/          # GitHub Actions
├── data/                   # Runtime assets
│   ├── fonts/              # Font files (.otf)
│   └── images/             # Image assets (.png, .jpg)
├── gradle/                 # Gradle configuration
│   ├── wrapper/            # Gradle wrapper binaries
│   └── libs.versions.toml  # Version catalog
├── src/                    # Source code
│   └── main/
│       ├── kotlin/         # Kotlin source files
│       └── resources/      # Runtime resources
├── build.gradle.kts        # Build configuration
├── gradle.properties       # Gradle settings
├── settings.gradle.kts     # Project settings
├── gradlew                 # Gradle wrapper (Unix)
└── gradlew.bat             # Gradle wrapper (Windows)
```

## Directory Purposes

**`src/main/kotlin/`:**
- Purpose: All Kotlin source files
- Contains: Top-level `.kt` files with OPENRNDR programs
- Key files: `TemplateProgram.kt`, `TemplateLiveProgram.kt`
- Convention: Flat structure, no subpackages by default

**`src/main/resources/`:**
- Purpose: Runtime configuration files bundled into JAR
- Contains: `log4j2.yaml` for logging configuration
- Key files: `log4j2.yaml`
- Note: NOT for images/fonts - use `data/` directory instead

**`data/`:**
- Purpose: External assets loaded at runtime
- Contains: Images, fonts, and other media files
- Key files: `data/fonts/default.otf`, `data/images/pm5544.png`
- Note: Bundled with jpackage distributions, separate from JAR

**`gradle/`:**
- Purpose: Gradle build configuration
- Contains: Version catalog and wrapper
- Key files: `libs.versions.toml` (all dependency versions)

**`.github/workflows/`:**
- Purpose: CI/CD automation
- Contains: GitHub Actions workflow files
- Key files: `build-on-commit.yaml`, `publish-binaries.yaml`

## Key File Locations

**Entry Points:**
- `src/main/kotlin/TemplateProgram.kt`: Standard program template
- `src/main/kotlin/TemplateLiveProgram.kt`: Live-coding program template

**Configuration:**
- `build.gradle.kts`: Main build configuration, ORX feature toggles
- `gradle/libs.versions.toml`: Dependency versions (OPENRNDR, ORX, Kotlin)
- `gradle.properties`: Gradle settings (task visibility, Kotlin style)
- `settings.gradle.kts`: Project name and plugin repositories

**Resources:**
- `src/main/resources/log4j2.yaml`: Logging configuration
- `data/fonts/default.otf`: Default UI font
- `data/images/pm5544.png`: Test pattern image
- `data/images/cheeta.jpg`: Sample photograph

**CI/CD:**
- `.github/workflows/build-on-commit.yaml`: Build verification
- `.github/workflows/publish-binaries.yaml`: Cross-platform releases

## Naming Conventions

**Files:**
- Kotlin programs: `PascalCase.kt` (e.g., `TemplateProgram.kt`)
- Entry point class: `<Name>Kt` suffix for Gradle (e.g., `TemplateProgramKt`)
- Gradle files: `lowercase.gradle.kts`
- Workflows: `kebab-case.yaml`

**Directories:**
- Source packages: lowercase (if packages used)
- Asset categories: lowercase plural (e.g., `fonts/`, `images/`)

**Programs:**
- Main function: `fun main() = application { }`
- Configuration: `configure { }` block
- Setup: `program { }` or `oliveProgram { }` block
- Render: `extend { }` block

## Where to Add New Code

**New Program:**
- Create file: `src/main/kotlin/MyProgram.kt`
- Copy structure from `TemplateProgram.kt`
- Run with: `./gradlew run -Popenrndr.application=MyProgramKt`

**New Assets:**
- Images: `data/images/my-image.png`
- Fonts: `data/fonts/my-font.ttf`
- Load with: `loadImage("data/images/my-image.png")`

**New ORX Extension:**
- Edit: `build.gradle.kts`
- Add to `orxFeatures` set: `"orx-feature-name"`
- Reload Gradle configuration

**New Dependencies:**
- External libs: Add to `gradle/libs.versions.toml` under `[libraries]`
- Reference in: `build.gradle.kts` as `implementation(libs.my.lib)`

**Tests (Future):**
- Location: `src/test/kotlin/` (currently not present)
- Follow: Standard Gradle test structure

## Special Directories

**`gradle/wrapper/`:**
- Purpose: Gradle wrapper for consistent builds
- Generated: Yes (by Gradle)
- Committed: Yes

**`data/`:**
- Purpose: Runtime assets (not bundled in JAR, loaded from filesystem)
- Generated: No
- Committed: Yes
- Bundle: Copied to jpackage output

**`.planning/codebase/`:**
- Purpose: GSD planning documents
- Generated: Yes (by GSD tools)
- Committed: Optional

## Build Outputs

**Development:**
- `./gradlew run` - Compiles and runs directly
- No intermediate artifacts needed

**Distribution:**
- `build/libs/openrndr-geo-1.0.0-all.jar` - Fat JAR (shadowJar task)
- `build/jpackage/` - Native executable bundle (jpackage task)
- `build/distributions/openrndr-application.zip` - Release artifact

## Platform-Specific Notes

**macOS:**
- Uses `-XstartOnFirstThread` JVM arg for GLFW compatibility
- jpackage creates `.app` bundle
- Data files placed in `Contents/Resources/data/`

**Windows:**
- Use `gradlew.bat` instead of `./gradlew`
- jpackage creates executable in `bin/` directory

**Linux:**
- Standard executable in `bin/` directory
- Both x64 and arm64 architectures supported

---

*Structure analysis: 2026-02-21*
