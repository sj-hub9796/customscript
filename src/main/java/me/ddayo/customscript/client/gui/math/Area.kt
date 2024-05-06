package me.ddayo.goosegooseduck.client.math

import me.ddayo.goosegooseduck.client.math.Point.Companion.with


interface IArea {
    val points: Array<out Point>
    fun isIn(p: Point): Boolean

    val minX: Double
    val minY: Double
    val maxX: Double
    val maxY: Double
    val centerX: Double
    val centerY: Double

    operator fun times(x: Double): IArea
}

class RTArea(override vararg val points: Point) : IArea {
    override fun isIn(p: Point): Boolean {
        var inside = false

        var j = points.size - 1
        for (i in points.indices) {
            if ((points[i].y > p.y) != (points[j].y > p.y) &&
                (p.x < (points[j].x - points[i].x) * (p.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)
            )
                inside = !inside
            j = i
        }
        return inside
    }

    override val minX get() = points.minOf { it.x }
    override val minY get() = points.minOf { it.y }
    override val maxX get() = points.maxOf { it.x }
    override val maxY get() = points.maxOf { it.y }
    override val centerX get() = (minX + maxX) / 2
    override val centerY get() = (minY + maxY) / 2

    override fun times(x: Double): IArea {
        return RTArea(*points.map { (it.x * x) with (it.y * x) }.toTypedArray())
    }
}

class Area(override vararg val points: Point) : IArea {
    override fun isIn(p: Point): Boolean {
        val rst = Point.ccw(points.last(), points.first(), p).run {
            if (this == 0.0) return true
            this < 0
        }
        return !(0..points.size - 2).any { (Point.ccw(points[it], points[it + 1], p) < 0) != rst }
    }

    companion object {
        fun rectArea(p1: Point, p2: Point) = Area(p1, p1.x with p2.y, p2, p2.x with p1.y)
    }

    override operator fun times(x: Double): Area {
        return Area(*points.map { (it.x * x) with (it.y * x) }.toTypedArray())
    }

    override val minX get() = points.minOf { it.x }
    override val minY get() = points.minOf { it.y }
    override val maxX get() = points.maxOf { it.x }
    override val maxY get() = points.maxOf { it.y }
    override val centerX get() = (minX + maxX) / 2
    override val centerY get() = (minY + maxY) / 2
}