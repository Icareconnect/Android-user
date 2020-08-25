package com.consultantapp.ui.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class CommonFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    val fragments = mutableListOf<Fragment>()
    private val titles = mutableListOf<String>()


    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    fun addTab(title: String, fragment: Fragment): CommonFragmentPagerAdapter {
        fragments.add(fragment)
        titles.add(title)

        return this
    }

    fun clear() {
        fragments.clear()
        titles.clear()
    }

    override fun saveState(): Parcelable? {
        return null
    }
}
