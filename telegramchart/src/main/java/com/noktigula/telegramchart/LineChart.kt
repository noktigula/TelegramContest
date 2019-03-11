package com.noktigula.telegramchart

import android.graphics.Canvas
import android.graphics.Color
import java.lang.RuntimeException

class LineChart {
    fun draw(canvas: Canvas, filler: ChartBuilder.()-> Unit ) {
        val builder = ChartBuilder()
        builder.filler()
        validate(builder)

        drawInternal(canvas,LineChartState(builder.xAxis, builder.yAxis, builder.entries))
    }

    private fun drawInternal(canvas: Canvas, state: LineChartState) {
        //TODO
    }

    fun validate(builder:ChartBuilder) {
        if (builder.xAxis == LineChartAxis.EMPTY || builder.yAxis == LineChartAxis.EMPTY || builder.entries.isEmpty()) {
            throw RuntimeException("Not enough data")
        }
    }
}

class ChartBuilder(
    var xAxis: LineChartAxis = LineChartAxis.EMPTY,
    var yAxis: LineChartAxis = LineChartAxis.EMPTY,
    var entries: Array<LineChartEntry> = arrayOf()
)

data class LineChartState(
    val xAxis: LineChartAxis,
    val yAxis: LineChartAxis,
    val entries: Array<LineChartEntry>
)

data class LineChartAxis(val title: String, val values: IntArray) {
    companion object {
        val EMPTY = LineChartAxis("", intArrayOf())
    }
}
data class LineChartEntry(val title: String, val color: Int, val data: Array<Point>)
data class Point(val x: Int, val y: Int)