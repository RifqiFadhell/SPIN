package com.spin.id.ui.otp

import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.otp.OtpRequestResponse
import com.spin.id.api.response.otp.OtpSuccessResponse
import com.spin.id.api.response.register.RegisterResponse

class OtpContract {

    interface View {
        fun showProgress()
        fun hideProgress()
        fun getDataRequest(result: OtpRequestResponse)
        fun getDataVerify(result: OtpSuccessResponse)
        fun getDataLogin(result: LoginResponse)
        fun getDataRegister(result: RegisterResponse)
        fun showError(throwable: Throwable)
        fun showErrorRegister(throwable: Throwable)
    }
}