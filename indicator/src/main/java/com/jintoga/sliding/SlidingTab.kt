package com.jintoga.sliding

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.jintoga.indicator.R

class SlidingTab @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var mDistributeEvenly: Boolean = false

    private var mViewPager: ViewPager? = null

    private lateinit var backContainer: LinearLayout
    private lateinit var frontContainer: LinearLayout
    private lateinit var indicator: Indicator

    private var currentView: View? = null
    private var currentViewWidth = 0
    private var currentViewHeight = 0

    init {
        init(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {

        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        // Make sure that the Tab Strips fills this View
        isFillViewport = true


        val holderView = FrameLayout(context)
        backContainer = LinearLayout(context)
        backContainer.setHorizontalGravity(LinearLayout.HORIZONTAL)
        holderView.addView(backContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        indicator = Indicator(context)
        holderView.addView(indicator, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        frontContainer = LinearLayout(context)
        frontContainer.setHorizontalGravity(LinearLayout.HORIZONTAL)
        holderView.addView(frontContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        addView(holderView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    fun setViewPager(viewPager: ViewPager?) {
        backContainer.removeAllViews()
        frontContainer.removeAllViews()

        mViewPager = viewPager
        if (viewPager != null) {
            populateTabStrip()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentViewWidth == 0 || currentViewHeight == 0) {
            val indicatorView = indicator.findViewById<View>(R.id.indicator)
            val params = indicatorView.layoutParams
            val width = currentView?.width ?: 0
            val height = currentView?.height ?: 0
            params.width = width
            params.height = height
            indicatorView.layoutParams = params
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        currentViewWidth = currentView?.width ?: 0
        currentViewHeight = currentView?.height ?: 0
    }


    private fun populateTabStrip() {
        val adapter = mViewPager?.adapter!!
        val tabClickListener = TabClickListener()
        populateTab(backContainer, adapter, null,
                R.layout.item_background_view)
        populateTab(frontContainer, adapter, tabClickListener,
                R.layout.item_foreground_view)
        if (currentView == null) {
            currentView = frontContainer.getChildAt(0)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    private fun populateTab(tabStrip: LinearLayout,
                            adapter: PagerAdapter,
                            listener: View.OnClickListener?,
                            layoutId: Int) {
        for (i in 0 until adapter.count) {
            val textView: TextView
            val container: View

            val rootView = LayoutInflater.from(context).inflate(layoutId, tabStrip,
                    false)
            container = rootView.findViewWithTag("container")
            textView = rootView.findViewWithTag("textView")

            if (mDistributeEvenly) {
                val lp = rootView.layoutParams as LinearLayout.LayoutParams
                lp.width = 0
                lp.weight = 1f
            }

            textView.text = adapter.getPageTitle(i)
            if (listener != null) {
                container.setOnClickListener(listener)
            }
            tabStrip.addView(rootView)
        }
    }


    private inner class TabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            for (i in 0 until frontContainer.childCount) {
                if (v === frontContainer.getChildAt(i)) {
                    mViewPager?.currentItem = i
                    currentView = v
                    animateIndicator(v)
                    return
                }
            }
        }
    }


    private fun animateIndicator(v: View) {
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = OvershootInterpolator(0.85f)

        val toWidth = v.width.toFloat()
        val widthAnimator = ValueAnimator.ofInt(currentViewWidth, toWidth.toInt())
        widthAnimator.addUpdateListener { animation ->
            val indicatorView = indicator.findViewById<View>(R.id.indicator)
            indicatorView.layoutParams.width = animation.animatedValue as Int
            indicatorView.requestLayout()
        }
        widthAnimator.duration = 300

        val toHeight = v.height.toFloat()
        val heightAnimator = ValueAnimator.ofInt(currentViewHeight, toHeight.toInt())
        heightAnimator.addUpdateListener { animation ->
            val indicatorView = indicator.findViewById<View>(R.id.indicator)
            indicatorView.layoutParams.height = animation.animatedValue as Int
            indicatorView.requestLayout()
        }
        heightAnimator.duration = 300

        val margin = resources.getDimension(R.dimen.default_margin)
        val toX = v.x - margin
        val translationAnimator = ObjectAnimator.ofFloat(indicator, "translationX", toX)
        translationAnimator.duration = 400

        animatorSet.playTogether(widthAnimator, heightAnimator, translationAnimator)
        animatorSet.start()
    }
}