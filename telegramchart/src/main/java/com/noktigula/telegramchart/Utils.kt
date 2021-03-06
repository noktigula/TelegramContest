package com.noktigula.telegramchart

import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver

fun loge(message:String) {
    Log.e("Mylog", message)
}

fun View.afterLayout(action:()->Unit) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object:ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

infix fun Float.asDipByMetrics(metrics:DisplayMetrics) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)