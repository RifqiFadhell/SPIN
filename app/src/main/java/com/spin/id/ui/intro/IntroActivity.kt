package com.spin.id.ui.intro

import android.os.Bundle
import android.util.Log
import com.amplitude.api.Amplitude
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spin.id.R
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.home.feed.FeedPresenter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.INTRO
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import kotlinx.android.synthetic.main.intro_activity.*
import kotlinx.android.synthetic.main.toolbar.*

class IntroActivity : BaseActivity() {

    private var tinyDb: TinyDB? = null
    var fromPage = ""

    var type = ""
    var isLogin: Boolean? = null
    var email = ""
    var firstName = ""
    var lastName = ""
    var idToken = ""

    override fun provideLayout() {
        setContentView(R.layout.intro_activity)
    }

    override fun getScreenName() = INTRO

    override fun init(bundle: Bundle?) {
        tinyDb = TinyDB(this)
        indicatorPagerIntro?.setViewPager(viewPagerIntro)
        textTitleToolbar.text = getString(R.string.game_title_toolbar)
    }

    override fun initData(bundle: Bundle?) {
        getIntentData()
        isLogin = tinyDb?.getBoolean(TinyConstant.TINY_STATUS_LOGIN)
        Log.e("FROM_PAGE_INTRO", "dari: " + fromPage)
        if (fromPage.equals("DetailProduct")) {
            viewPagerIntro?.adapter = IntroPagerAdapter(supportFragmentManager)
            setCurrentItem(2)
        } else {
            if (type.equals("set-preference") && isLogin == true) {
                viewPagerIntro?.adapter = UpdateFeedPagerAdapter(supportFragmentManager)
                setCurrentItem(0)
            } else {
                viewPagerIntro?.adapter = IntroPagerAdapter(supportFragmentManager)
                setCurrentItem(0)
            }
        }
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener { onBackPressed() }
        buttonSkip.setOnClickListener {
            Firebase.auth.signOut()
            this.signOutGoogle()
            this.signOutFacebook()
            FeedPresenter.onRefresh = true
            goToActivity(HomeActivity::class.java)
            TinyDB(this).putBoolean("SPLASH", true)
            finishAffinity()
            Amplitude.getInstance().logEvent(AnalyticsTrackerConstant.PARAM_SKIP_REGISTRATION)
        }
    }

    fun setCurrentItem(position: Int) {
        when (position) {
            0 -> {
                buttonSkip.toGone()
                textTitleToolbar.text = getString(R.string.game_title_toolbar)
            }
            1 -> {
                buttonSkip.toGone()
                textTitleToolbar.text = getString(R.string.topic_title_toolbar)
            }
            2 -> {
                if (idToken.isNotEmpty()) {
                    buttonSkip.toGone()
                } else {
                    buttonSkip.toVisible()
                }
                textTitleToolbar.text = getString(R.string.register_title_toolbar)
            }
        }
        viewPagerIntro?.setCurrentItem(position, true)
    }

    fun hideButtonSkip() {
        buttonSkip.toGone()
    }

    override fun onBackPressed() {
        val current = viewPagerIntro.currentItem
        if (fromPage.equals("DetailProduct")) {
            super.onBackPressed()
        } else {
            if (current != 0) {
                setCurrentItem(current - 1)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun getIntentData() {
        if (intent != null) {
            fromPage = intent.getStringExtra(BoardingActivity.FROM_PAGE) ?: ""
            type = intent.getStringExtra("type") ?: ""
            email = intent.getStringExtra("email") ?: ""
            firstName = intent.getStringExtra("firstName") ?: ""
            lastName = intent.getStringExtra("lastName") ?: ""
            idToken = intent.getStringExtra("idToken") ?: ""
        }
    }
}