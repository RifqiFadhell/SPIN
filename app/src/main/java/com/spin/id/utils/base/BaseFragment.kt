package com.spin.id.utils.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spin.id.utils.analytic.AnalyticsTrackerUtil

abstract class BaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewOut = provideLayout(inflater, container, savedInstanceState)
        return viewOut
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init(savedInstanceState)
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    abstract fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    protected abstract fun init(bundle: Bundle?)

    protected abstract fun initData(bundle: Bundle?)

    protected abstract fun initListener(bundle: Bundle?)

    protected abstract fun getScreenName(): String?

    override fun onResume() {
        super.onResume()
        getScreenName()?.let {
            AnalyticsTrackerUtil.logScreenEvent(it)
        }
    }
}
