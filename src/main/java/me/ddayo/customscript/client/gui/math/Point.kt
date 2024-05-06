package me.ddayo.goosegooseduck.client.math

data class Point(val x: Double, val y: Double) {
    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())

    companion object {
        infix fun Double.with(other: Double) = Point(this, other)
        infix fun Int.with(other: Int) = Point(this, other)

        fun ccw(p1: Point, p2: Point, p3: Point): Double {
            val a = p1.x * p2.y + p2.x * p3.y + p3.x * p1.y
            val b = p1.y * p2.x + p2.y * p3.x + p3.y * p1.x
            return a - b
        }
    }

    infix operator fun minus(other: Point) = (x - other.x) with (y - other.y)
    infix operator fun plus(other: Point) = (x + other.x) with (y + other.y)
    infix operator fun div(other: Double) = (x / other) with (y / other)
    infix operator fun div(other: Int) = (x / other) with (y / other)
    infix fun center(other: Point) = (this + other) / 2
}