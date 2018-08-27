package com.jintoga.sliding

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
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.jintoga.indicator.R

class SlidingTabIndicator @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr),
        PageIndicator,
        ViewPager.OnPageChangeListener {

    private lateinit var backContainer: LinearLayout
    private lateinit var frontContainer: LinearLayout
    private lateinit var indicatorView: IndicatorView

    private var distributeEvenly: Boolean = false

    private var viewPager: ViewPager? = null
    private var currentTabView: View? = null
    private var pageChangeListener: ViewPager.OnPageChangeListener? = null

    private var selectedTabIndex: Int = 0

    private var tabSelector: Runnable? = null

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

        indicatorView = IndicatorView(context)
        holderView.addView(indicatorView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        frontContainer = LinearLayout(context)
        frontContainer.setHorizontalGravity(LinearLayout.HORIZONTAL)
        holderView.addView(frontContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        addView(holderView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (tabSelector != null) {
            // Re-post the selector we saved
            post(tabSelector)
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (tabSelector != null) {
            removeCallbacks(tabSelector)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val indicator = indicatorView.findViewById<View>(R.id.indicator)
        val width = indicator.width
        val height = indicator.height
        if (width == 0 || height == 0) {
            val currentView = this.currentTabView!!
            val params = indicator.layoutParams
            params.width = currentView.width
            params.height = currentView.height
            indicator.layoutParams = params
            val margin = resources.getDimension(R.dimen.default_margin)
            this.indicatorView.x = currentView.x - margin
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (currentTabView == null) {
            currentTabView = frontContainer.getChildAt(selectedTabIndex)
        }
    }

    override fun setViewPager(viewPager: ViewPager) {
        if (this.viewPager === viewPager) {
            return
        }
        this.viewPager?.clearOnPageChangeListeners()
        this.viewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    override fun setViewPager(viewPager: ViewPager, initialPosition: Int) {
        setViewPager(viewPager)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(position: Int) {
        selectedTabIndex = position
        viewPager?.currentItem = position

        val tabCount = frontContainer.childCount
        for (i in 0 until tabCount) {
            val tabView = frontContainer.getChildAt(i)
            val isSelected = i == position
            tabView.isSelected = isSelected
            if (isSelected) {
                scrollToTab(position)
                AnimationHelper.animateIndicator(tabView, currentTabView, indicatorView)
            }
        }
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        pageChangeListener = listener
    }

    override fun notifyDataSetChanged() {
        val adapter = viewPager?.adapter
                ?: throw IllegalStateException("ViewPager does not have adapter instance.")
        initTabItems(adapter)

        val count = adapter.count
        if (selectedTabIndex > count) {
            selectedTabIndex = count - 1
        }
        setCurrentItem(selectedTabIndex)
        requestLayout()
    }

    override fun onPageScrollStateChanged(p0: Int) {
        pageChangeListener?.onPageScrollStateChanged(p0)
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
        pageChangeListener?.onPageScrolled(p0, p1, p2)
    }

    override fun onPageSelected(p0: Int) {
        setCurrentItem(p0)
        pageChangeListener?.onPageSelected(p0)
    }

    private fun initTabItems(pagerAdapter: PagerAdapter) {
        backContainer.removeAllViews()
        frontContainer.removeAllViews()

        val tabClickListener = TabClickListener()
        populateTab(backContainer, pagerAdapter, null,
                R.layout.item_background_view)
        populateTab(frontContainer, pagerAdapter, tabClickListener,
                R.layout.item_foreground_view)
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

            if (distributeEvenly) {
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

    private fun scrollToTab(position: Int) {
        if (tabSelector != null) {
            removeCallbacks(tabSelector)
        }
        val tabView = frontContainer.getChildAt(position)
        tabSelector = Runnable {
            val scrollPos = tabView.left - (width - tabView.width) / 2
            smoothScrollTo(scrollPos, 0)
            tabSelector = null
        }
        post(tabSelector)
    }

    private inner class TabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            for (i in 0 until frontContainer.childCount) {
                if (v == frontContainer.getChildAt(i)) {
                    viewPager?.currentItem = i
                    return
                }
            }
        }
    }
}