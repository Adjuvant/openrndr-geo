package geo.internal.cache

import geo.Geometry

/**
 * Composite cache key combining viewport state and geometry reference.
 *
 * Uses identity equality (===) for geometry comparison rather than content hash.
 * This follows the Unity-style caching pattern where geometry objects are identified
 * by their object reference, not their content. This avoids expensive content hashing
 * for large geometries.
 *
 * @property viewportState The viewport configuration affecting projection
 * @property geometryRef Reference to the geometry object (identity-based)
 */
internal data class CacheKey(
    val viewportState: ViewportState,
    val geometryRef: Geometry
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CacheKey) return false

        // Viewport state compared by value (data class equals)
        // Geometry compared by identity (reference equality)
        return viewportState == other.viewportState &&
                geometryRef === other.geometryRef
    }

    override fun hashCode(): Int {
        // Combine viewport state hash with identity hash code for geometry
        // 31 is a standard prime multiplier for hash code combinations
        return 31 * viewportState.hashCode() + System.identityHashCode(geometryRef)
    }
}
