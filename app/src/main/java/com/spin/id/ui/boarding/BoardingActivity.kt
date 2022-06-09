package com.spin.id.ui.boarding

import android.os.Bundle
import android.os.SystemClock
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.spin.id.R
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.login.LoginActivity
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.BOARDING
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_ON_BOARDING_LOGIN_CLICKED
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_ON_BOARDING_REGISTER_CLICKED
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.boarding_activity.*
import kotlinx.android.synthetic.main.toolbar.*

class BoardingActivity : BaseActivity() {

    private var tracker: AmplitudeClient? = null

    companion object {
        const val FROM_PAGE = "FROM_PAGE"
    }

    private var fromPage = ""

    override fun provideLayout() {
        setContentView(R.layout.boarding_activity)
    }

    override fun getScreenName() = BOARDING

    override fun init(bundle: Bundle?) {
        if (TinyDB(this).getBoolean(TinyConstant.TINY_STATUS_LOGOUT)) {
            goToActivity(HomeActivity::class.java)
            TinyDB(this).putBoolean("SPLASH", true)
            finish()
        }
        tracker = Amplitude.getInstance()
        tracker?.logEvent(AnalyticsTrackerConstant.PARAM_ON_BOARDING_LOAD)
        initToolbar()
        viewPagerBoarding?.adapter =
            BoardingPagerAdapter(supportFragmentManager)
        indicatorPagerBoarding?.setViewPager(viewPagerBoarding)
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            fromPage = intent.getStringExtra(FROM_PAGE) ?: ""
        }
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener {
            onBackPressed()
        }
        var mLastClickRegister: Long = 0
        var mLastClickLogin: Long = 0
        buttonRegister.setOnClickListener {
            tracker?.logEvent(PARAM_ON_BOARDING_REGISTER_CLICKED)
            if (SystemClock.elapsedRealtime() - mLastClickRegister < 1000) {
                return@setOnClickListener
            }
            mLastClickRegister = SystemClock.elapsedRealtime()
            val b = Bundle()
            b.putString(FROM_PAGE, fromPage)
            goToActivity(IntroActivity::class.java, b)
        }

        buttonLogin.setOnClickListener {
            tracker?.logEvent(PARAM_ON_BOARDING_LOGIN_CLICKED)
            if (SystemClock.elapsedRealtime() - mLastClickLogin < 1000) {
                return@setOnClickListener
            }
            mLastClickLogin = SystemClock.elapsedRealtime()
            val b = Bundle()
            b.putString(FROM_PAGE, fromPage)
            goToActivity(LoginActivity::class.java, b)
        }
    }

    private fun initToolbar() {
        buttonBack.toVisible()
        textTitleToolbar.toGone()
        imageToolbar.toVisible()
        buttonOption.toGone()
    }
}