package com.spin.id.ui.home.profile.referral

import android.os.Bundle
import com.spin.id.R
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.android.synthetic.main.referral_activity.*

class ReferralActivity : BaseActivity() {

    private var code: Int? = 0

    override fun provideLayout() {
        setContentView(R.layout.referral_activity)
    }

    override fun init(bundle: Bundle?) {

    }

    override fun initData(bundle: Bundle?) {

    }

    override fun initListener(bundle: Bundle?) {
        buttonInvite.setOnClickListener {
            layoutReferralCode.toVisible()
            code = 1
        }

        buttonAdd.setOnClickListener {
            layoutReferralAdd.toVisible()
            code = 2
        }
    }

    override fun getScreenName(): String? {
        return "referral_screen"
    }

    override fun onBackPressed() {
        when (code) {
            1 -> {
                layoutReferralCode.toGone()
                code = 0
            }
            2 -> {
                layoutReferralAdd.toGone()
                code = 0
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}