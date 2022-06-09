package com.spin.id.ui.login

import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.validation.CheckUserResponse


class LoginContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun getData(result: LoginResponse)
        fun getDataUser(result: CheckUserResponse)
        fun getDataAddNumber(result: LoginResponse)
        fun showError(throwable: Throwable)
    }

    interface SSOView {
        fun getDataSSO(result: LoginResponse, idToken: String?, firstName: String?, lastName: String?, email: String?, type: String)
        fun showErrorSSO(throwable: Throwable)
    }
}