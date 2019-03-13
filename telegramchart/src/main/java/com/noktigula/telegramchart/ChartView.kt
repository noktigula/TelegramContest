package com.noktigula.telegramchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class ChartView @JvmOverloads constructor(context: Context, attrSet: AttributeSet?=null, defStyleAttr:Int=0) : View(context, attrSet, defStyleAttr) {
    companion object {
        const val BUCKETS = 6
        const val DAY_MILLIS = 24*60*60*1000
        const val VISIBLE_DATES = 6
    }

    private lateinit var viewState:ViewState

    val axisPaint = Paint()
    val axisTextPaint = Paint()
    init {
        axisPaint.color = Color.GRAY
        axisPaint.strokeWidth = 1f

        axisTextPaint.color = Color.GRAY
        axisTextPaint.textSize = 16f asDipByMetrics context.resources.displayMetrics
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        val w = width.toFloat()
        val h = height.toFloat()

        val datesHeight = drawDates(canvas, viewState)

        //TODO add margin to text
        canvas.drawLine(0f, h-datesHeight, w, h-datesHeight, axisPaint)
        canvas.drawText("0", 0f, h, axisTextPaint)

        for (i in viewState.yPos.indices) {
            canvas.drawLine(0f, viewState.yPos[i], width.toFloat(), viewState.yPos[i], axisPaint)
            canvas.drawText(viewState.yTitles[i], 0f, viewState.yPos[i], axisTextPaint)
        }
    }

    //@returns max height of text
    private fun drawDates(canvas: Canvas, state:ViewState) : Float {
        var max = 0f
        val rect = Rect()
        for(i in viewState.datePos.indices) {
            canvas.drawText(viewState.dateTitles[i], viewState.datePos[i], height.toFloat()-1, axisTextPaint)
            axisTextPaint.getTextBounds(viewState.dateTitles[i], 0, viewState.dateTitles[i].length, rect)
            if (rect.height() > max) {
                max = rect.height().toFloat()
            }
        }
        return max
    }

    fun setState(state:LineChartState) {
        if (height == 0) {
            afterLayout {
                this.viewState = stateFromChartState(state)
            }
        } else {
            this.viewState = stateFromChartState(state)
        }

        invalidate()
        requestLayout()
    }

    private fun stateFromChartState(state: LineChartState) : ViewState {
        //FIXME FIRST need to include text offset, so it needs to be ready at this point
        val maxY = roundToHundred(state.maxY())
        val lines = BUCKETS - 1
        val start = maxY / BUCKETS
        val ys = FloatArray(lines)
        val yTitles = Array(lines) { "" }
        for (i in 1 ..lines) {
            val yPos = (height - (height / BUCKETS * i)).toFloat() // TODO minus half text height
            ys[i-1] = yPos
            yTitles[i-1] = "${start * i}"
        }
        //TODO accept window together with chart
        val currentWindow = windowByIndices(state,0, 10)
        val windowDates = datesPositions(currentWindow)
        val dateTitles = readableDates(currentWindow)
        return ViewState(ys, yTitles, windowDates, dateTitles, currentWindow)
    }

    private fun roundToHundred(x:Long) = ((x+99) / 100) * 100

    fun windowByIndices(state:LineChartState, startIndex: Int, endIndex:Int) : ChartWindow {
        return ChartWindow(
                startDate = state.xAxis.values[startIndex],
                endDate = state.xAxis.values[endIndex],
                startIndex = startIndex,
                endIndex = endIndex
        )
    }

    fun datesPositions(window:ChartWindow) : FloatArray {
        val step = (width / VISIBLE_DATES).toFloat()
        val xPos = FloatArray(VISIBLE_DATES)
        for(i in 1 .. VISIBLE_DATES) {
            xPos[i-1] = step * i
        }
        return xPos
    }

    fun readableDates(window:ChartWindow) : Array<String> {
        val roundedStart = window.startDate % DAY_MILLIS
        val roundedEnd = window.startDate % DAY_MILLIS
        var deltaDays = 0
        while((roundedStart+deltaDays) < roundedEnd) {
            deltaDays++
        }

        var readableDates = Array(VISIBLE_DATES){ ""}
        val step = deltaDays / VISIBLE_DATES
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        for (i in 1 .. VISIBLE_DATES) {
            val nextDate = roundedStart + (step * i * DAY_MILLIS)
            val date = Date(nextDate)
            readableDates[i-1] = formatter.format(date)
        }
        return readableDates
    }

    internal data class ViewState(
            val yPos:FloatArray,
            val yTitles:Array<String>,
            val datePos:FloatArray,
            val dateTitles:Array<String>,
            val chartWindow: ChartWindow
    )

    data class ChartWindow(val startDate:Long, val endDate:Long, val startIndex:Int, val endIndex:Int)
}
