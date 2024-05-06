package me.ddayo.goosegooseduck.client.math

import me.ddayo.goosegooseduck.client.math.Point.Companion.with
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.sqrt

abstract class LinearFunction {
    abstract fun getY(x: Double): Double
    abstract fun getSide(p: Point): Boolean
    abstract fun getDistance(p: Point): Double

    companion object {
        fun create(p1: Point, p2: Point) = if(p1.x == p2.x) InfLinearFunction(p1.x) else FLinearFunction(p1, p2)
        fun create(p: Point) = create(p, 0 with 0)
    }
}

private class FLinearFunction(val m: Double, val b: Double): LinearFunction() {
    override fun getY(x: Double) = m * x + b
    override fun getSide(p: Point) = p.y > getY(p.x)
    override fun getDistance(p: Point) = abs(p.x * m - p.y + b) / sqrt(m * m + 1)

    constructor(x1: Double, y1: Double, x2: Double, y2: Double): this((y1 - y2) / (x1 - x2), y1 - x1 * (y1 - y2) / (x1 - x2))
    constructor(p1: Point, p2: Point): this(p1.x, p1.y, p2.x, p2.y)
}

private class InfLinearFunction(val definedX: Double): LinearFunction() {
    override fun getY(x: Double): Double = Double.POSITIVE_INFINITY
    override fun getSide(p: Point) = p.x > definedX
    override fun getDistance(p: Point) = abs(definedX - p.x)
}