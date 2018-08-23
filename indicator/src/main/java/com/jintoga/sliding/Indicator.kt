package com.jintoga.sliding

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.jintoga.indicator.R

class Indicator @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        init()
    }

    private fun init() {
        inflate(context, R.layout.item_indicator_view, this)
    }

}