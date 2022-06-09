package com.spin.id.ui.login

import com.spin.id.CustomApplication
import com.spin.id.api.request.login.AddNumberRequest
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.login.LoginSSORequest
import com.spin.id.api.request.validation.CheckUserRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class LoginPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun login(view: LoginContract.View, request: LoginRequest) {
        view.showProgress()
        disposable = apiFactory?.login(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun checkValidation(view: LoginContract.View, request: CheckUserRequest) {
        disposable = apiFactory?.checkUser(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataUser(result)
            }, {
                view.showError(it)
            })
    }

    fun addNumber(view: LoginContract.View, request: AddNumberRequest) {
        view.showProgress()
        disposable = apiFactory?.addNumber(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataAddNumber(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun login(
        view: LoginContract.SSOView,
        request: LoginSSORequest,
        firstName: String?,
        lastName: String?,
        email: String?,
        type: String
    ) {
        disposable = apiFactory?.login(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getDataSSO(it, request.token, firstName, lastName, email, type)
            }, {
                view.showErrorSSO(it)
            })
    }
}