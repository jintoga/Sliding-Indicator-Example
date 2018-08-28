package com.jintoga.viewpagerindicatorexmp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.jintoga.slidingindicatorview.DataBinder
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), DataBinder {

    private lateinit var adapter: TestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = TestAdapter(supportFragmentManager)

        pager.adapter = adapter
        indicator.setViewPager(
                viewPager = pager,
                dataBinder = this,
                initialPosition = 4)
    }

    override fun onTabCount(): Int = CONTENT.size

    override fun onBindTabData(rootView: View, position: Int) {
        val textView = rootView.findViewWithTag<TextView>("textView")
        textView.text = adapter.getPageTitle(position)
    }

    private val CONTENT = arrayOf("Recent", "Artists", "Albums", "Songs"
            , "Playlists", "2141 as", "15125", "Play125125aists", "GeAAnres")

    internal inner class TestAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return TestFragment.newInstance(CONTENT[position % CONTENT.size])
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return CONTENT[position % CONTENT.size].toUpperCase()
        }

        override fun getCount(): Int {
            return CONTENT.size
        }
    }
}
