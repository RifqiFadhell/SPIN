package com.spin.id.ui.intro.game

import com.spin.id.api.response.game.GameResponse
import com.spin.id.api.response.preference.PreferenceResponse

interface SelectGameView {
    fun showProgress()
    fun hideProgress()
    fun getData(result: GameResponse)
    fun showError(throwable: Throwable)
}