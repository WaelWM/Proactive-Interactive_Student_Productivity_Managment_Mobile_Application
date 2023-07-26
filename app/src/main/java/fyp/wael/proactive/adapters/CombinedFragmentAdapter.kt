package fyp.wael.proactive.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import fyp.wael.proactive.userInterface.fragment.EventsFragment
import fyp.wael.proactive.userInterface.fragment.ProactiveCentralFragment

class CombinedFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EventsFragment()
            1 -> ProactiveCentralFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}