package com.spin.id.ui.otp

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.otp.OtpRequest
import com.spin.id.api.request.otp.OtpVerifyRequest
import com.spin.id.api.request.register.RegisterRequest
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.otp.OtpRequestResponse
import com.spin.id.api.response.otp.OtpSuccessResponse
import com.spin.id.api.response.register.RegisterResponse
import com.spin.id.api.response.topic.TopicData
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PASSWORD
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_USERNAME
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.forgotpassword.NewPasswordActivity
import com.spin.id.ui.home.HomeActivity
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.PermissionUtils
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_REQUEST_OTP
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SUCCESS_OTP
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.otp_activity.*
import kotlinx.android.synthetic.main.otp_activity.backgroundButton
import kotlinx.android.synthetic.main.success_registered_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONException
import org.json.JSONObject

class OtpActivity : BaseActivity(), OtpContract.View {

    private var presenter: OtpPresenter? = null
    private var mobile: String? = ""
    private var id: String? = ""
    private var status: String? = "normal"
    private var from: String? = ""
    private var codeOtp: String? = ""
    private var transactionId: String? = ""
    private var firstNumberOtp: String? = ""
    private var counter: Int? = 0

    private var progress: LoaderIndicatorHelper? = null
    private var countDownTimer: CountDownTimer? = null
    private var tinyDB: TinyDB? = null
    private var tracker: AmplitudeClient? = null

    //BUNDLE REGISTER
    private var userName: String? = ""
    private var firstName: String? = ""
    private var lastName: String? = ""
    private var email: String? = ""
    private var passwords: String? = ""

    override fun provideLayout() {
        setContentView(R.layout.otp_activity)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.OTP

    override fun init(bundle: Bundle?) {
        tinyDB = TinyDB(this)
        hideCountDownTimerUI()
        hideResendButton()
        tracker = Amplitude.getInstance()
        if (intent != null) {
            mobile = intent.getStringExtra("mobile")
            status = intent.getStringExtra("status")
            userName = intent.getStringExtra("username")
            firstName = intent.getStringExtra("firstname")
            lastName = intent.getStringExtra("lastname")
            email = intent.getStringExtra("email")
            passwords = intent.getStringExtra("password")
            id = intent.getStringExtra("id")
        }
        buttonSubmit.isEnabled = false
        initToolbar()
        setAllCaption()
        progress = LoaderIndicatorHelper()
    }

    override fun initData(bundle: Bundle?) {
        presenter = OtpPresenter()
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener { onBackPressed() }

        buttonSubmit.setSingleClickListener { checkPermission() }

        layout.setOnClickListener { hideKeyboard() }

        editNumberOtpSecondLogin.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (editNumberOtpSecondLogin.text?.length == 0) {
                    editNumberOtpFirstLogin.isEnabled = true
                    editNumberOtpFirstLogin.setText("")
                    focusOnFirstDigit()
                    editNumberOtpSecondLogin.isEnabled = false
                }
            }
            false
        }

