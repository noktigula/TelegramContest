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
        if (builder.xAxis == XAxis.EMPTY || /*builder.yAxis == YAxis.EMPTY ||*/ builder.entries.isEmpty()) {
            throw RuntimeException("Not enough data")
        }
    }
}

class ChartBuilder(
    var xAxis: XAxis = XAxis.EMPTY,
    var yAxis: YAxis = YAxis.EMPTY,
    var entries: Array<LineChartEntry> = arrayOf()
)

data class LineChartState(
    val xAxis: XAxis,
    val yAxis: YAxis,
    val entries: Array<LineChartEntry>
) {
    fun maxY():Long {
        var max = Long.MIN_VALUE
        entries.forEach { entry ->
            entry.data.forEach {y ->
                if (y > max) {
                    max = y
                }
            }
        }
        return max
    }
}

data class XAxis(val title: String, val values: LongArray) {
    companion object {
        val EMPTY = XAxis("", longArrayOf())
    }
}

data class YAxis(val title: String, val values: IntArray) {
    companion object {
        val EMPTY = YAxis("", intArrayOf())
    }
}
data class LineChartEntry(val title: String, val color: Int, val data: LongArray)