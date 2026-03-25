package geo.organization

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class Phase18OrganizationTest {

    @Test
    fun org01_four_necro_files_moved_to_examples_subdirs() {
        val movedFiles = listOf(
            "examples/layer/layer_BlendModes.kt",
            "examples/layer/layer_Output.kt",
            "examples/proj/proj_HaversineDemo.kt",
            "examples/render/render_LiveRendering.kt"
        )
        assertEquals("Expected 4 necro files moved", 4, movedFiles.count { File(it).exists() })
    }

    @Test
    fun org01_necro_examples_directory_cleaned() {
        val necroDir = File("src/main/kotlin/geo/examples")
        if (necroDir.exists()) {
            val remaining = necroDir.listFiles()?.filter { it.name.endsWith(".kt") } ?: emptyList()
            assertTrue("Expected 0 .kt files in geo/examples/, found: ${remaining.size}", remaining.isEmpty())
        }
    }

    @Test
    fun org02_thirteen_geo_root_files_moved_to_geo_core() {
        val coreFiles = listOf(
            "src/main/kotlin/geo/core/Bounds.kt",
            "src/main/kotlin/geo/core/CachedGeoSource.kt",
            "src/main/kotlin/geo/core/Feature.kt",
            "src/main/kotlin/geo/core/GeoJSON.kt",
            "src/main/kotlin/geo/core/Geometry.kt",
            "src/main/kotlin/geo/core/GeoPackage.kt",
            "src/main/kotlin/geo/core/GeoSource.kt",
            "src/main/kotlin/geo/core/GeoSourceConvenience.kt",
            "src/main/kotlin/geo/core/GeoStack.kt",
            "src/main/kotlin/geo/core/ProjectionExtensions.kt",
            "src/main/kotlin/geo/core/SpatialIndex.kt",
            "src/main/kotlin/geo/core/loadGeo.kt",
            "src/main/kotlin/geo/core/project.kt"
        )
        assertEquals("Expected 13 files in geo.core/", 13, coreFiles.count { File(it).exists() })
    }

    @Test
    fun org02_all_core_files_declare_package_geo_core() {
        val coreDir = File("src/main/kotlin/geo/core")
        val ktFiles = coreDir.listFiles()?.filter { it.name.endsWith(".kt") } ?: emptyList()
        assertEquals("Expected 13 .kt files in geo.core/", 13, ktFiles.size)

        for (file in ktFiles) {
            val content = file.readText()
            assertTrue("File ${file.name} should declare 'package geo.core'", content.contains("package geo.core"))
        }
    }

    @Test
    fun org02_no_geo_root_kt_files_remain() {
        val geoRoot = File("src/main/kotlin/geo")
        val rootKtFiles = geoRoot.listFiles()?.filter { 
            it.isFile && it.name.endsWith(".kt") && it.name != "examples" && !it.name.startsWith(".")
        } ?: emptyList()
        assertTrue(
            "No .kt files should remain in geo root. Found: ${rootKtFiles.map { it.name }}",
            rootKtFiles.isEmpty()
        )
    }

    @Test
    fun org03_geo_core_imports_are_correct() {
        val coreTypes = listOf("Bounds", "Feature", "GeoJSON", "Geometry", "GeoSource")
        val srcDir = File("src/main/kotlin/geo")
        val filesWithGeoPrefix = mutableListOf<String>()
        
        srcDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filter { !it.path.contains("/geo/core/") }
            .forEach { file ->
                val content = file.readText()
                for (type in coreTypes) {
                    val badImport = "import geo.$type"
                    val goodImport = "import geo.core.$type"
                    if (content.contains(badImport) && !content.contains(goodImport)) {
                        filesWithGeoPrefix.add("${file.name}: $badImport")
                    }
                }
            }
        
        assertTrue(
            "No direct 'geo.X' imports should remain in src (should be geo.core.X). Found: $filesWithGeoPrefix",
            filesWithGeoPrefix.isEmpty()
        )
    }
}
