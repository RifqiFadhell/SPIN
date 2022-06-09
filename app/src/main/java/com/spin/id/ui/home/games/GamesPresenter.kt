package com.spin.id.ui.home.games

import com.spin.id.CustomApplication
import com.spin.id.api.request.mission.MissionRequest
import com.spin.id.api.request.redeem.RedeemRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GamesPresenter {
    private var apiFactory = CustomApplication.apiClient
    private var disposable: Disposable? = null

    fun getListMission(view: GamesContract.GamesMissionView, user_id: String? = null) {
        disposable = apiFactory?.getMissionGames(MissionRequest(user_id))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataMission(result)
            }, {
                view.showError(it)
            })
    }

    fun getListLeaderboard(view: GamesContract.LeaderboardView) {
        view.showProgress()
        disposable = apiFactory?.getListLeaderboard()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getDataLeaderboard(result)
                view.hideProgress()
            }, {
                view.showError(it)
                view.hideProgress()
            })
    }

    fun redeem(view: GamesContract.Redeem, request: RedeemRequest, token: String) {
        disposable = apiFactory?.redeem(request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result ->
                view.getData(result)
            }, {
                view.showError(it)
            })
    }
}