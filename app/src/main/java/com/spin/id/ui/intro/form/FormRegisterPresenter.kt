package com.spin.id.ui.intro.form

import com.spin.id.CustomApplication
import com.spin.id.api.request.login.AddNumberRequest
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.login.LoginSSORequest
import com.spin.id.api.request.preference.UpdatePreferenceRequest
import com.spin.id.api.request.register.RegisterRequest
import com.spin.id.api.request.register.RegisterSSORequest
import com.spin.id.api.request.validation.CheckUserRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FormRegisterPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun checkValidation(view: FormRegisterContract.View, request: CheckUserRequest) {
        disposable = apiFactory?.checkUser(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })
    }

    fun register(view: FormRegisterContract.View, request: RegisterRequest) {
        view.showProgress()
        disposable = apiFactory?.register(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataRegister(result)
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun login(view: FormRegisterContract.View, request: LoginRequest) {
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

    fun registerSSO(view: FormRegisterContract.View, request: RegisterSSORequest) {
        disposable = apiFactory?.register(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataRegisterSSO(result)
                view.hideProgress()
            }, {
                view.showErrorRegisterSSO(it)
                view.hideProgress()
            })
    }

    fun login(view: FormRegisterContract.View, request: LoginSSORequest){
        disposable = apiFactory?.login(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view.getDataLogin(it)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun addNumber(view: FormRegisterContract.View, request: AddNumberRequest) {
        disposable = apiFactory?.addNumber(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataAddNumber(result)
            }, {
                view.showError(it)
            })
    }

    fun updatePreference(
        view: FormRegisterContract.View,
        request: UpdatePreferenceRequest,
        token: String
    ) {
        disposable = apiFactory?.updatePreference(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataUpdatePreference(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })

    }
}