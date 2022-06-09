package com.spin.id.ui.intro.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.spin.id.R
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.game.GameResponse
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LIST_GAME
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.intro.IntroPresenter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SELECT_GAME_LOAD
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SELECT_GAME_SUBMIT
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.select_game_fragment.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class SelectGameFragment : BaseFragment(),
    SelectGameView {

    private lateinit var presenter: IntroPresenter
    private lateinit var adapter: GameListAdapter
    private var itemList = ArrayList<GameData>()
    private var itemListSelected = ArrayList<GameData>()
    private var tracker: AmplitudeClient? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.select_game_fragment, container, false)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.SELECT_GAME

    override fun init(bundle: Bundle?) {
        tracker = Amplitude.getInstance()
        presenter = IntroPresenter()
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        adapter = GameListAdapter(requireContext(), itemList) { pos: Int, gameData: GameData ->
            gameData.isSelected = !gameData.isSelected
            adapter.notifyItemChanged(pos)
            countCheckedSelected()
        }

        listGame?.layoutManager = LinearLayoutManager(context)
        listGame?.adapter = adapter
        buttonNext.toDisable()
        buttonNext.text = getString(R.string.save_label)
    }

    override fun initData(bundle: Bundle?) {
        presenter.getListGame(this)
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener {
            startActivity(Intent(activity, BoardingActivity::class.java))
        }
        buttonNext?.setOnClickListener {
            if (itemListSelected.isEmpty()) {
                return@setOnClickListener
            }
            validateSelectGame()
            (activity as IntroActivity).setCurrentItem(1)
        }
        buttonRefreshError?.setOnClickListener {
            initData(bundle)
        }
    }

    private fun setUserProperties(key: String, value: Boolean) {
        firebaseAnalytics.setUserProperty(key, value.toString())
    }

    private fun validateSelectGame() {
        setUserProperty()
        val list = ArrayList<GameData>()
        val listName = ArrayList<String>()
        for (x in 0 until itemList.size) {
            if (itemList[x].isSelected) {
                list.plusAssign(itemList[x])
                listName.plusAssign(itemList[x].name)
            }
        }
        TinyDB(requireContext()).putListObject(TINY_LIST_GAME, list as java.util.ArrayList<Any>)

        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Game Category", listName)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(PARAM_SELECT_GAME_SUBMIT, eventProperties)
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
        layoutErrorGame?.toGone()
        listGame?.toGone()
        textTitleGame?.toGone()
    }

    override fun hideProgress() {
    }

    override fun getData(result: GameResponse) {
        if (result.status != 200) requireContext().showErrorDialog()
        Log.e("GAME DATA", "MASUK GET DATA")
        layoutErrorGame?.toGone()
        listGame?.toVisible()
        textTitleGame?.toVisible()

        val data = result.data
        val dataExist = TinyDB(requireContext()).getListObject(
            TINY_LIST_GAME,
            GameData::class.java
        ) as ArrayList<GameData>
        val finalList = ArrayList<GameData>()
        val combine = ArrayList<GameData>()
        combine.plusAssign(dataExist)
        combine.plusAssign(data)
        if (!dataExist.isNullOrEmpty()) {
            val filter: HashSet<GameData> = java.util.HashSet(combine)
            val final: ArrayList<GameData> = ArrayList(filter)
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
        layoutErrorGame?.toVisible()
        textTitleError?.text = throwable.message

        listGame?.toGone()
        textTitleGame?.toGone()
    }

    override fun onResume() {
        super.onResume()
        tracker?.logEvent(PARAM_SELECT_GAME_LOAD)
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
            buttonNext.toEnable()
        } else {
            textCounter.visibility = View.GONE
            buttonNext.toDisable()
        }
        textCounter.text =
            getString(R.string.game_selected_text, itemListSelected.size.toString())
    }
}