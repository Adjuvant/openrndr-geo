package geo.crs

/**
 * Enumeration of commonly used Coordinate Reference Systems.
 * 
 * Provides strongly-typed CRS selection instead of raw string codes.
 * 
 * ## Usage
 * ```kotlin
 * // Transform to Web Mercator
 * val transformed = source.transform(to = CRS.WebMercator)
 * 
 * // Transform to British National Grid
 * val ukData = source.transform(to = CRS.BritishNationalGrid)
 * ```
 */
enum class CRS(
    val code: String,
    val displayName: String,
    val description: String
) {
    /**
     * WGS84 - World Geodetic System 1984
     * Standard GPS coordinate system
     * EPSG:4326
     */
    WGS84(
        code = "EPSG:4326",
        displayName = "WGS84",
        description = "World Geodetic System 1984 - standard GPS coordinates"
    ),
    
    /**
     * Web Mercator - Spherical Mercator used by web maps
     * Google Maps, OpenStreetMap, Bing Maps
     * EPSG:3857
     */
    WebMercator(
        code = "EPSG:3857",
        displayName = "Web Mercator",
        description = "Spherical Mercator - web map standard"
    ),
    
    /**
     * British National Grid (OSGB36)
     * Ordnance Survey National Grid for UK mapping
     * EPSG:27700
     */
    BritishNationalGrid(
        code = "EPSG:27700",
        displayName = "British National Grid",
        description = "Ordnance Survey National Grid - UK mapping"
    ),
    
    /**
     * Unknown CRS - used when detection fails
     */
    UNKNOWN(
        code = "UNKNOWN",
        displayName = "Unknown",
        description = "Unknown coordinate reference system"
    );
    
    companion object {
        /**
         * Parse a CRS from an EPSG code or name.
         * 
         * Supports:
         * - Full codes: "EPSG:4326", "EPSG:3857", "EPSG:27700"
         * - Short codes: "4326", "3857", "27700"
         * - Names: "WGS84", "WebMercator", "BritishNationalGrid"
         * 
         * @param input The CRS identifier
         * @return The matching CRS or UNKNOWN
         */
        fun fromString(input: String): CRS {
            val normalized = input.trim().lowercase()
            
            return when {
                // Full EPSG codes
                normalized == "epsg:4326" || normalized == "4326" -> WGS84
                normalized == "epsg:3857" || normalized == "3857" -> WebMercator
                normalized == "epsg:27700" || normalized == "27700" -> BritishNationalGrid
                
                // Names (case-insensitive)
                normalized == "wgs84" || normalized == "epsg:4326" -> WGS84
                normalized == "webmercator" || normalized == "web_mercator" || normalized == "epsg:3857" -> WebMercator
                normalized == "britishnationalgrid" || normalized == "bng" || normalized == "osgb36" || normalized == "epsg:27700" -> BritishNationalGrid
                
                // Unknown
                else -> UNKNOWN
            }
        }
        
        /**
         * Get CRS from EPSG code integer.
         */
        fun fromEPSG(code: Int): CRS = fromString("EPSG:$code")
    }
    
    /**
     * Get the EPSG code as an integer, or null if unknown.
     */
    fun toEPSGCode(): Int? {
        return code.removePrefix("EPSG:").toIntOrNull()
    }
    
    /**
     * Check if this CRS is the default WGS84.
     */
    fun isWGS84(): Boolean = this == WGS84
    
    /**
     * Check if this CRS is Web Mercator.
     */
    fun isWebMercator(): Boolean = this == WebMercator
    
    /**
     * Check if this CRS is unknown/unspecified.
     */
    fun isUnknown(): Boolean = this == UNKNOWN
}
