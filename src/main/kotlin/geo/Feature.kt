package geo

/**
 * Represents a geographic feature combining geometry with properties.
 * Features are the primary unit of geographic data - each feature has
 * a geometry (shape/location) and a set of properties (attributes).
 *
 * @property geometry The geometry defining the feature's shape and location
 * @property properties A map of string keys to property values (can be null)
 */
data class Feature(
    val geometry: Geometry,
    val properties: Map<String, Any?> = emptyMap()
) {
    /**
     * Gets a property value by key.
     *
     * @param key The property name
     * @return The property value, or null if not present
     */
    fun property(key: String): Any? = properties[key]

    /**
     * Gets a property value by key with type safety.
     * Returns null if the key doesn't exist or the value is not of the expected type.
     *
     * @param key The property name
     * @return The property value cast to type T, or null
     */
    inline fun <reified T> propertyAs(key: String): T? {
        return properties[key] as? T
    }

    /**
     * Checks if a property with the given key exists.
     *
     * @param key The property name to check
     * @return true if the property exists, false otherwise
     */
    fun hasProperty(key: String): Boolean = properties.containsKey(key)

    /**
     * Gets a string property value.
     *
     * @param key The property name
     * @return The string value, or null if not present or not a string
     */
    fun stringProperty(key: String): String? = propertyAs<String>(key)

    /**
     * Gets a double property value.
     *
     * @param key The property name
     * @return The double value, or null if not present or not a number
     */
    fun doubleProperty(key: String): Double? = propertyAs<Number>(key)?.toDouble()

    /**
     * Gets an integer property value.
     *
     * @param key The property name
     * @return The integer value, or null if not present or not a number
     */
    fun intProperty(key: String): Int? = propertyAs<Number>(key)?.toInt()

    /**
     * Gets a boolean property value.
     *
     * @param key The property name
     * @return The boolean value, or null if not present or not a boolean
     */
    fun booleanProperty(key: String): Boolean? = propertyAs<Boolean>(key)

    /**
     * Gets all property keys.
     *
     * @return Set of property names
     */
    fun propertyKeys(): Set<String> = properties.keys

    /**
     * The bounding box of this feature's geometry.
     */
    val boundingBox: Bounds
        get() = geometry.boundingBox

    companion object {
        /**
         * Creates a feature from a point with optional properties.
         */
        fun fromPoint(x: Double, y: Double, properties: Map<String, Any?> = emptyMap()): Feature {
            return Feature(Point(x, y), properties)
        }
    }
}
