package com.spin.id.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging
import com.spin.id.BuildConfig
import com.spin.id.R
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.topic.TopicData
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_CONFIG
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DATA_COMMENT_LIKE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK_PAGE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK_USER_ID
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK_VALUE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LIST_GAME
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LIST_TOPIC
import com.spin.id.preference.tinyDb.TinyConstant.TINY_NOTIFICATION
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGOUT
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.feed.FeedFragment
import com.spin.id.ui.home.games.GamesFragment
import com.spin.id.ui.home.games.LeaderboardActivity
import com.spin.id.ui.home.games.data.DailyTask
import com.spin.id.ui.home.profile.ProfileFragment
import com.spin.id.ui.home.shop.ItemDetailActivity
import com.spin.id.ui.home.shop.ShopFragment
import com.spin.id.ui.home.shop.detail.DetailProductActivity
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.order.OrderListActivity
import com.spin.id.utils.TimeUtils
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.HOME
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_MENU_CLICKED
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.showLongToast
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.android.synthetic.main.home_toolbar.*
import kotlinx.android.synthetic.main.notification_dialog.*
import kotlinx.android.synthetic.main.update_dialog.*
import org.json.JSONException
import org.json.JSONObject

class HomeActivity : BaseActivity() {

    private var feedFragment = FeedFragment()
    private var shopFragment = ShopFragment()
    private var gamesFragment = GamesFragment()
    private var profileFragment = ProfileFragment()
    private var fragmentManager = supportFragmentManager
    private var fragmentActive: Fragment = feedFragment
    private var countDownTimer: CountDownTimer? = null

    private var nameFragmentActive: String = "Feed"
    private var itemId: Int? = null
    private var orderId: String? = null
    private var status: Boolean? = false
    private var statusSplash: Boolean? = false

    private var tinyDb: TinyDB? = null
    private lateinit var dialog: AlertDialog
    private lateinit var dialogUpdate: AlertDialog

    private var tracker: AmplitudeClient? = null

    private val listTopic = ArrayList<String>()
    private val listGame = ArrayList<String>()

    private var fromPage = ""

    var counterOrder: Int = 0

    companion object {
        const val QUERY_PARAM_POST_ID = "post_id"
        const val QUERY_PARAM_USER_ID = "utm_campaign"
        const val QUERY_PARAM_PAGE = "page"
        const val QUERY_PARAM_VALUE = "value"
    }

    override fun provideLayout() {
        setContentView(R.layout.home_activity)
    }

    override fun getScreenName() = HOME

