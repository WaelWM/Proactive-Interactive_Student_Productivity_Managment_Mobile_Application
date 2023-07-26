package fyp.wael.proactive.userInterface.fragment

// Programmer Name: Wael Mohammed Abdullah Al-Harazi, Software Engineering Student
// Program Name     : CombinedFragment.kt
// Description      : To combine both events and ProactiveCentralFragment
// First Written on : Monday, 22-May-2023
// Edited on        : Thursday, 6-Jul-2023

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fyp.wael.proactive.R
import fyp.wael.proactive.adapters.CombinedFragmentAdapter


class CombinedFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_combined, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        setupViewPager()
        return view
    }

    private fun setupViewPager() {
        val adapter = CombinedFragmentAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Events"
                1 -> "Knowledge Base"
                else -> null
            }
        }.attach()
    }
}




