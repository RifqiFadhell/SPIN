package com.spin.id.ui.forgotpassword

import android.os.Bundle
import com.spin.id.R
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.GamesItem
import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.login.TopicsItem
import com.spin.id.api.response.topic.TopicData
import com.spin.id.api.response.validation.CheckUserResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.login.LoginContract
import com.spin.id.ui.login.LoginPresenter
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.showErrorDialog
import com.spin.id.utils.extensions.showLongToast
import kotlinx.android.synthetic.main.forgot_password_success_activity.*
import kotlinx.android.synthetic.main.toolbar.*

class ForgotPasswordSuccessActivity : BaseActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private var username = ""
    private var password = ""

    private var progress: LoaderIndicatorHelper? = null
    private var tinyDB: TinyDB? = null

    override fun provideLayout() {
        setContentView(R.layout.forgot_password_success_activity)
    }

    override fun getScreenName() = "forgot_password_success_page"

    override fun init(bundle: Bundle?) {
        textTitleToolbar?.text = getString(R.string.forgot_password_title_toolbar)

        presenter = LoginPresenter()
        progress = LoaderIndicatorHelper()
        tinyDB = TinyDB(this)
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            username = intent.getStringExtra("username").toString()
            password = intent.getStringExtra("password").toString()
        }
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener { finish() }
        buttonLogin?.setOnClickListener { login() }
    }

    private fun login() {
        val request = LoginRequest()
        request.username = username
        request.password = password

        presenter.login(this, request)
    }

    override fun showProgress() {
        progress?.showDialog(this)
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    override fun getData(result: LoginResponse) {
        when (result.status) {
            200 -> {
                saveLogin(result)
                goToActivity(HomeActivity::class.java)
                tinyDB?.putBoolean("SPLASH", true)
                finishAffinity()
            }

            else -> showErrorDialog()
        }
    }

    override fun getDataUser(result: CheckUserResponse) {
    }

    override fun showError(throwable: Throwable) {
        this.showLongToast(throwable.message.toString())
    }

    private fun saveLogin(response: LoginResponse) {
        response.data?.games?.let {
            updateGame(it)
        }
        response.data?.topics?.let {
            updateTopic(it)
        }
        tinyDB?.putBoolean(TinyConstant.TINY_STATUS_LOGIN, true)
        tinyDB?.putObject(TinyConstant.TINY_PROFILE, response.data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateGame(game: List<GamesItem>) {
        (TinyDB(this).getListObject(
            TinyConstant.TINY_LIST_GAME, GameData::class.java
        ) as ArrayList<*>).clear()
        val gameList = ArrayList<GameData>()
        game.let {
            for (item in it) {
                gameList.add(
                    GameData(
                        item.gameCategoryId.toString(), item.image.toString(), item.name.toString(),
                        item.sort_order.toString(), item.status.toString(), isSelected = true
                    )
                )
            }
        }
        tinyDB?.putListObject(TinyConstant.TINY_LIST_GAME, gameList as java.util.ArrayList<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateTopic(topic: List<TopicsItem>) {
        (TinyDB(this).getListObject(
            TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
        ) as ArrayList<*>).clear()
        val topicList = ArrayList<TopicData>()
        topic.let {
            for (item in it) {
                topicList.add(
                    TopicData(
                        item.description.toString(),
                        item.topicCategoryId.toString(),
                        item.image.toString(),
                        item.name.toString(),
                        item.sort_order.toString(),
                        item.status.toString(),
                        isSelected = true
                    )
                )
            }
        }
        tinyDB?.putListObject(TinyConstant.TINY_LIST_TOPIC, topicList as java.util.ArrayList<Any>)
    }

    override fun getDataAddNumber(result: LoginResponse) {}
}