package com.spin.id.ui.intro

import com.spin.id.CustomApplication
import com.spin.id.api.request.preference.UpdatePreferenceRequest
import com.spin.id.ui.intro.game.SelectGameView
import com.spin.id.ui.intro.topic.SelectTopicView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class IntroPresenter {

    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun getListGame(view: SelectGameView) {
        view.showProgress()
        disposable = apiFactory?.getListGame()
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

    fun getListTopic(view: SelectTopicView) {
        view.showProgress()
        disposable = apiFactory?.getListTopic()
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

    fun updatePreference(view: SelectTopicView, request: UpdatePreferenceRequest, token: String) {
        view.showProgress()
        disposable = apiFactory?.updatePreference(token, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.successUpdateDataPreference(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }
}