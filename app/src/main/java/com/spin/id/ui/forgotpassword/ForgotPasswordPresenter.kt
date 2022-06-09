package com.spin.id.ui.forgotpassword

import com.spin.id.CustomApplication
import com.spin.id.api.request.resetPassword.ResetRequest
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.ui.intro.form.FormRegisterContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ForgotPasswordPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun checkValidation(
        view: ForgotPasswordContract.ForgotPasswordView,
        request: CheckUserRequest
    ) {
        disposable = apiFactory?.checkUser(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })
    }

    fun resetPassword(view: ForgotPasswordContract.NewPasswordView, request: ResetRequest) {
        view.showLoading()
        disposable = apiFactory?.resetPassword(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.hideLoading()
                view.getMessage(result)
            }, {
                view.hideLoading()
                view.showError(it)
            })
    }
}