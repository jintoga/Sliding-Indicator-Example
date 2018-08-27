package com.jintoga.sliding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.OvershootInterpolator
import com.jintoga.indicator.R


object AnimationHelper {
    private const val RESIZE_ANIMATION_DURATION = 350L
    private const val TRANSLATION_ANIMATION_DURATION = 380L

    fun animateIndicator(toView: View, fromView: View?, indicatorView: IndicatorView) {
        val indicator = indicatorView.findViewById<View>(R.id.indicator)
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
        widthAnimator.duration = RESIZE_ANIMATION_DURATION

        val fromHeight = fromView.height
        val toHeight = toView.height
        val heightAnimator = ValueAnimator.ofInt(fromHeight, toHeight)
        heightAnimator.addUpdateListener { animation ->
            indicator.layoutParams.height = animation.animatedValue as Int
            indicator.requestLayout()
        }
        heightAnimator.duration = RESIZE_ANIMATION_DURATION

        val margin = indicatorView.resources.getDimension(R.dimen.default_margin)
        val toX = toView.x - margin
        val translationAnimator = ObjectAnimator.ofFloat(indicatorView, "translationX", toX)
        translationAnimator.duration = TRANSLATION_ANIMATION_DURATION

        animatorSet.playTogether(widthAnimator, heightAnimator, translationAnimator)
        animatorSet.start()
    }
}