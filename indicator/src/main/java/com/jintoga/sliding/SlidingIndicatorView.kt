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
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.jintoga.indicator.R

class SlidingIndicatorView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr),
        PageIndicator,
        ViewPager.OnPageChangeListener {

    private lateinit var behindContainer: LinearLayout
    private lateinit var frontContainer: LinearLayout
    private lateinit var indicatorView: IndicatorView

    private var distributeEvenly: Boolean = false

    private var viewPager: ViewPager? = null
    private var currentTabView: View? = null
    private var pageChangeListener: ViewPager.OnPageChangeListener? = null

    private var selectedTabIndex: Int = 0

    private var tabSelector: Runnable? = null

    private var behindLayout = -1
    private var frontLayout = -1
    private var indicatorLayout = -1
    private var indicatorResizeDuration: Long = AnimationHelper.DEFAULT_RESIZE_ANIMATION_DURATION
    private var indicatorTranslateDuration: Long = AnimationHelper.DEFAULT_TRANSLATION_ANIMATION_DURATION

    init {
        init(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingIndicatorView, 0, 0)
        behindLayout = typedArray.getResourceId(R.styleable.SlidingIndicatorView_behindLayout, -1)
        frontLayout = typedArray.getResourceId(R.styleable.SlidingIndicatorView_frontLayout, -1)
        indicatorLayout = typedArray.getResourceId(R.styleable.SlidingIndicatorView_indicatorLayout, -1)
        val indicatorResizeDuration = typedArray.getInteger(R.styleable.SlidingIndicatorView_indicatorResizeDuration, -1)
        val indicatorTranslateDuration = typedArray.getInteger(R.styleable.SlidingIndicatorView_indicatorTranslateDuration, -1)
        typedArray.recycle()

        if (behindLayout == -1 || frontLayout == -1 || indicatorLayout == -1) {
            throw IllegalAccessException("Behind, Front And Indicator background are required")
        }

        if (indicatorResizeDuration != -1) {
            this.indicatorResizeDuration = indicatorResizeDuration.toLong()
        }

        if (indicatorTranslateDuration != -1) {
            this.indicatorTranslateDuration = indicatorTranslateDuration.toLong()
        }

        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        // Make sure that the Tab Strips fills this View
        isFillViewport = true

        val holderView = FrameLayout(context)
        behindContainer = LinearLayout(context)
        behindContainer.setHorizontalGravity(LinearLayout.HORIZONTAL)
        holderView.addView(behindContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        indicatorView = IndicatorView(indicatorLayout, context)
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
        val indicator = indicatorView.getIndicator()
        val width = indicator.width
        val height = indicator.height
        if (width == 0 || height == 0) {
            val currentView = this.currentTabView!!
            val params = indicator.layoutParams
            params.width = currentView.width
            params.height = currentView.height
            indicator.layoutParams = params
            val margin = (currentView.layoutParams as MarginLayoutParams).leftMargin
            this.indicatorView.x = currentView.x - margin
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (currentTabView == null) {
            currentTabView = frontContainer.getChildAt(selectedTabIndex)
        }
    }


    override fun setViewPager(viewPager: ViewPager, initialPosition: Int?) {
        if (this.viewPager === viewPager) {
            return
        }
        this.viewPager?.clearOnPageChangeListeners()
        this.viewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        notifyDataSetChanged()
        if (initialPosition != null) {
            setCurrentItem(initialPosition)
        }
    }

    override fun setCustomData(data: List<SlidingViewItem>, initialPosition: Int?) {
        notifyDataSetChanged()
        if (initialPosition != null) {
            setCurrentItem(initialPosition)
        }
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
                AnimationHelper
                        .getInstance(indicatorResizeDuration, indicatorTranslateDuration)
                        .animateIndicator(tabView, currentTabView, indicatorView)
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

    private fun initTabItems(pagerAdapter: PagerAdapter) {
        behindContainer.removeAllViews()
        frontContainer.removeAllViews()

        val tabClickListener = TabClickListener()
        populateTab(pagerAdapter, tabClickListener)
    }

    private fun populateTab(adapter: PagerAdapter,
                            listener: OnClickListener?) {
        for (i in 0 until adapter.count) {
            val behindRootView = LayoutInflater.from(context).inflate(behindLayout, behindContainer, false)
            val frontRootView = LayoutInflater.from(context).inflate(frontLayout, frontContainer, false)
            behindContainer.addView(behindRootView)
            frontContainer.addView(frontRootView)
            val title = adapter.getPageTitle(i)
            bindData(behindRootView, title.toString(), null)
            bindData(frontRootView, title.toString(), listener)
        }
    }

    private fun bindData(rootView: View, title: String, listener: OnClickListener?) {
        val container = rootView.findViewWithTag<View>("container")
        val textView = rootView.findViewWithTag<TextView>("textView")

        val params = rootView.layoutParams as LinearLayout.LayoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        if (distributeEvenly) {
            params.width = 0
            params.weight = 1f
        }

        textView.text = title

        if (listener != null) {
            container.setOnClickListener(listener)
        }
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