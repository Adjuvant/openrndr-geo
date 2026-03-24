package geo

/**
 * Represents a rectangular bounding box in 2D space.
 * Bounds are defined by minimum and maximum X and Y coordinates.
 *
 * @property minX The minimum X coordinate (left edge)
 * @property minY The minimum Y coordinate (bottom edge)
 * @property maxX The maximum X coordinate (right edge)
 * @property maxY The maximum Y coordinate (top edge)
 */
data class Bounds(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    companion object {
        /**
         * Creates an empty bounds instance.
         * All coordinates are set to NaN to indicate emptiness.
         */
        fun empty() = Bounds(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
    }

    /**
     * Checks if this bounds intersects with another bounds.
     * Empty bounds never intersect with anything.
     *
     * @param other The bounds to test for intersection
     * @return true if the bounds intersect, false otherwise
     */
    fun intersects(other: Bounds): Boolean {
        if (isEmpty() || other.isEmpty()) return false
        return !(maxX < other.minX || minX > other.maxX ||
                 maxY < other.minY || minY > other.maxY)
    }

    /**
     * Checks if this bounds contains the given point.
     *
     * @param x The X coordinate of the point
     * @param y The Y coordinate of the point
     * @return true if the point is within or on the boundary of this bounds
     */
    fun contains(x: Double, y: Double): Boolean {
        return x >= minX && x <= maxX && y >= minY && y <= maxY
    }

    /**
     * Creates a new bounds that expands to include the given point.
     *
     * @param x The X coordinate to include
     * @param y The Y coordinate to include
     * @return A new bounds containing this bounds and the point
     */
    fun expandToInclude(x: Double, y: Double): Bounds {
        if (isEmpty()) {
            return Bounds(x, y, x, y)
        }
        return Bounds(
            minX = minOf(minX, x),
            minY = minOf(minY, y),
            maxX = maxOf(maxX, x),
            maxY = maxOf(maxY, y)
        )
    }

    /**
     * Creates a new bounds that expands to include another bounds.
     *
     * @param other The bounds to include
     * @return A new bounds containing both this and the other bounds
     */
    fun expandToInclude(other: Bounds): Bounds {
        if (other.isEmpty()) return this
        if (isEmpty()) return other
        return Bounds(
            minX = minOf(minX, other.minX),
            minY = minOf(minY, other.minY),
            maxX = maxOf(maxX, other.maxX),
            maxY = maxOf(maxY, other.maxY)
        )
    }

    /**
     * Returns the width of this bounds.
     */
    val width: Double
        get() = maxX - minX

    /**
     * Returns the height of this bounds.
     */
    val height: Double
        get() = maxY - minY

    /**
     * Returns the area of this bounds.
     */
    val area: Double
        get() = width * height

    /**
     * Returns the center point of this bounds.
     * @return Pair of (centerX, centerY)
     */
    val center: Pair<Double, Double>
        get() = Pair((minX + maxX) / 2.0, (minY + maxY) / 2.0)

    /**
     * Checks if this bounds is empty (contains NaN values).
     */
    fun isEmpty(): Boolean {
        return minX.isNaN() || minY.isNaN() || maxX.isNaN() || maxY.isNaN()
    }

    /**
     * Checks if this bounds is valid (non-empty and has proper ordering).
     */
    fun isValid(): Boolean {
        return !isEmpty() && minX <= maxX && minY <= maxY
    }
}
