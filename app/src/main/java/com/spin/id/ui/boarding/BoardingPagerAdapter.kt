package com.spin.id.ui.boarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.spin.id.R
import com.spin.id.ui.boarding.BoardingSwipeFragment

class BoardingPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> BoardingSwipeFragment.newInstance(R.drawable.ic_intro_1, "Unlock Like & Comment", "Register atau Login App supaya kalian bisa ikutan ngelike like dan comment di postingan.")
            1 -> BoardingSwipeFragment.newInstance(R.drawable.ic_intro_2, "Simpan Preferensi", "Register atau Login untuk menyimpan preferensi kamu, biar feed kamu selalu relevan!")
            else -> BoardingSwipeFragment.newInstance(R.drawable.ic_intro_3, "Ikutan Ekslusif Event", "Kita akan sering ngadain event ekslusif untuk member di App kita. Jadi yuk segera register atau login biar ga ketinggalan.")
        }
    }

}