package geo.examples

import geo.Feature
import geo.GeoJSON
import geo.GeoPackage
import geo.GeoSource

fun main(){
    val jsonDataSource: GeoSource = try {
        GeoJSON.load("data/sample.geojson")
    } catch (e: Exception) {
        println("✗ Failed to load file: ${e.message}")
        return
    }
    jsonDataSource.printSummary()

    println("✓ GeoJson Data inspection completed successfully")

    val gpkgDataSource: GeoSource = try{
        GeoPackage.load("data/geo/ness-vectors.gpkg")
    } catch (e: Exception){
        println("x Failed to load file: ${e.message}")
        return
    }
    gpkgDataSource.printSummary()
    println("✓ GeoPackage Data inspection completed successfully")

    val emptySource = object : GeoSource() {
        override val features: Sequence<Feature> = emptySequence()
    }
    emptySource.printSummary()
    println("✓ Handles empty source gracefully")
}