        editNumberOtpThirdLogin.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (editNumberOtpThirdLogin.text?.length == 0) {
                    editNumberOtpThirdLogin.setText("")
                    editNumberOtpSecondLogin.setText("")
                    focusOnSecondDigit()
                    editNumberOtpThirdLogin.isEnabled = false
                }
            }
            false
        }

        editNumberOtpFourthLogin.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (editNumberOtpFourthLogin.text?.length == 0) {
                    editNumberOtpThirdLogin.setText("")
                    focusOnThirdDigit()
                    editNumberOtpFourthLogin.isEnabled = false
                }
            }
            false
        }

        editNumberOtpFirstLogin.afterTextChangeEvents()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.editable.toString() != "") {
                    focusOnSecondDigit()
                    editNumberOtpFirstLogin.isEnabled = false
                }
            }

        editNumberOtpSecondLogin.afterTextChangeEvents()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.editable.toString() != "") {
                    focusOnThirdDigit()
                    editNumberOtpSecondLogin.isEnabled = false
                }
            }


        editNumberOtpThirdLogin.afterTextChangeEvents()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.editable.toString() != "") {
                    focusOnFourthDigit()
                    editNumberOtpThirdLogin.isEnabled = false
                }
            }

        editNumberOtpFourthLogin.afterTextChangeEvents()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                buttonVerify.toEnable()
            }

        editNumber.afterTextChangeEvents()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                checkNumber(it.editable.toString())
            }

        buttonResend.setOnClickListener {
            runCountDownTimer(0)
            hideResendButton()
            requestOtp()
        }

        buttonLogin.setOnClickListener {
            goToActivity(HomeActivity::class.java)
            tinyDB?.putBoolean("SPLASH", true)
            finishAffinity()
            val eventProperties = JSONObject()
            try {
                eventProperties.put("OTP Request Method", "Misscall")
                eventProperties.put("OTP Vendor", "Citcall")
                eventProperties.put("OTP Request Attempt", counter)
            } catch (exception: JSONException) {

            }
            tracker?.logEvent(PARAM_SUCCESS_OTP, eventProperties)
        }

        buttonVerify.setOnClickListener { verifyOtp() }
    }

    private fun initToolbar() {
        textTitleToolbar.text = getString(R.string.otp_toolbar_title)
        textInformationCall.toGone()
    }

    private fun checkNumber(number: String) {
        if (number.isNotEmpty()) {
            buttonSubmit.isEnabled = true
            mobile = number
        }
    }

    private fun runCountDownTimer(limit: Long) {
        var limiter: Long = 60000
        if (limit != 0.toLong()) limiter = limit
        showCountDownTimerUI()
        countDownTimer = object : CountDownTimer(limiter, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second = (millisUntilFinished / 1000).toString()
                if (second == "60") {
                    updateCountDownTimer("60")
                } else {
                    val secondFormat: String = if (second.length == 2) {
                        second
                    } else {
                        "0$second"
                    }
                    updateCountDownTimer(secondFormat)
                }
            }

            override fun onFinish() {
                hideCountDownTimerUI()
                showResendButton()
            }
        }.start()
    }

    private fun setAllCaption() {
        groupCodeOtp.toGone()
        when (status) {
            "mobile" -> {
                buttonSubmit.toDisable()
                textCaption1.text = getString(R.string.otp_caption_add_number)
                textInputNumber.toVisible()
                textCaption2.text = getString(R.string.otp_caption_2)
            }
            "forgot" -> {
                textCaption1.text = getString(R.string.otp_caption_1, mobile)
                textCaption2.text = getString(R.string.otp_caption_2)
                textInputNumber.toGone()
                groupCodeOtp.toGone()
                textResend.toGone()
                buttonSubmit.isEnabled = true
            }
            else -> {
                buttonSubmit.toEnable()
                textCaption1.text = getString(R.string.otp_caption_1, mobile)
                textInputNumber.toGone()
                textCaption2.text = getString(R.string.otp_caption_2)
            }
        }
    }

    private fun requestOtp() {
        val request = OtpRequest()
        if (status == "forgot" || status == "register") {
            request.method = "misscall"
            request.msisdn = mobile
        } else {
            request.method = "misscall"
            request.msisdn = mobile
            request.user_id = id?.toInt()
            request.retry = 0
        }

        val eventProperties = JSONObject()
        try {
            eventProperties.put("OTP Request Method", "Misscall")
            eventProperties.put("OTP Vendor", "Citcall")
        } catch (exception: JSONException) {

        }
        tracker?.logEvent(PARAM_REQUEST_OTP, eventProperties)
        presenter?.requestOtp(this, request)
        from = "request"
    }

    private fun verifyOtp() {
        val request = OtpVerifyRequest()
        val first = editNumberOtpFirstLogin.editableText.toString()
        val second = editNumberOtpSecondLogin.editableText.toString()
        val third = editNumberOtpThirdLogin.editableText.toString()
        val fourth = editNumberOtpFourthLogin.editableText.toString()
        codeOtp = "$first$second$third$fourth"
        request.code = firstNumberOtp + codeOtp
        request.method = "misscall"
        request.trxid = transactionId

        presenter?.verifyOtp(this, request)
        from = "verify"
        buttonVerify.toDisable()
    }

    private fun register() {
        val request = RegisterRequest()
        request.username = userName
        request.first_name = firstName
        request.last_name = lastName
        request.email = email
        request.password = passwords
        request.mobile = mobile
        request.games = getSelectedIdGames()
        request.topics = getSelectedIdTopic()

        presenter?.register(this, request)
    }

    private fun verifyLogin() {
        val userName = tinyDB?.getString(TINY_USERNAME)
        val password = tinyDB?.getString(TINY_PASSWORD)

        val request = LoginRequest()
        request.password = password
        request.username = userName

        presenter?.login(this, request)
        from = "login"
    }

    override fun getDataRequest(result: OtpRequestResponse) {
        if (result.status == 200) {
            buttonVerify.toDisable()
            showInputOtp(result.data?.firstToken.toString())
            transactionId = result.data?.transactionId.toString()
            firstNumberOtp = result.data?.firstToken

            buttonSubmit.toGone()
            buttonVerify.toVisible()
            runCountDownTimer(0)
            counter?.plus(1)
            textInformationCall.toVisible()
            textInformationCall.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            textInformationCall.setTextColor(ContextCompat.getColor(this, R.color.silverChalice))
        } else if (result.status == 406) {
            textInformationCall.toVisible()
            backgroundButton.toGone()
            textInformationCall.setBackgroundColor(ContextCompat.getColor(this, R.color.coralRed))
            textInformationCall.setTextColor(ContextCompat.getColor(this, R.color.white))
            textInformationCall.text = getString(R.string.otp_caption_limit_resend)
            result.data?.otpTimeout?.toLong()?.let { runCountDownTimer(it) }
        } else {
            showErrorDialog()
        }
    }

    override fun getDataVerify(result: OtpSuccessResponse) {
        if (result.status == 200) {
            if (status == "forgot") {
                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("mobile", mobile)
                startActivity(intent)
                finish()
            } else if (status == "register") {
                register()
            } else {
                verifyLogin()
            }
        } else if (result.status == 405) {
            showOkDialog(result.message.toString(), "Ok", null)
        } else if (result.status == 403) {
            showOkDialog(result.message.toString(), "Ok", null)
        } else {
            showErrorDialog()
        }
    }

    override fun getDataLogin(result: LoginResponse) {
        if (result.status == 200) {
            tinyDB?.putBoolean(TINY_STATUS_LOGIN, true)
            tinyDB?.putObject(TINY_PROFILE, result.data)
            tinyDB?.putString(TinyConstant.TINY_TOKEN, "Bearer " + result.data?.token.toString())
            hideProgress()
            successRegisteredLayout.toVisible()
            backgroundButton.toGone()
        } else {
            showOkDialog(result.message.toString(), "Ok", DialogInterface.OnClickListener { _, _ ->
                goToActivity(BoardingActivity::class.java)
                finish()
            })
        }
    }

    override fun getDataRegister(result: RegisterResponse) {
        showLongToast(result.message.toString())
        when (result.code) {
            200 -> {
                verifyLogin()
            }
            411 -> {
                showOkDialog(
                    getString(R.string.register_number_email_was_available_error),
                    "Ok",
                    null
                )
            }
            410 -> {
                showOkDialog(getString(R.string.register_username_was_available_error), "Ok", null)
            }
            else -> showOkDialog(result.message.toString(), "Ok", null)
        }
    }

    override fun showError(throwable: Throwable) {
        progress?.dismissDialog()
        resetOtp()
        this.showOkDialog(throwable.message.toString(), "Oke", null)
        hideCountDownTimerUI()
        showResendButton()
    }

    override fun showErrorRegister(throwable: Throwable) {
        progress?.dismissDialog()
        resetOtp()
        this.showOkDialog(throwable.message.toString(), "Oke", DialogInterface.OnClickListener { dialog, which ->
            finishAffinity()
        })
        hideCountDownTimerUI()
        showResendButton()
    }

    private fun checkPermission() {
        if (!PermissionUtils.isPermissionGranted(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) && !PermissionUtils.isPermissionGranted(
                this,
                Manifest.permission.CALL_PHONE
            ) && !PermissionUtils.isPermissionGranted(
                this,
                Manifest.permission.READ_CALL_LOG
            )
        ) {
            PermissionUtils.requestPermission(
                this, arrayOf(
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.CALL_PHONE
                ), 120
            )
        } else {
            requestOtp()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 120) {
            requestOtp()
        }
    }

    private val fourDigitsReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Extract your data - better to use constants...
            val getLastFour = intent.getStringExtra("last_four_digit")
            if (getLastFour != null && getLastFour.isNotEmpty() && getLastFour != "null") {
                fillThePinPlaceholder(getLastFour)
            }
        }
    }

    private fun fillThePinPlaceholder(pin: String?) {
        editNumberOtpFirstLogin.setText(pin?.substring(0, 1))
        editNumberOtpSecondLogin.setText(pin?.substring(1, 2))
        editNumberOtpThirdLogin.setText(pin?.substring(2, 3))
        editNumberOtpFourthLogin.setText(pin?.substring(3, 4))
        focusOnFourthDigit()
    }

    private val verifyCommandReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra("verify")) {
                val commandVerify = intent.getStringExtra("verify")
                if (commandVerify == "yes") {
                    showShortToast("incoming call")
                    buttonVerify.toEnable()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            fourDigitsReceiver?.let { unregisterReceiver(it) }
            verifyCommandReceiver?.let { unregisterReceiver(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.last_four_digit")
        filter.addAction("android.intent.action.verify")
        registerReceiver(fourDigitsReceiver, filter)
        registerReceiver(verifyCommandReceiver, filter)
    }

    private fun resetOtp() {
        editNumberOtpFirstLogin.setText("")
        editNumberOtpSecondLogin.setText("")
        editNumberOtpThirdLogin.setText("")
        editNumberOtpFourthLogin.setText("")
    }

    /*private fun showEmailOtp() {
        imageInformation?.setImageResource(R.drawable.ic_forgot_password)
        textCaption1?.text = getString(R.string.otp_caption_email, "dailyspin@game.id")
    }*/

    private fun updateCountDownTimer(second: String?) {
        textResend.text = getString(R.string.otp_caption_resend, second)
    }

    private fun showResendButton() {
        buttonResend.toVisible()
    }

    private fun hideResendButton() {
        buttonResend.toGone()
    }

    private fun showCountDownTimerUI() {
        textResend.toVisible()
    }

    private fun hideCountDownTimerUI() {
        textResend.toGone()
    }

    private fun focusOnFirstDigit() {
        editNumberOtpFirstLogin.isEnabled = true
        editNumberOtpFirstLogin.requestFocus()
    }

    private fun focusOnSecondDigit() {
        editNumberOtpSecondLogin.isEnabled = true
        editNumberOtpSecondLogin.requestFocus()
    }

    private fun focusOnThirdDigit() {
        editNumberOtpThirdLogin.isEnabled = true
        editNumberOtpThirdLogin.requestFocus()
    }

    private fun focusOnFourthDigit() {
        editNumberOtpFourthLogin.isEnabled = true
        editNumberOtpFourthLogin.requestFocus()
    }

    private fun showInputOtp(firstNumber: String) {
        textNumber.text = firstNumber
        textNumber.toVisible()
        textInputNumber.toGone()
        groupCodeOtp.toVisible()
        textCaption1.toGone()
        textCaption2.toGone()
    }

    override fun showProgress() {
        progress?.showDialog(this)
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    private fun getSelectedIdGames(): ArrayList<String> {
        val idListGame = ArrayList<String>()
        val itemList = ArrayList<GameData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(this).getListObject(
                TinyConstant.TINY_LIST_GAME, GameData::class.java
            ) as ArrayList<GameData>
        )
        Log.e("DATA GAMES SELECTED 2", Gson().toJson(itemList))
        for (item in itemList) {
            if (item.isSelected) {
                idListGame.add(item.id)
            }
        }
        return idListGame
    }

    private fun getSelectedIdTopic(): ArrayList<String> {
        val idListTopic = ArrayList<String>()
        val itemList = ArrayList<TopicData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(this).getListObject(
                TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
            ) as ArrayList<TopicData>
        )
        for (item in itemList) {
            if (item.isSelected) {
                idListTopic.add(item.id)
            }
        }
        return idListTopic
    }
}