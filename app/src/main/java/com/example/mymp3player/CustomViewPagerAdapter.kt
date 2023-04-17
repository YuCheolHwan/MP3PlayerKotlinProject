package com.example.mymp3player

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CustomViewPagerAdapter(activity : FragmentActivity) : FragmentStateAdapter(activity){
    val fragmentList = ArrayList<Fragment>()
    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList.get(position)

    fun addListFragment(fragment : Fragment){
        this.fragmentList.add(fragment)
    }

}