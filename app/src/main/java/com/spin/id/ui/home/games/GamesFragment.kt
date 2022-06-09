package com.spin.id.ui.home.games

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spin.id.R
import com.spin.id.api.request.redeem.RedeemRequest
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.mission.mission.MissionResponse
import com.spin.id.api.response.mission.mission.Task
import com.spin.id.api.response.mission.redeem.RedeemResponse
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DAILY_DATE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DATA_COMMENT_LIKE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.home.games.data.DailyTask
import com.spin.id.ui.webview.webview.builder.WebviewBuilder
import com.spin.id.ui.webview.webview.constant.AppSourceConstant
import com.spin.id.ui.webview.webview.constant.HttpMethodConstant
import com.spin.id.utils.RegexFilter
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.SELECT_GAME
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.games_fragment.*
import kotlinx.android.synthetic.main.show_point_dialog.*


class GamesFragment : BaseFragment(), GamesContract.GamesMissionView, GamesContract.Redeem {

    private var presenter: GamesPresenter? = null
    private lateinit var adapter: MissionListAdapter
    private var itemList = ArrayList<Task>()
    private var tinyDB: TinyDB? = null
    private lateinit var dialog: AlertDialog

    private var token: Int? = null

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.games_fragment, container, false)
    }

    override fun getScreenName() = SELECT_GAME

    override fun init(bundle: Bundle?) {
        setDialogCollect()
        tinyDB = TinyDB(requireContext())
        presenter = GamesPresenter()
        adapter = MissionListAdapter(requireContext(), itemList, null)
        listMission?.layoutManager = LinearLayoutManager(context)
        listMission?.adapter = adapter
    }

    override fun initData(bundle: Bundle?) {
        refresh()
    }

    override fun initListener(bundle: Bundle?) {
        buttonDetail.setOnClickListener {
            goToWebView(
                "https://dailyspin.id/spin-token-leaderboards-faq/",
                "FAQ"
            )
        }
        buttonLeaderBoards.setOnClickListener {
            val b = Bundle()
            b.putInt("TOKEN_VALUE", token ?: 0)
            activity?.goToActivity(LeaderboardActivity::class.java, b)
        }
    }

    private fun getUserId(): String {
        val data = TinyDB(requireContext()).getObject(
            TINY_PROFILE, LoginData::class.java
        )
        return if (data != null) {
            val userId = data.userId
            if (userId != null) userId
            else ""
        } else ""
    }

    private fun goToWebView(url: String, title: String) {
        WebviewBuilder(requireContext())
            .setUrl(url)
            .setTitle(title)
            .setVersionCode("0")
            .setHttpMethod(HttpMethodConstant.Value.POST)
            .setIsDebug(true)
            .setAppSource(AppSourceConstant.Value.SPIN_ANDROID)
            .show()
    }

    override fun getDataMission(result: MissionResponse) {
        token = result.data.user_spin_token
        val data = result.data.task
        (activity as HomeActivity).setTokenValue(token ?: 0)
        textTitleBanner.text = result.data.banner_title
        textSubtitleBanner.text = result.data.banner_caption
        if (data.isNotEmpty()) {
            emptyLayout.toGone()
            itemList.clear()
            itemList.addAll(data)
            adapter.notifyDataSetChanged()
            setDailyTask(result)
            Log.e("DATA_MISSION_GAMES", Gson().toJson(result))
        } else {
            emptyLayout.toVisible()
            tinyDB?.remove(TINY_DATA_COMMENT_LIKE)
            tinyDB?.remove(TINY_DAILY_DATE)
        }
    }

    override fun showError(throwable: Throwable) {
        Log.e("DATA_MISSION_GAMES", throwable.message.toString())
    }

    private fun setDailyTask(result: MissionResponse) {
        if (result.status == 200) {
            val timeDaily = result.data.task[0].task_date
            val timeNow = TimeUtils.getTime()

            if (tinyDB?.getString(TINY_DAILY_DATE).toString().isEmpty()) {
                tinyDB?.putString(TINY_DAILY_DATE, timeDaily)
                val list = ArrayList<com.spin.id.api.request.redeem.Task>()
                val tasking = com.spin.id.api.request.redeem.Task(
                    "",
                    result.data.task[0].task_id.toString(),
                    ""
                )
                list.plusAssign(tasking)
                val request = RedeemRequest(
                    list,
                    tinyDB?.getObject(TINY_PROFILE, LoginData::class.java)?.userId.toString()
                )
                presenter?.redeem(this, request, "")

                val task = result.data.task[1]
                val data = DailyTask(task.task_id, task.task_limit, task.user_task_completion)
                tinyDB?.putObject(TINY_DATA_COMMENT_LIKE, data)
            }

            val timePast = tinyDB?.getString(TINY_DAILY_DATE).toString()

            val dateOnline = RegexFilter.splitFirstWords(timeDaily)
            val dateSaved = RegexFilter.splitFirstWords(timePast)
            val timeSaved = RegexFilter.splitSecondWords(timePast)

            if (dateSaved != dateOnline) {
                if (timeSaved < timeNow) {
                    tinyDB?.putString(TINY_DAILY_DATE, timeDaily)
                    val list = ArrayList<com.spin.id.api.request.redeem.Task>()
                    val tasking = com.spin.id.api.request.redeem.Task(
                        "",
                        result.data.task[0].task_id.toString(),
                        ""
                    )
                    list.plusAssign(tasking)
                    val request = RedeemRequest(
                        list,
                        tinyDB?.getObject(TINY_PROFILE, LoginData::class.java)?.userId.toString()
                    )
                    presenter?.redeem(this, request, "")

                    val task = result.data.task[1]
                    val data = DailyTask(task.task_id, task.task_limit, task.user_task_completion)
                    tinyDB?.putObject(TINY_DATA_COMMENT_LIKE, data)
                }
            } else {
                Log.d("Debug", "After Daily Set")
            }
        }
    }

    override fun getData(result: RedeemResponse) {
        if (result.status == 200) {
            token = result.data?.userSpinToken
            (activity as HomeActivity).setTokenValue(token ?: 0)
            dialog.show()
            dialog.textSubtitle.text = getString(R.string.open_spin_app)
            dialog.textCongratsPoint.text = getString(R.string.congrats_point_text, "1")
            dialog.buttonCollect.setOnClickListener {
                dialog.hide()
                refresh()
            }
        }
    }

    private fun setDialogCollect() {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.show_point_dialog)
            .setCancelable(false)
        dialog = progressDialog.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun refresh() {
        presenter?.getListMission(this, getUserId())
    }

    fun showNotification() {
        val data = tinyDB?.getObject(TINY_DATA_COMMENT_LIKE, DailyTask::class.java)
        if (data != null && data.userTaskCompletion > 0) {
            dialog.show()
            dialog.textSubtitle.text = getString(R.string.like_comment_task)
            dialog.textCongratsPoint.text =
                getString(R.string.congrats_point_text, data.userTaskCompletion.toString())
            dialog.buttonCollect.setOnClickListener {
                /*val dataTask = DailyTask(data.taskId, data.taskLimit, data.userTaskCompletion - 1)
                tinyDB?.putObject(TINY_DATA_COMMENT_LIKE, dataTask)
                dialog.hide()
                (activity as HomeActivity).setBadge()*/
                dialog.hide()
                val dataTask = DailyTask(data.taskId, data.taskLimit, 0)
                tinyDB?.putObject(TINY_DATA_COMMENT_LIKE, dataTask)
                (activity as HomeActivity).setBadge()
            }
        }
        presenter?.getListMission(this, getUserId())
    }
}