package com.spin.id.ui.home.games

import com.spin.id.api.response.mission.leaderboard.LeaderboardResponse
import com.spin.id.api.response.mission.mission.MissionResponse
import com.spin.id.api.response.mission.redeem.RedeemResponse

class GamesContract {

    interface GamesMissionView {
        fun getDataMission(result: MissionResponse)
        fun showError(throwable: Throwable)
    }

    interface LeaderboardView {
        fun showProgress()
        fun hideProgress()
        fun getDataLeaderboard(result: LeaderboardResponse)
        fun showError(throwable: Throwable)
    }

    interface Redeem {
        fun getData(result: RedeemResponse)
        fun showError(throwable: Throwable)
    }
}