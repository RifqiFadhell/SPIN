package com.spin.id.ui.home.feed.banner

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.spin.id.api.response.banner.BannerData
import com.spin.id.api.response.feed.list.FeedData

class BannerPagerAdapter(
    fm: FragmentManager,
    private val banners: List<BannerData>,
    private val onItemClick: ((Int, BannerData) -> Unit)? = null
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return BannerFragment.newInstance(banners[position].thumbnail ?: "")
    }

    override fun getCount(): Int {
        return banners.size
    }

}