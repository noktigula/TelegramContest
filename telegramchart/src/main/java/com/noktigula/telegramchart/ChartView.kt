package com.noktigula.telegramchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class ChartView @JvmOverloads constructor(context: Context, attrSet: AttributeSet?=null, defStyleAttr:Int=0) : View(context, attrSet, defStyleAttr) {
    companion object {
        const val BUCKETS = 6
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
        canvas.drawLine(0f, h-1, w, h-1, axisPaint)
        canvas.drawText("0", 0f, h, axisTextPaint)

        for (i in viewState.yPos.indices) {
            canvas.drawLine(0f, viewState.yPos[i], width.toFloat(), viewState.yPos[i], axisPaint)
            canvas.drawText(viewState.yTitles[i], 0f, viewState.yPos[i], axisTextPaint)
        }
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
        return ViewState(ys, yTitles)
    }

    private fun roundToHundred(x:Long) = ((x+99) / 100) * 100

    internal data class ViewState(val yPos:FloatArray, val yTitles:Array<String>)
}
