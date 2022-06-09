package com.spin.id.ui.intro.topic

import com.spin.id.api.response.preference.PreferenceResponse
import com.spin.id.api.response.topic.TopicResponse

interface SelectTopicView {
    fun showProgress()
    fun hideProgress()
    fun getData(result: TopicResponse)
    fun showError(throwable: Throwable)
    fun successUpdateDataPreference(result: PreferenceResponse)
}