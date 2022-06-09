package com.spin.id.ui.forgotpassword

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.api.response.validation.CheckUserResponse
import com.spin.id.ui.otp.OtpActivity
import com.spin.id.utils.RegexFilter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.FORGOT_PASSWORD
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.hideKeyboard
import com.spin.id.utils.extensions.showLongToast
import com.spin.id.utils.extensions.toDisable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.forgot_password_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : BaseActivity(), ForgotPasswordContract.ForgotPasswordView {

    private lateinit var presenter: ForgotPasswordPresenter
    private var userName = ""
    private var isValid = false

    override fun provideLayout() {
        setContentView(R.layout.forgot_password_activity)
    }

    override fun getScreenName() = FORGOT_PASSWORD

    override fun init(bundle: Bundle?) {
        textTitleToolbar?.text = getString(R.string.forgot_password_title_toolbar)
        buttonKirimForgotPassword.toDisable()
        presenter = ForgotPasswordPresenter()
    }

    override fun initData(bundle: Bundle?) {

    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener { finish() }
        layoutForgotPassword?.setOnClickListener { hideKeyboard() }
        editEmail.afterTextChangeEvents()
            .skip(1)
            .debounce(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                validateUsernameNumber(it.editable.toString())
            }
        buttonKirimForgotPassword?.setOnClickListener {
            /*ADD API*/
            if (editEmail?.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra("mobile", editEmail?.text.toString())
            intent.putExtra("status", "forgot")
            startActivity(intent)
            finish()
        }
    }

    private fun validateUsernameNumber(value: String) {
        val request = CheckUserRequest()
        when {
            value.isEmpty() -> {
                textInputEmail.error = getString(R.string.register_username_empty_error)
                showButton(false)
            }
            else -> {
                if (RegexFilter.isNumberValid(value)) {
                    request.mobile = value
                } else {
                    request.username = value
                }
                presenter.checkValidation(this, request)
                userName = value
            }
        }
    }

    override fun getData(response: CheckUserResponse) {
        if (response.status == 401) {
            isValid = true
            textInputEmail.error = null
            editEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_verified),
                null
            )
            showButton(true)
        } else {
            isValid = false
            textInputEmail.error = getString(R.string.login_username_not_found)
        }
    }

    override fun showError(throwable: Throwable) {
        this.showLongToast(throwable.message.toString())
    }

    private fun showButton(isFilled: Boolean){
        if (isFilled && isValid) {
            buttonKirimForgotPassword?.isEnabled = true
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                buttonKirimForgotPassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button, theme))
            } else {
                buttonKirimForgotPassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button))
            }
        } else {
            buttonKirimForgotPassword?.isEnabled = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                buttonKirimForgotPassword?.backgroundTintList =
                    ColorStateList.valueOf(
                        resources.getColor(
                            R.color.color_button_inactive,
                            theme
                        )
                    )
            } else {
                buttonKirimForgotPassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button_inactive))
            }
        }

    }
}