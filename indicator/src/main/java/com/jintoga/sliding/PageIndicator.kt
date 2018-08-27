package com.jintoga.sliding

import android.support.v4.view.ViewPager

interface PageIndicator : ViewPager.OnPageChangeListener {
    /**
     * Bind the indicator to a ViewPager.
     *
     * @param view
     * @param initialPosition
     */
    fun setViewPager(viewPager: ViewPager, initialPosition: Int? = null)

    /**
     * Using the indicator as a "standalone" View.
     *
     * @param view
     * @param initialPosition
     */
    fun setCustomData(data: List<SlidingViewItem>, initialPosition: Int? = null)

    /**
     *
     * Set the current page of both the ViewPager and indicator.
     *
     *
     * This **must** be used if you need to set the page before
     * the views are drawn on screen (e.g., default start page).
     *
     * @param position
     */
    fun setCurrentItem(position: Int)

    /**
     * Set a page change listener which will receive forwarded events.
     *
     * @param listener
     */
    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener)

    /**
     * Notify the indicator that the fragment list has changed.
     */
    fun notifyDataSetChanged()
}