package com.spin.id.ui.home.shop.payment

import android.graphics.Color
import android.os.Bundle
import com.spin.id.R
import com.spin.id.ui.order.detail.OrderDetailActivity
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.home_toolbar.*

class PaymentOVOActivity : BaseActivity() {

    companion object {
        const val ORDER_ID = "ORDER_ID"
        const val MESSAGE_TITLE = "MESSAGE_TITLE"
        const val MESSAGE_CONTENT = "MESSAGE_CONTENT"
    }

    private var orderId: String? = ""
    private var messageTitle: String? = ""
    private var messageContent: String? = ""

    override fun provideLayout() {
        setContentView(R.layout.activity_payment)
    }

    override fun init(bundle: Bundle?) {
        initToolbar()
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            messageTitle = intent.getStringExtra(MESSAGE_TITLE)
            messageContent = intent.getStringExtra(MESSAGE_CONTENT)
        }

        textTitlePayment?.text = messageTitle
        textCaptionPayment?.text = messageContent
    }

    override fun initListener(bundle: Bundle?) {
        butttonCheckOrder?.setOnClickListener {
            val b = Bundle()
            b.putString(OrderDetailActivity.ORDER_ID, orderId)
            goToActivity(OrderDetailActivity::class.java, b)
            finish()
        }
        buttonBack?.setOnClickListener {
            val b = Bundle()
            b.putString(OrderDetailActivity.ORDER_ID, orderId)
            goToActivity(OrderDetailActivity::class.java, b)
            finish()
        }
    }

    override fun getScreenName(): String? {
        return null
    }

    private fun initToolbar() {
        imageToolbar?.toGone()
        textTitleToolbar.toVisible()
        buttonBack?.setColorFilter(Color.argb(255, 255, 255, 255))
        textTitleToolbar?.setTextColor(Color.parseColor("#ffffff"))
        textTitleToolbar?.text = getString(R.string.payment_complete_title)
    }

}