    override fun init(bundle: Bundle?) {
        TinyDB(this).remove(TINY_STATUS_LOGOUT)
        tracker = Amplitude.getInstance()
        tinyDb = TinyDB(this)
        val topic =
            tinyDb?.getListObject(TINY_LIST_TOPIC, TopicData::class.java) as ArrayList<TopicData>
        val game =
            tinyDb?.getListObject(TINY_LIST_GAME, GameData::class.java) as ArrayList<GameData>
        for (x in 0 until topic.size) {
            listTopic.plusAssign(topic[x].name)
        }
        for (x in 0 until game.size) {
            listGame.plusAssign(game[x].name)
        }
        statusSplash = tinyDb?.getBoolean("SPLASH")
        if (tinyDb?.getBoolean(TINY_STATUS_LOGIN) != false) {
            status = true
            Amplitude.getInstance().userId =
                tinyDb?.getObject(TINY_PROFILE, LoginData::class.java)?.userId.toString()
        } else {
            Amplitude.getInstance().userId = ""
        }

        if (intent != null) if (!intent.getStringExtra("post_id").isNullOrEmpty()) {
            tinyDb?.putString("post_id", intent.getStringExtra("post_id"))
            setNotificationOpened()
        } else if (!intent.getStringExtra("order_id").isNullOrEmpty()) {
            tinyDb?.putString("order_id", intent.getStringExtra("order_id"))
            setNotificationOpened()
        }
        initToolbar()
        checkingDeepLink()
        validateConfig()
        setDialogNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.catalinaBlue)
        }
        setBadge()
        redirectToDetailProduct()
    }

    override fun initData(bundle: Bundle?) {
        tinyDb?.remove("from")
    }

    override fun initListener(bundle: Bundle?) {

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFeed -> {
                    layoutToolbar.toVisible()
                    buttonOption.toVisible()
                    textToken.toGone()
                    groupOrder.toGone()
                    trackerMenu(nameFragmentActive, "Feed")
                    fragmentManager.beginTransaction().hide(fragmentActive).show(feedFragment)
                        .commitAllowingStateLoss()
                    fragmentActive = feedFragment
                    nameFragmentActive = "Feed"
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.homeShop -> {
                    trackerMenu(nameFragmentActive, "Shop")
                    layoutToolbar.toVisible()
                    buttonOption.toGone()
                    textToken.toGone()
                    groupOrder.toVisible()
                    setNotifShop()
                    fragmentManager.beginTransaction().hide(fragmentActive).show(shopFragment)
                        .commitAllowingStateLoss()
                    fragmentActive = shopFragment
                    nameFragmentActive = "Shop"
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.homeGames -> {
                    trackerMenu(nameFragmentActive, "Games")
                    if (status != true) {
                        goToActivity(BoardingActivity::class.java)
                    } else {
                        layoutToolbar.toVisible()
                        buttonOption.toGone()
                        textToken.toVisible()
                        groupOrder.toGone()
                        fragmentManager.beginTransaction().hide(fragmentActive).show(gamesFragment)
                            .commitAllowingStateLoss()
                        itemId = item.itemId
                        setBadge()
                        fragmentActive = gamesFragment
                        nameFragmentActive = "Games"
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.frameLayout) as GamesFragment
                        fragment.showNotification()
                        return@setOnNavigationItemSelectedListener true
                    }
                }

                R.id.homeProfile -> {
                    trackerMenu(nameFragmentActive, "Profile")
                    if (status != true) {
                        goToActivity(BoardingActivity::class.java)
                    } else {
                        layoutToolbar.toGone()
                        fragmentManager.beginTransaction().hide(fragmentActive)
                            .show(profileFragment).commitAllowingStateLoss()
                        fragmentActive = profileFragment
                        nameFragmentActive = "Profile"
                        return@setOnNavigationItemSelectedListener true
                    }
                }
            }
            false
        }

        buttonOption.setOnClickListener {
            val bundles = Bundle()
            bundles.putString("type", "set-preference")
            goToActivity(IntroActivity::class.java, bundles)
        }

        textOrder?.setOnClickListener {
            if (status != true) {
                goToActivity(BoardingActivity::class.java)
            } else {
                goToActivity(OrderListActivity::class.java)
            }
        }
    }

    fun setBadge() {
        val data = tinyDb?.getObject(TINY_DATA_COMMENT_LIKE, DailyTask::class.java)
        if (data != null && data.userTaskCompletion > 0) {
            bottomNavigation.getOrCreateBadge(R.id.homeGames)
            val game: BadgeDrawable? = bottomNavigation.getBadge(R.id.homeGames)
            game?.isVisible = true
            game?.number = data.userTaskCompletion
        } else {
            bottomNavigation.getOrCreateBadge(R.id.homeGames)
            val game: BadgeDrawable? = bottomNavigation.getBadge(R.id.homeGames)
            game?.isVisible = false
        }
    }

    fun setNotifShop() {
        if (counterOrder > 0) {
            textTotalOrder?.toVisible()
            textTotalOrder?.text = counterOrder.toString()
        } else {
            textTotalOrder?.toGone()
        }
    }

    private fun trackerMenu(current: String, target: String) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Current Page", current)
            eventProperties.put("Menu Item Target", target)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(PARAM_MENU_CLICKED, eventProperties)
    }

    private fun initFragments() {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.frameLayout, feedFragment)
        transaction.add(R.id.frameLayout, shopFragment).hide(shopFragment)
        transaction.add(R.id.frameLayout, profileFragment).hide(profileFragment)
        transaction.add(R.id.frameLayout, gamesFragment).hide(gamesFragment)
        transaction.commitAllowingStateLoss()
        setUserProperties()
    }

    private fun setUserProperties() {
        var statusLogin = false
        if (tinyDb?.getBoolean(TINY_STATUS_LOGIN) != null && tinyDb?.getBoolean(TINY_STATUS_LOGIN) == true) {
            statusLogin = true
        }
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Game", listGame)
            eventProperties.put("Selected Topic", listTopic)
            eventProperties.put("Login Status", statusLogin)
        } catch (exception: JSONException) {
        }
        tracker?.setUserProperties(eventProperties)
    }

    private fun setNotificationOpened() {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Game", listGame)
            eventProperties.put("Selected Topic", listTopic)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent("Push Notification Clicked", eventProperties)
    }

    private fun checkNotification() {
        if (tinyDb?.getBoolean(TINY_NOTIFICATION) != true) {
            dialog.show()
            dialog.buttonNo.setOnClickListener { disableNotification() }
            dialog.buttonYes.setOnClickListener { setNotification() }
        }
    }

    private fun setDialogNotification() {
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.notification_dialog)
            .setCancelable(false)

        dialog = progressDialog.create()
    }

    private fun setNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("general")
        tinyDb?.putBoolean(TINY_NOTIFICATION, true)
        Amplitude.getInstance().logEvent("Allow Notification Prompt")
        dialog.hide()
    }

    private fun disableNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("general")
        tinyDb?.putBoolean(TINY_NOTIFICATION, false)
        Amplitude.getInstance().logEvent("Decline Notification Prompt")
        dialog.hide()
    }

    private fun checkingDeepLink() {
        FirebaseApp.initializeApp(this)
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener {
                it?.let { pendingDynamicLinkData ->
                    val deepLink = pendingDynamicLinkData.link
                    var targetPage = ""
                    /*if available*/
                    val postId = deepLink?.getQueryParameter(QUERY_PARAM_POST_ID)
                    val userId = deepLink?.getQueryParameter(QUERY_PARAM_USER_ID)
                    val page = deepLink?.getQueryParameter(QUERY_PARAM_PAGE)
                    val value = deepLink?.getQueryParameter(QUERY_PARAM_VALUE)

                    if (!postId.isNullOrEmpty() || !userId.isNullOrEmpty()) {
                        TinyDB(this).putString(TINY_DEEP_LINK, postId)
                        TinyDB(this).putString(TINY_DEEP_LINK_USER_ID, userId)
                    }

                    if (!page.isNullOrEmpty() || !value.isNullOrEmpty()) {
                        TinyDB(this).putString(TINY_DEEP_LINK_PAGE, page)
                        TinyDB(this).putString(TINY_DEEP_LINK_VALUE, value)
                        targetPage = page.toString()
                    }


                    if (statusSplash == false) {
                        countDownTimer = object : CountDownTimer(2000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                backgroundSplash.toGone()
                                imageSplash.toGone()
                                statusSplash = true
                                initFragments()
                                openTargetPage(targetPage)
                            }
                        }.start()
                    } else {
                        groupSplash.toGone()
                        initFragments()
                    }
                    checkNotification()
                } ?: run {
                    /*if null*/
                    if (statusSplash == false) {
                        countDownTimer = object : CountDownTimer(2000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                backgroundSplash.toGone()
                                imageSplash.toGone()
                                statusSplash = true
                                initFragments()
                            }
                        }.start()
                    } else {
                        groupSplash.toGone()
                        initFragments()
                    }
                    checkNotification()
                }
            }
        checkUpdate()
    }

    private fun openTargetPage(page: String) {
        when (page) {
            "game" -> {
                if (status == true) {
                    fragmentManager.beginTransaction().hide(fragmentActive).show(gamesFragment)
                        .commitAllowingStateLoss()
                } else {
                    goToActivity(IntroActivity::class.java)
                }
            }
            "leader_board" -> {
                goToActivity(LeaderboardActivity::class.java)
            }
            "shop" -> {
                fragmentManager.beginTransaction().hide(fragmentActive).show(shopFragment)
                    .commitAllowingStateLoss()
            }
            "product_list" -> {
                goToActivity(ItemDetailActivity::class.java)
            }
            "product_detail" -> {
                goToActivity(DetailProductActivity::class.java)
            }
        }
    }

    private fun checkUpdate() {
        val date = TimeUtils.getDate()
        var saveDate = ""
        if (!tinyDb?.getString("date").isNullOrEmpty()) saveDate =
            tinyDb?.getString("date").toString()
        if (saveDate != date) {
            setDialogUpdate()
            validateUpdate()
        }
    }

    private fun validateConfig() {
        val update = FirebaseDatabase.getInstance().reference
        var config: String
        update.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                showLongToast(error.message)
            }

            override fun onDataChange(status: DataSnapshot) {
                config = status.child("config").value.toString()
                if (config.isNotEmpty()) tinyDb?.putString(TINY_CONFIG, config)
            }
        })
    }

    private fun validateUpdate() {
        val date = TimeUtils.getDate()
        val update = FirebaseDatabase.getInstance().reference
        var code: Int
        val codeApp = BuildConfig.VERSION_CODE
        update.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                code = codeApp
            }

            override fun onDataChange(status: DataSnapshot) {
                code = status.child("update").value.toString().toInt()
                if (code > codeApp) {
                    dialogUpdate.show()
                    dialogUpdate.buttonUpdateNo.setOnClickListener {
                        dialogUpdate.hide()
                        tinyDb?.putString("date", date)
                    }
                    dialogUpdate.buttonUpdateYes.setOnClickListener {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                            )
                        )
                        tinyDb?.remove("date")
                    }
                } else {
                    dialogUpdate.hide()
                }
            }
        })
    }

    private fun setDialogUpdate() {
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.update_dialog)
            .setCancelable(false)
        dialogUpdate = progressDialog.create()
    }

    private fun initToolbar() {
        buttonBack.toGone()
        textTitleToolbar.toGone()
        imageToolbar.toVisible()
        buttonOption.toVisible()
    }

    fun setTokenValue(token: Int) {
        textToken?.text = getString(R.string.val_token_text, token.toString())
    }

    private fun redirectToDetailProduct() {
        fromPage = intent.getStringExtra("FROM_PAGE") ?: ""
        if (fromPage.equals("DetailProduct")) {
            val selectedId = tinyDb?.getInt(TinyConstant.TINY_LAST_SELECTED_PRODUCT_ID) ?: 0
            val selectedName = tinyDb?.getString(TinyConstant.TINY_LAST_SELECTED_PRODUCT_NAME)
            val b = Bundle()
            b.putInt(ItemDetailActivity.PRODUCT_ID, selectedId)
            b.putString(ItemDetailActivity.PRODUCT_NAME, selectedName)
            b.putString(ItemDetailActivity.FROM_PAGE, fromPage)
            goToActivity(ItemDetailActivity::class.java, b)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB(this).remove(TINY_DEEP_LINK)
        TinyDB(this).remove("post_id")
        TinyDB(this).remove(TINY_DEEP_LINK_USER_ID)
        statusSplash = false
        tinyDb?.putBoolean("SPLASH", false)
    }

    override fun onPause() {
        super.onPause()
        TinyDB(this).remove(TINY_DEEP_LINK)
        TinyDB(this).remove("post_id")
        TinyDB(this).remove(TINY_DEEP_LINK_USER_ID)
    }
}