package com.jintoga.sliding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator


class AnimationHelper private constructor(private val resizeDuration: Long,
                                          private val translationDuration: Long) {

    companion object {
        const val DEFAULT_RESIZE_ANIMATION_DURATION = 350L
        const val DEFAULT_TRANSLATION_ANIMATION_DURATION = 380L

        @Volatile
        private var INSTANCE: AnimationHelper? = null

        fun getInstance(resizeDuration: Long,
                        translationDuration: Long): AnimationHelper =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: AnimationHelper(resizeDuration, translationDuration)
                            .also { INSTANCE = it }
                }
    }


    fun animateIndicator(toView: View, fromView: View?, indicatorView: IndicatorView) {
        val indicator = indicatorView.getIndicator()
        if (fromView == null) {
            indicator.layoutParams.width = 0
            indicator.layoutParams.height = 0
            indicator.requestLayout()
            return
        }

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = OvershootInterpolator(0.85f)
        val fromWidth = fromView.width
        val toWidth = toView.width
        val widthAnimator = ValueAnimator.ofInt(fromWidth, toWidth)
        widthAnimator.addUpdateListener { animation ->
            indicator.layoutParams.width = animation.animatedValue as Int
            indicator.requestLayout()
        }
        widthAnimator.duration = resizeDuration

        val fromHeight = fromView.height
        val toHeight = toView.height
        val heightAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
        heightAnimator.addUpdateListener { animation ->
            indicator.layoutParams.height = animation.animatedValue as Int
            indicator.requestLayout()
        }
        heightAnimator.duration = resizeDuration

        val margin = (toView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
        val toX = toView.x - margin
        val translationAnimator = ObjectAnimator.ofFloat(indicatorView, "translationX", toX)
        translationAnimator.duration = translationDuration

        animatorSet.playTogether(widthAnimator, heightAnimator, translationAnimator)
        animatorSet.start()
    }
}