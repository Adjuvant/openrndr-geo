package geo.internal.batch

import org.openrndr.math.Vector2

/**
 * Batch coordinate storage using DoubleArray pairs for contiguous memory.
 * 
 * This internal class stores coordinates as two DoubleArrays (x[], y[]) instead of
 * List<Vector2> to improve cache locality and reduce allocation overhead during
 * batch transformation operations.
 * 
 * Per RESEARCH.md Pattern 1: DoubleArray pairs for contiguous memory
 */
internal class CoordinateBatch(
    val x: DoubleArray,
    val y: DoubleArray
) {
    init {
        require(x.size == y.size) { 
            "CoordinateBatch x and y arrays must have same size: ${x.size} vs ${y.size}" 
        }
    }
    
    /**
     * Number of coordinate pairs in this batch.
     */
    val size: Int get() = x.size
    
    /**
     * Converts this batch to an OPENRNDR-compatible Array<Vector2>.
     * Used for rendering with OPENRNDR's Drawer.
     */
    fun toVector2Array(): Array<Vector2> = Array(size) { i ->
        Vector2(x[i], y[i])
    }
    
    /**
     * Converts this batch to a List<Vector2>.
     * Used for APIs requiring List instead of Array.
     */
    fun toVector2List(): List<Vector2> = List(size) { i ->
        Vector2(x[i], y[i])
    }
    
    companion object {
        /**
         * Creates a CoordinateBatch from a list of Vector2 points.
         */
        fun fromPoints(points: List<Vector2>): CoordinateBatch {
            val x = DoubleArray(points.size) { i -> points[i].x }
            val y = DoubleArray(points.size) { i -> points[i].y }
            return CoordinateBatch(x, y)
        }
        
        /**
         * Creates a single-element CoordinateBatch from a Vector2 point.
         */
        fun fromPoint(point: Vector2): CoordinateBatch {
            return CoordinateBatch(
                doubleArrayOf(point.x),
                doubleArrayOf(point.y)
            )
        }
        
        /**
         * Creates an empty CoordinateBatch.
         */
        fun empty(): CoordinateBatch {
            return CoordinateBatch(DoubleArray(0), DoubleArray(0))
        }
    }
}
