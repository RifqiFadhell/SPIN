package com.spin.id.ui.forgotpassword

import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.resetPassword.ResetResponse
import com.spin.id.api.response.validation.CheckUserResponse

class ForgotPasswordContract {
    interface ForgotPasswordView {
        fun getData(response: CheckUserResponse)
        fun showError(throwable: Throwable)
    }

    interface NewPasswordView {
        fun showLoading()
        fun hideLoading()
        fun getMessage(response: ResetResponse)
        fun showError(throwable: Throwable)
    }
}