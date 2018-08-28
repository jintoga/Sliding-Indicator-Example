package com.jintoga.slidingindicatorview

import android.view.View

interface DataBinder {
    /**
     *
     */
    fun onBindTabData(rootView: View, position: Int)

    /**
     * @return number of tabs to be created
     */
    fun onTabCount(): Int
}