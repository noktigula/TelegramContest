package com.noktigula.telegramchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ChartView @JvmOverloads constructor(context: Context, attrSet: AttributeSet?=null, defStyleAttr:Int=0) : View(context, attrSet, defStyleAttr) {
    companion object {
        const val BUCKETS = 5
    }

    private lateinit var viewState:ViewState

    val axisPaint = Paint()
    init {
        axisPaint.color = Color.GRAY
        axisPaint.strokeWidth = 1f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        loge("Drawing, state=${viewState}")
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), axisPaint)

        for (i in viewState.yPos.indices) {
            canvas.drawLine(0f, viewState.yPos[i], width.toFloat(), viewState.yPos[i], axisPaint)
            canvas.drawText(viewState.yTitles[i], 0f, viewState.yPos[i], axisPaint)
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
        val maxY = state.maxY()
        val start = maxY / BUCKETS
        val ys = FloatArray(BUCKETS)
        val yTitles = Array(BUCKETS) { "" }
        loge("Creating state, height=${height}")
        for (i in 1 ..BUCKETS) {
            val yPos = (height - (height / ChartView.BUCKETS * i)).toFloat() // TODO minus half text height
            ys[i-1] = yPos
            yTitles[i-1] = "${start * i}"
        }
        return ViewState(ys, yTitles)
    }

    internal data class ViewState(val yPos:FloatArray, val yTitles:Array<String>)
}
