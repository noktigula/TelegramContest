package com.noktigula.telegramchart

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ChartView @JvmOverloads constructor(context: Context, attrSet: AttributeSet?=null, defStyleAttr:Int=0) : View(context, attrSet, defStyleAttr) {
    val textSize = 16f asDipByMetrics context.resources.displayMetrics
    var chart = LineChart(textSize, LineChartState.EMPTY)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        chart.draw(canvas)
    }

    fun setState(state:LineChartState) {
        if (height == 0) {
            afterLayout {
                this.chart = LineChart(textSize, state)
            }
        } else {
            this.chart = LineChart(textSize, state)
        }

        invalidate()
        requestLayout()
    }

    data class ChartWindow(val startDate:Long, val endDate:Long, val startIndex:Int, val endIndex:Int) {
        fun size() = endIndex - startIndex
    }
}
