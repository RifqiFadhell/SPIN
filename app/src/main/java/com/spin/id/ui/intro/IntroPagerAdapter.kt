package com.spin.id.ui.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.spin.id.ui.intro.form.FormRegisterFragment
import com.spin.id.ui.intro.game.SelectGameFragment
import com.spin.id.ui.intro.topic.SelectTopicFragment

class IntroPagerAdapter(fm: FragmentManager):
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> SelectGameFragment()
            1 -> SelectTopicFragment()
            2 -> FormRegisterFragment()
            else -> SelectGameFragment()
        }
    }
}