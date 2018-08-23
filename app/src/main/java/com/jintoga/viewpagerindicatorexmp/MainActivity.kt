package com.jintoga.viewpagerindicatorexmp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = TestAdapter(supportFragmentManager)

        pager.adapter = adapter
        indicator.setViewPager(pager)
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
