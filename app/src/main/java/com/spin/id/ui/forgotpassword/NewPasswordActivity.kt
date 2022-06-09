package com.spin.id.ui.forgotpassword

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.resetPassword.ResetRequest
import com.spin.id.api.response.resetPassword.ResetResponse
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.new_password_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.concurrent.TimeUnit

class NewPasswordActivity : BaseActivity(), ForgotPasswordContract.NewPasswordView {

    private lateinit var presenter: ForgotPasswordPresenter

    private var mobile: String? = ""
    private var upper = false
    private var sensitive = false
    private var min = false

    private var progress: LoaderIndicatorHelper? = null

    override fun provideLayout() {
        setContentView(R.layout.new_password_activity)
    }

    override fun getScreenName() = "new_password_page"

    override fun init(bundle: Bundle?) {
        presenter = ForgotPasswordPresenter()
        progress = LoaderIndicatorHelper()
        textTitleToolbar?.text = getString(R.string.forgot_password_title_toolbar)
        buttonChangePassword.toDisable()
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            mobile = intent.getStringExtra("mobile")
        }
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener { finish() }
        editNewPassword?.setOnFocusChangeListener { _, _ ->
            showPasswordDialog()
        }
        editConfirmNewPassword?.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
        }
        editNewPassword.afterTextChangeEvents()
            .skip(1)
            .debounce(700, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                validatePassword(it.editable.toString())
            }
        editConfirmNewPassword?.addTextChangedListener {
            validateButton()
        }
        buttonChangePassword?.setOnClickListener {
            if (editNewPassword?.text.toString().isEmpty() &&
                editConfirmNewPassword?.text.toString().isEmpty()
            ) {
                return@setOnClickListener
            }
            /*ADD API*/
            val request = ResetRequest(
                mobile,
                editNewPassword?.text.toString(),
                editConfirmNewPassword?.text.toString()
            )
            presenter.resetPassword(this, request)
        }
        layoutNewPassword?.setOnClickListener { hideKeyboard() }
    }

    private fun validateButton() {
        if (editNewPassword?.text.toString().isNotEmpty() &&
            editConfirmNewPassword?.text.toString().isNotEmpty() &&
            editNewPassword?.text.toString().equals(editConfirmNewPassword?.text.toString()) && min
        ) {
            buttonChangePassword?.isEnabled = true
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                buttonChangePassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button, theme))
            } else {
                buttonChangePassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button))
            }
        } else {
            buttonChangePassword?.isEnabled = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                buttonChangePassword?.backgroundTintList =
                    ColorStateList.valueOf(
                        resources.getColor(R.color.color_button_inactive, theme)
                    )
            } else {
                buttonChangePassword.toDisable()
                buttonChangePassword?.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_button_inactive))
            }
        }
    }

    private fun validatePassword(password: String) {
//        if (password.matches(".*[A-Z].*".toRegex())) {
//            textUppercase.setTextColor(ContextCompat.getColor(this, R.color.limeAde))
//            textUppercase.setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(
//                    this,
//                    R.drawable.ic_check
//                ), null, null, null
//            )
//            upper = true
//        } else {
//            textUppercase.setTextColor(ContextCompat.getColor(this, R.color.dustyGray))
//            textUppercase.setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(
//                    this,
//                    R.drawable.ic_uncheck
//                ), null, null, null
//            )
//            upper = false
//        }

//        if (password.matches(".*[0-9].*".toRegex())) {
//            textSpecialChar.setTextColor(ContextCompat.getColor(this, R.color.limeAde))
//            textSpecialChar.setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(
//                    this,
//                    R.drawable.ic_check
//                ), null, null, null
//            )
//            sensitive = true
//        } else {
//            textSpecialChar.setTextColor(
//                ContextCompat.getColor(
//                    this,
//                    R.color.dustyGray
//                )
//            )
//            textSpecialChar.setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(
//                    this,
//                    R.drawable.ic_uncheck
//                ), null, null, null
//            )
//            sensitive = false
//        }

        if (password.length > 7) {
            textMinPassword.setTextColor(ContextCompat.getColor(this, R.color.limeAde))
            textMinPassword.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_check
                ), null, null, null
            )
            min = true
        } else {
            textMinPassword.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.dustyGray
                )
            )
            textMinPassword.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_uncheck
                ), null, null, null
            )
            min = false
        }

        when {
            password.isEmpty() -> {
                textInputNewPassword.error = getString(R.string.register_password_empty_error)
            }
            min -> {
                textInputNewPassword.error = null
            }
        }
        validateButton()
    }

    private fun showPasswordDialog() {
        layoutPassword.toVisible()
        imageGatePassword.toVisible()
    }

    private fun hidePasswordDialog() {
        layoutPassword.toGone()
        imageGatePassword.toGone()
    }

    override fun showLoading() {
        progress?.showDialog(this)
    }

    override fun hideLoading() {
        progress?.dismissDialog()
    }

    override fun getMessage(response: ResetResponse) {
        when (response.status) {
            200 -> {
                val b = Bundle()
                b.putString("username", mobile)
                b.putString("password", editConfirmNewPassword.text.toString())
                goToActivity(ForgotPasswordSuccessActivity::class.java, b)
                finish()
            }
            else -> {
                showDialogError(response.message)
            }
        }
    }

    override fun showError(throwable: Throwable) {
        showDialogError(throwable.message.toString())
    }

    private fun showDialogError(message: String) {
        showOkDialog(message, "Oke", DialogInterface.OnClickListener { _, _ ->
            resetAll()
        })
    }

    private fun resetAll() {
        editNewPassword?.error = null
        editNewPassword?.setText("")
        editConfirmNewPassword?.setText("")
    }
}