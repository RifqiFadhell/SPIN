package com.spin.id.ui.otp

import com.spin.id.CustomApplication
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.otp.OtpRequest
import com.spin.id.api.request.otp.OtpVerifyRequest
import com.spin.id.api.request.register.RegisterRequest
import com.spin.id.ui.intro.form.FormRegisterContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class OtpPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun requestOtp(view: OtpContract.View, request: OtpRequest) {
        view.showProgress()
        disposable = apiFactory?.requestOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataRequest(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun verifyOtp(view: OtpContract.View, request: OtpVerifyRequest) {
        view.showProgress()
        disposable = apiFactory?.verifyOtp(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideProgress()
                view.getDataVerify(result)
            }, {
                view.hideProgress()
                view.showError(it)
            })
    }

    fun login(view: OtpContract.View, request: LoginRequest) {
        disposable = apiFactory?.login(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataLogin(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun register(view: OtpContract.View, request: RegisterRequest) {
        view.showProgress()
        disposable = apiFactory?.register(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataRegister(result)
                view.hideProgress()
            }, {
                view.showErrorRegister(it)
                view.hideProgress()
            })
    }
}