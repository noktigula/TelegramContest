package com.noktigula.telegramchart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*


class LineChart(val textSize:Float, val state:LineChartState) {
    companion object {
        const val BUCKETS = 6
        const val DAY_MILLIS = 24*60*60*1000
        const val VISIBLE_DATES = 6
    }
    val axisPaint = Paint()
    val axisTextPaint = Paint()
    val linePaints = ArrayList<Paint>()
    init {
        axisPaint.color = Color.GRAY
        axisPaint.strokeWidth = 1f

        axisTextPaint.color = Color.GRAY
        axisTextPaint.textSize = textSize
    }

    fun draw(canvas: Canvas) {
        //TODO merge LineChartState and ViewState
        val viewState = stateFromChartState(canvas, state)
        drawInternal(canvas, viewState)
    }

    private fun stateFromChartState(canvas: Canvas, state: LineChartState) : ViewState {
        //TODO accept window together with chart
        val currentWindow = windowByIndices(state,0, 10)
        val windowDates = datesPositions(canvas)
        val dateTitles = readableDates(currentWindow)

        //FIXME FIRST need to include text offset, so it needs to be ready at this point
        val maxY = roundToHundred(state.maxY())
        val lines = BUCKETS - 1
        val start = maxY / BUCKETS
        val ys = FloatArray(lines)
        val yTitles = Array(lines) { "" }
        val textOffset = measureDatesOffset(dateTitles)
        val adjustedHeight = canvas.height - textOffset
        for (i in 1 ..lines) {
            val yPos = (adjustedHeight - (adjustedHeight / BUCKETS * i)).toFloat() // TODO minus half text height
            ys[i-1] = yPos
            yTitles[i-1] = "${start * i}"
        }

        updateLinePaints(linePaints, state.entries)

        return ViewState(
                yPos = ys,
                yTitles = yTitles,
                datePos = windowDates,
                dateTitles = dateTitles,
                dateYOffset = textOffset,
                chartWindow = currentWindow,
                yStep = adjustedHeight / maxY,
                lines = state.entries)
    }

    fun drawDates(canvas: Canvas, viewState:ViewState) : Float {
        var max = 0f
        val rect = Rect()
        for(i in viewState.datePos.indices) {
            canvas.drawText(viewState.dateTitles[i], viewState.datePos[i], canvas.height.toFloat() - 1, axisTextPaint)
            axisTextPaint.getTextBounds(viewState.dateTitles[i], 0, viewState.dateTitles[i].length, rect)
            if (rect.height() > max) {
                max = rect.height().toFloat()
            }
        }
        return max
    }

    private fun updateLinePaints(receiver:MutableList<Paint>, source:Array<LineChartEntry>) {
        receiver.clear()
        receiver.addAll(source.map {
            val paint = Paint()
            paint.strokeWidth = 5f
            paint.color = Color.rgb(it.color and 0xff0000, it.color and 0x00ff00, it.color and 0x0000ff)
            paint
        })
    }

    private fun roundToHundred(x:Long) = ((x+99) / 100) * 100

    fun windowByIndices(state:LineChartState, startIndex: Int, endIndex:Int) : ChartView.ChartWindow {
        return ChartView.ChartWindow(
                startDate = state.xAxis.values[startIndex],
                endDate = state.xAxis.values[endIndex],
                startIndex = startIndex,
                endIndex = endIndex
        )
    }

    fun datesPositions(canvas: Canvas) : FloatArray {
        val step = (canvas.width / VISIBLE_DATES).toFloat()
        val xPos = FloatArray(VISIBLE_DATES)
        xPos[0] = 0f
        for(i in 1 until VISIBLE_DATES) {
            xPos[i] = step * i
        }
        return xPos
    }

    fun readableDates(window: ChartView.ChartWindow) : Array<String> {
        var deltaDays = (window.endDate - window.startDate) / DAY_MILLIS

        var readableDates = Array(VISIBLE_DATES){ ""}
        val step = deltaDays / VISIBLE_DATES
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        for (i in 1 .. VISIBLE_DATES) {
            val nextDate = window.startDate + (step * i * DAY_MILLIS)
            val date = Date(nextDate)
            readableDates[i-1] = formatter.format(date)
        }
        return readableDates
    }

    private fun drawInternal(canvas: Canvas, viewState: ViewState) {
        val dateTextOffset = drawDates(canvas, viewState)
        val drawableArea = canvas.height - dateTextOffset

        drawYLines(canvas, drawableArea, viewState.yPos, viewState.yTitles)
        drawChartLines()


    }

    fun drawChartLines(canvas:Canvas) {
        //TODO current assumption that each point index corresponds to date index
        for (i in viewState.lines.indices) {
            val line = viewState.lines[i]
            for (j in 1 until viewState.chartWindow.size()-1) {
                //FIXME there is inconsistency between window size and actually displayable dates
                val yStart = drawableArea - line.data[j-1] * viewState.yStep
                val yEnd = drawableArea - line.data[j] * viewState.yStep
                if (j < viewState.datePos.size) {
                    canvas.drawLine(viewState.datePos[j-1], yStart, viewState.datePos[j], yEnd, linePaints[i])
                }
            }
        }
    }


    //FIXME FIRST need to find a way to allow change method calls without changing the results and without side effects
    private fun draw(canvas: Canvas) {
        drawInternal(canvas)
    }

    private fun drawYLinesInternal(canvas: Canvas, drawableArea:Float, yLines:FloatArray, yTitles: Array<String>) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        canvas.drawLine(0f, drawableArea, w, drawableArea, axisPaint)
        canvas.drawText("0", 0f, drawableArea, axisTextPaint)

        for (i in yLines.indices) {
            canvas.drawLine(0f, yLines[i], canvas.width.toFloat(), yLines[i], axisPaint)
            canvas.drawText(yTitles[i], 0f, yLines[i], axisTextPaint)
        }
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
    companion object {
        val EMPTY = LineChartState(
                XAxis.EMPTY,
                YAxis.EMPTY,
                arrayOf()
        )
    }
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

data class ViewState(
        val yPos:FloatArray,
        val yTitles:Array<String>,
        val datePos:FloatArray,
        val dateTitles:Array<String>,
        val dateYOffset: Float,
        val chartWindow: ChartView.ChartWindow,
        val yStep:Float,
        val lines:Array<LineChartEntry>
)

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