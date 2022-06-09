package com.spin.id.ui.home.games

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spin.id.R
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.mission.leaderboard.LeaderboardResponse
import com.spin.id.api.response.mission.leaderboard.Rank
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToWebView
import kotlinx.android.synthetic.main.activity_leaderboard.*
import kotlinx.android.synthetic.main.leaderboard_toolbar.*

class LeaderboardActivity : BaseActivity(), GamesContract.LeaderboardView {

    private lateinit var presenter: GamesPresenter
    private lateinit var adapter: LeaderboardListAdapter
    private var itemList = ArrayList<Rank>()
    private var progress: LoaderIndicatorHelper? = null

    private var token: Int? = null

    companion object {
        const val TOKEN_VALUE = "TOKEN_VALUE"
    }

    override fun provideLayout() {
        setContentView(R.layout.activity_leaderboard)
    }

    override fun init(bundle: Bundle?) {
        presenter = GamesPresenter()
        adapter = LeaderboardListAdapter(this, itemList, getUserName())
        progress = LoaderIndicatorHelper()
        listBoard?.layoutManager = LinearLayoutManager(this)
        listBoard?.adapter = adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.catalinaBlue)
        }
    }

    override fun initData(bundle: Bundle?) {
        presenter.getListLeaderboard(this)
        if (intent != null) {
            token = intent.getIntExtra(TOKEN_VALUE, 0)
        }
        textToken?.text = getString(R.string.val_token_text, token.toString())
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener {
            finish()
        }
        rewardCard?.setOnClickListener {
            goToWebView(
                "https://dailyspin.id/spin-esports-app-leaderboards-rewards/",
                "FAQ"
            )
        }
        faqCard?.setOnClickListener {
            goToWebView(
                "https://dailyspin.id/spin-app-games-faq/",
                "FAQ"
            )
        }
    }

    override fun getScreenName(): String? {
        return ""
    }

    override fun showProgress() {
        progress?.showDialog(this)
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    override fun getDataLeaderboard(result: LeaderboardResponse) {
        val data = result.data.rank
        itemList.addAll(data)
        adapter.notifyDataSetChanged()
        val infoText = result.data.info_text
        textSubtitleLeaderboard?.text = infoText
        Log.e("LIST_LEADERBOARD", Gson().toJson(result))
    }

    override fun showError(throwable: Throwable) {
        Log.e("LIST_LEADERBOARD", throwable.message.toString())
    }

    private fun getUserName(): String {
        val data = TinyDB(this).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        Log.e("DATA_PROFILE", Gson().toJson(data))
        return if (data != null) {
            val username = data.displayName
            if (username != null) username
            else ""
        } else ""
    }

}