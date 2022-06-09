package com.spin.id.ui.home.shop.payment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import com.spin.id.R
import com.spin.id.api.response.shop.buy.BuyData
import com.spin.id.ui.order.detail.OrderDetailActivity
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import kotlinx.android.synthetic.main.activity_payment_va.*
import kotlinx.android.synthetic.main.home_toolbar.*
import java.util.*

class PaymentVAActivity : BaseActivity() {
    companion object {
        const val DATA_PAYMENT = "DATA_PAYMENT"
    }

    private var buyData: BuyData? = null
    private var countDownTimer: CountDownTimer? = null

    override fun provideLayout() {
        setContentView(R.layout.activity_payment_va)
    }

    override fun init(bundle: Bundle?) {
        initToolbar()
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            buyData = intent.getParcelableExtra(DATA_PAYMENT)
        }
        initView()
        initCountdown()
    }

    override fun initListener(bundle: Bundle?) {
        buttonCopyVA?.setOnClickListener {
            textVANumber?.text.toString().copyText(this, "VA Copied")
        }
        buttonBack?.setOnClickListener {
            val b = Bundle()
            b.putString(OrderDetailActivity.ORDER_ID, buyData?.orderId)
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

    @SuppressLint("SetTextI18n")
    private fun initView() {
        buyData?.let {
            textTimePayment?.text =
                "${
                    it.expirationDate?.convertDate(
                        "yyyy-MM-dd HH:mm:ss",
                        "EEEE, dd MMMM yyyy HH:mm"
                    )
                } ${it.timezone}"
            imagePaymentLogo.loadImage(this, it.paymentLogo, R.drawable.ic_place_holder)
            textPaymentMethod?.text = getString(R.string.payment_method_va, it.channelCode)
            textVANumber?.text = it.accountNumber
            textPaymentTotal?.text = it.amount?.rupiahFormat()
        }
    }

    private fun initCountdown() {
        buyData?.let {
            val date = it.expirationDate?.toDate("yyyy-MM-dd HH:mm:ss")
            val currentDate = Date()
            date?.let {
                val sisaCounter = date.time - currentDate.time
                if (sisaCounter > 0) {
                    startCountDownTimer(sisaCounter)
                } else {
                    textCountdownPayment.text = getString(R.string.payment_countdown_finish)
                }
            }
        }
    }

    private fun startCountDownTimer(limit: Long) {
        countDownTimer = object : CountDownTimer(limit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hour = (millisUntilFinished / 1000 / 3600).toInt()
                val minute = ((millisUntilFinished / 1000) % 3600 / 60).toInt()
                val second = (millisUntilFinished / 1000) % 60

                val lastSentence = if (hour > 0) {
                    "${hour} Jam ${minute} menit"
                } else {
                    "${minute} menit"
                }

                textCountdownPayment.text = getString(R.string.payment_countdown, lastSentence)
            }

            override fun onFinish() {
                textCountdownPayment.text = getString(R.string.payment_countdown_finish)
            }
        }.start()
    }
}

