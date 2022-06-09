package com.spin.id.ui.intro.form

import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.preference.PreferenceResponse
import com.spin.id.api.response.register.RegisterResponse
import com.spin.id.api.response.validation.CheckUserResponse

class FormRegisterContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun getData(result: CheckUserResponse)
        fun getDataRegister(result: RegisterResponse)
        fun getDataLogin(result: LoginResponse)
        fun showError(throwable: Throwable)
        fun getDataRegisterSSO(result: RegisterResponse)
        fun showErrorRegisterSSO(throwable: Throwable)
        fun getDataAddNumber(result: LoginResponse)
        fun getDataUpdatePreference(result: PreferenceResponse)
    }
}