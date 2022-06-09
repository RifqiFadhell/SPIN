package com.spin.id.utils.base

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.spin.id.utils.analytic.AnalyticsTrackerUtil

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provideLayout()
        init(savedInstanceState)
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    protected abstract fun provideLayout()

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