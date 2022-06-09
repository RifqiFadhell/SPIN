package com.spin.id.ui.intro.topic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.spin.id.R
import com.spin.id.api.request.preference.UpdatePreferenceRequest
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.preference.PreferenceResponse
import com.spin.id.api.response.topic.TopicData
import com.spin.id.api.response.topic.TopicResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.home.feed.FeedPresenter
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.intro.IntroPresenter
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SELECT_TOPIC_LOAD
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SELECT_TOPIC_SUBMIT
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.select_topic_fragment.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class SelectTopicFragment : BaseFragment(),
    SelectTopicView {

    private lateinit var presenter: IntroPresenter
    private lateinit var adapter: TopicListAdapter
    private var itemList = ArrayList<TopicData>()
    private var itemListSelected = ArrayList<TopicData>()
    private var progress: LoaderIndicatorHelper? = null
    private var tracker: AmplitudeClient? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.select_topic_fragment, container, false)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.SELECT_TOPIC

    override fun init(bundle: Bundle?) {
        tracker = Amplitude.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        presenter = IntroPresenter()
        adapter = TopicListAdapter(
            requireContext(),
            itemList
        ) { pos: Int, topicData: TopicData ->
            topicData.isSelected = !topicData.isSelected
            adapter.notifyItemChanged(pos)
            countCheckedSelected()
        }
        progress = LoaderIndicatorHelper()
        listTopic?.layoutManager = LinearLayoutManager(context)
        listTopic?.adapter = adapter
        buttonNext.text = getString(R.string.save_label)
        buttonNext.toDisable()
    }

    override fun initData(bundle: Bundle?) {
        presenter.getListTopic(this)
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener {
            (activity as IntroActivity).setCurrentItem(0)
        }
        buttonNext?.setOnClickListener {
            if (itemListSelected.isEmpty()) {
                return@setOnClickListener
            }
            nextStep()
        }
        buttonRefreshError?.setOnClickListener {
            initData(bundle)
        }
    }

    private fun setUserProperties(key: String, value: Boolean) {
        firebaseAnalytics.setUserProperty(key, value.toString())
    }

    private fun nextStep() {
        if ((activity as IntroActivity).type == "set-preference" && (activity as IntroActivity).isLogin == true) {
            validateSelectTopic()
            updateDataPreference()
        } else {
            validateSelectTopic()
            (activity as IntroActivity).setCurrentItem(2)
        }
    }

    private fun validateSelectTopic() {
        setUserProperty()
        val list = ArrayList<TopicData>()
        val listName = ArrayList<String>()
        for (x in 0 until itemList.size) {
            if (itemList[x].isSelected) {
                list.plusAssign(itemList[x])
                listName.plusAssign(itemList[x].name)
            }
        }
        TinyDB(requireContext()).putListObject(
            TinyConstant.TINY_LIST_TOPIC,
            list as java.util.ArrayList<Any>
        )
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Topic Category", listName)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(PARAM_SELECT_TOPIC_SUBMIT, eventProperties)
    }

    private fun setUserProperty() {
        var count = 500L
        for (x in 0 until itemList.size) {
            count++
            GlobalScope.launch {
                delay(count)
                setUserProperties(itemList[x].codename, itemList[x].isSelected)
            }
        }
    }

    override fun showProgress() {
        progress?.showDialog(requireContext())
        listTopic?.toVisible()
        textTitleTopic?.toVisible()
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    override fun getData(result: TopicResponse) {
        if (result.status != 200) requireContext().showErrorDialog()
        layoutErrorTopic?.toGone()
        listTopic?.toVisible()
        textTitleTopic?.toVisible()

        val data = result.data
        val dataExist = TinyDB(requireContext()).getListObject(
            TinyConstant.TINY_LIST_TOPIC,
            TopicData::class.java
        ) as ArrayList<TopicData>
        val finalList = ArrayList<TopicData>()
        val combine = ArrayList<TopicData>()
        combine.plusAssign(dataExist)
        combine.plusAssign(data)
        if (!dataExist.isNullOrEmpty()) {
            val filter: HashSet<TopicData> = java.util.HashSet(combine)
            val final: ArrayList<TopicData> = ArrayList(filter)
            finalList.plusAssign(final)
        } else {
            finalList.plusAssign(data)
        }
        itemList.clear()
        itemList.addAll(finalList)
        adapter.notifyDataSetChanged()
        countCheckedSelected()
    }

    override fun showError(throwable: Throwable) {
        layoutErrorTopic?.toVisible()
        textTitleError?.text = throwable.message

        listTopic?.visibility = View.GONE
        textTitleTopic?.visibility = View.GONE
    }

    override fun successUpdateDataPreference(result: PreferenceResponse) {
        val status = TinyDB(requireContext()).getString("from")
        when (result.status) {
            200 -> context?.showShortToast("Update Preferences Berhasil")
            else -> context?.showShortToast(result.message)
        }
        FeedPresenter.onRefresh = true
        if (status == "login") requireActivity().goToActivity(HomeActivity::class.java)
        activity?.finish()
    }

    private fun updateDataPreference() {
        if (getUserId().isEmpty()) {
            context?.showShortToast("Anda belum login, silakan login dulu")
            activity?.finish()
            return
        }
        val request =
            UpdatePreferenceRequest(getSelectedIdGames(), getSelectedIdTopic(), getUserId())
        presenter.updatePreference(
            this,
            request,
            TinyDB(requireContext()).getString(TINY_TOKEN).toString()
        )
    }

    private fun countCheckedSelected() {
        itemListSelected.clear()
        for (item in itemList) {
            if (item.isSelected) {
                itemListSelected.add(item)
            }
        }
        if (itemListSelected.size > 0) {
            textCounter.visibility = View.VISIBLE
            buttonNext.isEnabled = true
        } else {
            textCounter.visibility = View.GONE
            buttonNext.isEnabled = false
        }
        textCounter.text =
            getString(R.string.topic_selected_text, itemListSelected.size.toString())
    }

    private fun getSelectedIdGames(): ArrayList<String> {
        val idListGame = ArrayList<String>()
        val itemList = ArrayList<GameData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_GAME, GameData::class.java
            ) as ArrayList<GameData>
        )
        Log.e("DATA GAMES SELECTED 2", Gson().toJson(itemList))
        for (item in itemList) {
            if (item.isSelected) {
                idListGame.add(item.id)
            }
        }
        return idListGame
    }

    private fun getSelectedIdTopic(): ArrayList<String> {
        val idListTopic = ArrayList<String>()
        val itemList = ArrayList<TopicData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
            ) as ArrayList<TopicData>
        )
        for (item in itemList) {
            if (item.isSelected) {
                idListTopic.add(item.id)
            }
        }
        return idListTopic
    }

    private fun getUserId(): String {
        val data = TinyDB(requireContext()).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        if (data != null) {
            val userId = data.userId
            if (userId != null) return userId
            else return ""
        } else return ""
    }

    override fun onResume() {
        super.onResume()
        tracker?.logEvent(PARAM_SELECT_TOPIC_LOAD)
    }
}