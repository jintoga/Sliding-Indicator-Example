package com.jintoga.sliding

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
class IndicatorView @JvmOverloads constructor(
        private val layoutId: Int,
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        init()
    }

    private fun init() {
        inflate(context, layoutId, this)
    }

    fun getIndicator(): View = this.getChildAt(0)

}