package com.spin.id.ui.webview.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.PorterDuff.Mode
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.feed.*
import com.spin.id.api.response.feed.comment.CommentData
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.api.response.feed.like.LikeFeedResponse
import com.spin.id.api.response.feed.list.FeedData
import com.spin.id.api.response.login.LoginData
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.feed.comment.CommentListAdapter
import com.spin.id.ui.home.feed.comment.DeleteCommentDialog
import com.spin.id.ui.home.feed.comment.ReplyCommentListDialog
import com.spin.id.ui.webview.WebConstant
import com.spin.id.ui.webview.WebConstant.DATA
import com.spin.id.ui.webview.utils.DeviceUtil
import com.spin.id.ui.webview.utils.formatContactNumber
import com.spin.id.ui.webview.webview.builder.DataModel
import com.spin.id.utils.NavigationBarUtil
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.extensions.*
import id.qasir.webviewaddon.utils.PhoneNumberUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.comment_fragment.*
import kotlinx.android.synthetic.main.webview_activity.*
import kotlinx.android.synthetic.main.webview_toolbar.*
import kotlinx.android.synthetic.main.webview_toolbar.toolbar
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class WebActivity : AppCompatActivity(), WebContract.LikeFeedView,
    WebContract.CommentListFeedView, WebContract.CommentListPageView,
    WebContract.CommentFeedView, WebContract.LikeCommentView,
    WebContract.DeleteCommentFeedView {

    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri>? = null

    lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    lateinit var frameProgressBar: FrameLayout
    lateinit var buttonRetry: Button
    lateinit var imageError: ImageView
    private lateinit var textError: TextView
    lateinit var layoutError: ConstraintLayout

    private var currentUrl: String? = null
    private var baseUrl: String? = null
    private var errorConnectionText: String? = null
    private var errorConnectionTextButton: String? = null
    private var errorServerText: String? = null
    private var errorServerTextButton: String? = null

    private var errorConnectionImage: Bitmap? = null
    private var errorServerImage: Bitmap? = null

    private var deviceId = ""
    private var customKey = ""
    private var customValue = ""
    private var versionCode = ""
    private var appSource = ""
    private var token = ""
    private var postId = ""
    private var like = ""
    private var comment = ""
    private var isLogin = false
    private var dataFeed: FeedData? = null

    private var closeWebView: Boolean = false
    private var useTokenFromIntent: Boolean = false
    private var webviewJavascriptEnable: Boolean = true
    private var webviewAllowFileAccess: Boolean = true
    private var webviewAllowContentAccess: Boolean = true
    private var webviewDomStorageEnabled: Boolean = true
    private var isDebug = false
    private var isError = false
    private var isButtonRetryVisible = true

    private var webviewLayerType: Int = View.LAYER_TYPE_HARDWARE
    private var webviewCacheMode: Int = WebSettings.LOAD_DEFAULT

    private var mCustomView: View? = null
    private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
    protected var mFullscreenContainer: FrameLayout? = null
    private var mOriginalOrientation = 0
    private var mOriginalSystemUiVisibility = 0

    private lateinit var commentDialog: BottomSheetDialog
    private var viewComment: View? = null
    private var totalSizeComment = 0
    private var itemListComment = ArrayList<CommentData>()
    private var totalPageComment = 0
    private lateinit var adapterComment: CommentListAdapter
    private var currentPageComment = 1
    private var presenter: WebPresenter? = null
    private var tracker: AmplitudeClient? = null

    companion object {

        const val REQUEST_SELECT_FILE = 100
        const val FILE_CHOOSER_RESULT_CODE = 1
        const val CONTACT_CHOOSER_RESULT_CODE = 2
        var tokenValue = ""
        var title = ""
        var clientSecret = ""
        var redirectToHomeUrl: Boolean = false
        var closeWebView: Boolean = false
        var homeUrl: String? = null
        var httpMethod: Int = 0
        var url: String? = null
        var additionalHttpHeaders: MutableMap<String, String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set content view
        setContentView(R.layout.webview_activity)

        init()
        initData(savedInstanceState)
        initListener(savedInstanceState)
    }

    class WebChromeClientCustom(var activity: WebActivity) : WebChromeClient() {

        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalSystemUiVisibility = 0

        private val FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE

        override fun onHideCustomView() {
            (activity.window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
            activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility = activity.window.decorView.systemUiVisibility
            mCustomViewCallback = paramCustomViewCallback
            (activity.window
                .decorView as FrameLayout)
                .addView(
                    mCustomView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            activity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
            mCustomView!!.setOnSystemUiVisibilityChangeListener { updateControls() }
        }

        private fun updateControls() {
            val params =
                mCustomView!!.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = 0
            params.topMargin = 0
            params.leftMargin = 0
            params.rightMargin = 0
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            mCustomView!!.layoutParams = params
            activity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
        }
    }

    override fun onBackPressed() {
        if (currentUrl != null && currentUrl == homeUrl || closeWebView) {
            finish()
            dataFeed?.let { trackerItemFeed(it) }
        } else if (redirectToHomeUrl) {
            redirectToHomeUrl = false

            when (httpMethod) {
                WebConstant.HTTP_METHOD_GET_VALUE -> {
                    webView.clearHistory()
                    additionalHttpHeaders?.let { webView.loadUrl(homeUrl.toString(), it) }
                }
                WebConstant.HTTP_METHOD_POST_VALUE -> {
                    webView.clearHistory()
                    webView.postUrl(homeUrl.toString(), ByteArray(0))
                }
            }
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
            dataFeed?.let { trackerItemFeed(it) }
        }
        window
    }

    private fun trackerItemFeed(data: FeedData) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Post ID", data.post_id)
            eventProperties.put("Post Description", data.caption)
            eventProperties.put("Post Game Main Category", data.game_name)
            eventProperties.put("Post Topic Main Category", data.topic_name)
            eventProperties.put("Post Moderator", "admin")
            eventProperties.put("Post Creator", data.publisher_name)
            eventProperties.put("Post Platform", data.platform_name)
            eventProperties.put("Post Schedule", data.publishing_schedule)
            eventProperties.put("Post Source", "Feed")
            eventProperties.put("Post Rank", data.trending_id)
        } catch (exception: JSONException) {
        }

        tracker?.logEvent(AnalyticsTrackerConstant.PARAM_CLOSE_WEB_VIEW, eventProperties)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return
                }
                uploadMessage?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(resultCode, intent)
                )
                uploadMessage = null
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (mUploadMessage == null) {
                return
            }
            // Use MainMenuActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result =
                if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else {
            Toast.makeText(applicationContext, "Failed to Upload Image", Toast.LENGTH_LONG).show()
        }

        if (resultCode == Activity.RESULT_OK && requestCode == CONTACT_CHOOSER_RESULT_CODE) {
            val contactData = intent?.data
            val cursor = managedQuery(contactData, null, null, null, null)
            cursor.moveToFirst()

            val number =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (PhoneNumberUtils.isValidFormat(number)) {
                sendContact(number.formatContactNumber())
            } else {
                Toast.makeText(this, R.string.web_view_number_not_valid, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        tracker = Amplitude.getInstance()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        webView = findViewById(R.id.web_view)
        progressBar = findViewById(R.id.progress_bar_refresh)
        frameProgressBar = findViewById(R.id.frame_progress_bar_refresh)
        buttonRetry = findViewById(R.id.button_action)
        imageError = findViewById(R.id.image_error)
        textError = findViewById(R.id.text_caption_error)
        layoutError = findViewById(R.id.layout_error)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        deviceId = DeviceUtil(this).deviceId
        isLogin = TinyDB(this).getBoolean(TINY_STATUS_LOGIN)

        val data = intent.getParcelableExtra<DataModel>(DATA)
        if (data == null) {
            val intent = intent.extras
            if (intent != null) {
                httpMethod = intent.getInt("http_method_key", 0)
                url = intent.getString("url_intent_key", "")
                appSource = intent.getString("app_source", "")
            }

        } else {
            tokenValue = data.token.toString()
            clientSecret = data.clientSecret.toString()
            versionCode = data.versionCode
            postId = versionCode
            appSource = data.appSource
            webviewLayerType = data.layerType
            webviewCacheMode = data.cacheMode
            webviewJavascriptEnable = data.javascriptEnable
            webviewAllowFileAccess = data.allowFileAccess
            webviewAllowContentAccess = data.allowContentAccess
            webviewDomStorageEnabled = data.domStorageEnabled
            customKey = data.customKey.toString()
            customValue = data.customValue.toString()
            httpMethod = data.httpMethod
            useTokenFromIntent = data.useTokenFromIntent
            isDebug = data.isDebug
            url = data.url
            baseUrl = data.baseUrl
            title = data.title
            like = data.like.toString()
            comment = data.comment.toString()
            errorConnectionImage = data.errorConnectionImage
            errorConnectionText = data.errorConnectionText
            errorConnectionTextButton = data.errorConnectionTextButton
            errorServerImage = data.errorConnectionImage
            errorServerText = data.errorConnectionText
            errorServerTextButton = data.errorConnectionTextButton
            isButtonRetryVisible = data.isButtonRetryVisible
            dataFeed = data.dataFeed
        }

        if (versionCode == "0") {
            cardBottom.toGone()
        } else {
            cardBottom.toVisible()
            buttonLike.text = like
            buttonComment.text = comment
        }

        textSubtitleToolbar.text = url

        webView.setLayerType(webviewLayerType, null)

        progressBar.indeterminateDrawable.setColorFilter(
            resources.getColor(R.color.color_button),
            Mode.SRC_IN
        )

        val webSettings = webView.settings
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.javaScriptEnabled = webviewJavascriptEnable
        webSettings.allowFileAccess = webviewAllowFileAccess
        webSettings.allowContentAccess = webviewAllowContentAccess
        webSettings.domStorageEnabled = webviewDomStorageEnabled
        webSettings.cacheMode = webviewCacheMode

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                showProgressBar()
                currentUrl = url
                additionalHttpHeaders?.let { view.loadUrl(url, it) }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                hideError()
                webView.visibility = View.GONE
                showProgressBar()
            }

            override fun onPageFinished(view: WebView, url: String) {
                textTitleToolbar.text = view.title
                hideProgressBar()
                if (!isError) {
                    hideError()
                    webView.visibility = View.VISIBLE
                }
                currentUrl = url
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                isError = true
                layoutError.visibility = View.VISIBLE
                webView.visibility = View.GONE
                buttonRetry.setOnClickListener {
                    view.loadUrl(currentUrl.toString())
                    isError = false
                }
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_TIMEOUT || errorCode == ERROR_CONNECT) {
                    showErrorConnection()
                } else if (errorCode == ERROR_BAD_URL) {
                    showErrorServer()
                } else {
                    showErrorServer()
                }
            }
        }
        webView.webChromeClient = WebChromeClientCustom(this)
        setBottomSheetComment()
    }

    private fun initData(bundle: Bundle?) {
        showProgressBar()
        homeUrl = url
        val myTokenKey = customKey
        val myTokenValue = customValue

        tokenValue = "Bearer $tokenValue"
        val useTokenFromIntent = useTokenFromIntent

        additionalHttpHeaders = LinkedHashMap()
//        additionalHttpHeaders?.set("app-version-code", versionCode)
//        additionalHttpHeaders?.set("app-version-source", appSource)
        when (httpMethod) {
            WebConstant.HTTP_METHOD_GET_VALUE -> {
                additionalHttpHeaders?.set("client-secret", clientSecret)

                if (useTokenFromIntent) {
                    additionalHttpHeaders?.set("Authorization", tokenValue)
                    additionalHttpHeaders?.set(myTokenKey, myTokenValue)
                    additionalHttpHeaders?.set("device-id", deviceId)
                } else {
                    additionalHttpHeaders?.set("Authorization", tokenValue)
                    additionalHttpHeaders?.set("device-id", deviceId)
                }

                webView.loadUrl(
                    url.toString(),
                    additionalHttpHeaders as LinkedHashMap<String, String>
                )
            }

            WebConstant.HTTP_METHOD_POST_VALUE -> {
                webView.postUrl(url.toString(), ByteArray(0))
            }
        }
        if (!isDebug && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        presenter = WebPresenter()
        if (!TinyDB(this).getString(TINY_TOKEN).isNullOrEmpty()) token =
            TinyDB(this).getString(TINY_TOKEN)

        if (dataFeed?.like_status == "ACTIVE") {
            buttonLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
        } else {
            buttonLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
        }
    }

    @SuppressLint("CheckResult")
    private fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener { onBackPressed() }
        buttonShare.setOnClickListener { share() }
        toolbar.inflateMenu(R.menu.webview_menu)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionRefresh -> {
                    webView.reload()
                    true
                }
                R.id.actionOpenChrome -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    true
                }
                R.id.actionCopyLink -> {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("label", url)
                    clipboard.setPrimaryClip(clip)
                    showLongToast("Link Copied")
                    true
                }
                R.id.actionClose -> {
                    finish()
                    true
                }

                else -> true
            }
        }
        buttonLike.setOnClickListener { if (isLogin) likeFeed(postId) else goToBoarding() }
        buttonComment.setOnClickListener { if (isLogin) goToComment(postId) else goToBoarding() }
        buttonShareBottom.setOnClickListener { share() }
        commentDialog.buttonBack.setOnClickListener { commentDialog.dismiss() }

        commentDialog.editComment.afterTextChangeEvents()
            .skip(1)
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                commentDialog.textCounter.text =
                    getString(R.string.counter_comment, it.editable?.length.toString())
                commentDialog.editComment.filters += InputFilter.LengthFilter(500)
            }
    }

    private fun goToBoarding() {
        goToActivity(BoardingActivity::class.java)
    }

    private fun goToComment(postId: String) {
        getListComment(postId)
        currentPageComment = 1
        commentDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentDialog.show()
        commentDialog.listComment?.layoutManager = LinearLayoutManager(this)
        adapterComment = CommentListAdapter(
            this,
            getUserId(),
            0,
            false,
            itemListComment,
            onLikeClick = { posComment: Int, posPost: Int, item: CommentData ->
                likeComment(posPost, posComment, item.comment_id)
            },
            onReplyClick = { posComment: Int, posPost: Int, item: CommentData ->
                showReplyCommentDialog(postId, posPost, posComment, item, true)
            },
            onSeeReplyClick = { posComment: Int, posPost: Int, item: CommentData ->
                showReplyCommentDialog(postId, posPost, posComment, item)
            },
            onOptionClick = { posComment: Int, posPost: Int, item: CommentData ->
                showDeleteCommentDialog(
                    postId,
                    posComment,
                    posPost,
                    item
                )
            }
        )
        commentDialog.listComment?.adapter = adapterComment
        commentDialog.scrollComment.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY && currentPageComment < totalPageComment
                ) {
                    currentPageComment++
                    getNextComment(postId, currentPageComment)
                }
            }
        }
        commentDialog.buttonSend?.setOnClickListener {
            val comment = commentDialog.editComment?.text.toString()
            if (comment.isNotEmpty()) {
                sendComment(0, postId, comment)
            }
        }
    }

    private fun getNextComment(postId: String, page: Int) {
        val request = CommentListFeedRequest(postId, page, getUserId())
        presenter?.getNextListComment(this, request)
    }

    private fun sendComment(position: Int, postId: String, comment: String) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Post ID", dataFeed?.post_id)
            eventProperties.put("Post Description", dataFeed?.caption)
            eventProperties.put("Post Game Main Category", dataFeed?.game_name)
            eventProperties.put("Post Topic Main Category", dataFeed?.topic_name)
            eventProperties.put("Post Moderator", "admin")
            eventProperties.put("Post Creator", dataFeed?.publisher_name)
            eventProperties.put("Post Platform", dataFeed?.platform_name)
            eventProperties.put("Post Schedule", dataFeed?.publishing_schedule)
            eventProperties.put("Post Source", "Webview")
            eventProperties.put("Post Rank", dataFeed?.trending_id)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_COMMENTED, eventProperties)

        commentDialog.editComment?.setText("")
        val request = CommentFeedRequest(getUserId(), postId, comment)
        presenter?.commentFeed(this, postId, position, request, token)
    }

    private fun getListComment(postId: String) {
        val request = CommentListFeedRequest(postId, 1, getUserId())
        presenter?.getListComment(this, request)
    }

    private fun likeComment(positionPost: Int, positionComment: Int, commentId: String) {
        val request = LikeCommentRequest(getUserId(), commentId)
        presenter?.likeOnComment(this, positionPost, positionComment, request, token)
    }

    private fun showReplyCommentDialog(
        postId: String,
        posPost: Int,
        posComment: Int,
        item: CommentData,
        isReply: Boolean? = null
    ) {
        ReplyCommentListDialog.show(
            supportFragmentManager, item, getUserId(), postId, posPost, posComment, false, isReply,
            onOptionClick = {
                showDeleteCommentDialog(postId, posComment, posPost, item)
            },
            callbackLike = { totalLikeComment: String?, totalLikePost: String, likeStatus: String? ->
                totalLikeComment?.let { itemListComment[posComment].likes_count = totalLikeComment }
                if (!likeStatus.isNullOrEmpty()) {
                    itemListComment[posComment].like_status = likeStatus
                }
                adapterComment.notifyItemChanged(posComment)
            },
            callbackComment = { totalCommentPost: Int, totalParentComment: Int ->
                itemListComment[posComment].comments_count = totalParentComment.toString()
                adapterComment.notifyItemChanged(posComment)
            }
        )
    }

    private fun showDeleteCommentDialog(
        postId: String,
        positionComment: Int,
        positionPost: Int,
        item: CommentData
    ) {
        DeleteCommentDialog.show(supportFragmentManager) {
            presenter?.deleteComment(
                this,
                postId,
                positionComment,
                positionPost,
                DeleteCommentFeedRequest(getUserId(), item.comment_id)
                , token
            )
            itemListComment.remove(item)
            adapterComment.notifyDataSetChanged()
        }
    }

    private fun setBottomSheetComment() {
        commentDialog = BottomSheetDialog(this, R.style.DialogStyle)
        viewComment = layoutInflater.inflate(R.layout.comment_fragment, null)
        viewComment?.let {
            commentDialog.setContentView(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NavigationBarUtil.setWhiteNavigationBar(this, commentDialog)
        }

        val params = (viewComment?.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        val behavior = params.behavior
        (behavior as BottomSheetBehavior<*>).state = BottomSheetBehavior.STATE_EXPANDED
        commentDialog.currentFocus
    }

    private fun likeFeed(postId: String) {
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Post ID", dataFeed?.post_id)
            eventProperties.put("Post Description", dataFeed?.caption)
            eventProperties.put("Post Game Main Category", dataFeed?.game_name)
            eventProperties.put("Post Topic Main Category", dataFeed?.topic_name)
            eventProperties.put("Post Moderator", "admin")
            eventProperties.put("Post Creator", dataFeed?.publisher_name)
            eventProperties.put("Post Platform", dataFeed?.platform_name)
            eventProperties.put("Post Schedule", dataFeed?.publishing_schedule)
            eventProperties.put("Post Source", "Webview")
            eventProperties.put("Post Rank", dataFeed?.trending_id)
        } catch (exception: JSONException) {
        }
        tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_LIKED, eventProperties)

        val request = LikeFeedRequest(getUserId(), postId)
        presenter?.likeFeed(this, request, token)
    }

    override fun getTotalLike(result: LikeFeedResponse) {
        if (result.status != 200) showErrorDialog()
        val totalLike = result.total_post_likes
        buttonLike.text = totalLike
        if (result.data[0].like_status == "ACTIVE") {
            buttonLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
        } else {
            buttonLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlike, 0, 0, 0)
        }
    }

    override fun showErrorLike(throwable: Throwable) {
        showShortToast(throwable.message.toString())
    }

    override fun showLoadingComment() {
        commentDialog.progressBar?.toVisible()
        commentDialog.emptyComment?.toGone()
        commentDialog.listComment?.toGone()
    }

    override fun hideLoadingComment() {
        commentDialog.progressBar?.toGone()
    }

    override fun getDataComment(result: CommentResponse) {
        val data = result.data
        totalSizeComment = result.total_items
        totalPageComment = result.total_pages
        if (!data.isNullOrEmpty()) {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
            itemListComment.clear()
            itemListComment.addAll(data)
            adapterComment.totalSize = totalSizeComment
            adapterComment.notifyDataSetChanged()
        } else {
            itemListComment.clear()
            commentDialog.emptyComment?.toVisible()
            commentDialog.listComment?.toGone()
        }
        if (result.status != 200) showErrorDialog()
    }

    override fun showErrorComment(throwable: Throwable) {
        showShortToast(throwable.message.toString())
    }

    override fun getDataNextComment(result: CommentResponse) {
        val data = result.data
        if (!data.isNullOrEmpty()) {
            itemListComment.addAll(data)
            adapterComment.notifyDataSetChanged()
        }
        if (result.status != 200) showErrorDialog()
    }

    override fun getDataComment(position: Int, postId: String, result: CommentResponse) {
        val comment = result.data?.get(0)
        val totalComment = result.total_comments
        buttonComment.text = totalComment
        comment?.let {
            itemListComment.add(comment)
            adapterComment.notifyDataSetChanged()
            updateListComment(postId)
        }
        if (itemListComment.isNotEmpty()) {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
        }
        if (result.status != 200) showErrorDialog()
    }

    private fun updateListComment(postId: String) {
        currentPageComment = 1
        val request = CommentListFeedRequest(postId, 1, getUserId())
        presenter?.updateListComment(this, request)
    }

    private fun getUserId(): String {
        val data = TinyDB(this).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        if (data != null) {
            val userId = data.userId
            if (userId != null) return userId
            else return ""
        } else return ""
    }

    override fun getDeletedComment(
        postId: String,
        positionComment: Int,
        positionPost: Int,
        result: CommentResponse
    ) {
        if (itemListComment.isEmpty()) {
            commentDialog.emptyComment?.toVisible()
            commentDialog.listComment?.toGone()
        } else {
            commentDialog.emptyComment?.toGone()
            commentDialog.listComment?.toVisible()
        }
        val totalComment = result.total_comments
        buttonComment.text = totalComment
        supportFragmentManager.findFragmentByTag("ReplyCommentDialog.tag")?.let {
            (it as ReplyCommentListDialog).dismiss()
        }
        updateListComment(postId)
    }

    override fun getTotalLikeComment(
        positionPost: Int,
        positionComment: Int,
        result: LikeCommentResponse
    ) {
        if (result.status != 200) showErrorDialog()
        val totalLikeComment = result.total_comment_likes
        val likeStatus = result.data[0].like_status

        itemListComment[positionComment].likes_count = totalLikeComment
        itemListComment[positionComment].like_status = likeStatus
        adapterComment.notifyItemChanged(positionComment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionRefresh -> {
                init()
                true
            }
            R.id.actionOpenChrome -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
                true
            }
            R.id.actionCopyLink -> {
                val clipboard: ClipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("label", url)
                clipboard.setPrimaryClip(clip)
                showLongToast("Link Copied")
                true
            }
            R.id.actionClose -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun share() {
        if (dataFeed != null) {
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Post ID", dataFeed?.post_id)
                eventProperties.put("Post Description", dataFeed?.caption)
                eventProperties.put("Post Game Main Category", dataFeed?.game_name)
                eventProperties.put("Post Topic Main Category", dataFeed?.topic_name)
                eventProperties.put("Post Moderator", "admin")
                eventProperties.put("Post Creator", dataFeed?.publisher_name)
                eventProperties.put("Post Platform", dataFeed?.platform_name)
                eventProperties.put("Post Schedule", dataFeed?.publishing_schedule)
                eventProperties.put("Post Source", "Webview")
                eventProperties.put("Post Rank", dataFeed?.trending_id)
            } catch (exception: JSONException) {
            }
            tracker?.logEvent(AnalyticsTrackerConstant.PARAM_POST_SHARED, eventProperties)
        }

        val finalUrl: String = if (baseUrl != null) {
            if (baseUrl!!.contains("?")) {
                "$baseUrl&utm_source=spin&utm_medium=post&utm_campaign=shared-page"
            } else {
                "$baseUrl?&utm_source=spin&utm_medium=post&utm_campaign=shared-page"
            }
        } else {
            url.toString()
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                finalUrl
            )
        }
        startActivity(intent)
    }

    private fun showProgressBar() {
        frameProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        frameProgressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun sendContact(number: String) {
        webView.evaluateJavascript("javascript: getContact(\"$number\")", null)
    }

    private fun showErrorConnection() {
        if (errorConnectionImage != null) {
            imageError.setImageBitmap(errorConnectionImage)
        } else {
            imageError.setImageResource(R.drawable.ic_internet_connection_problem)
        }
        if (!errorConnectionText.isNullOrBlank()) {
            textError.text = errorConnectionText
        } else {
            textError.text = getString(R.string.web_view_text_error_connection)
        }
        if (!errorConnectionTextButton.isNullOrBlank()) {
            buttonRetry.text = errorConnectionTextButton
        } else {
            buttonRetry.text = getString(R.string.web_view_button_retry)
        }

        buttonRetry.visibility = if (isButtonRetryVisible) View.VISIBLE else View.GONE
    }

    private fun showErrorServer() {
        if (errorServerImage != null) {
            imageError.setImageBitmap(errorServerImage)
        } else {
            imageError.setImageResource(R.drawable.ic_server_error)
        }
        if (!errorServerText.isNullOrBlank()) {
            textError.text = errorServerText
        } else {
            textError.text = getString(R.string.web_view_text_error_server)
        }
        if (!errorServerTextButton.isNullOrBlank()) {
            buttonRetry.text = errorServerTextButton
        } else {
            buttonRetry.text = getString(R.string.web_view_button_retry)
        }

        buttonRetry.visibility = if (isButtonRetryVisible) View.VISIBLE else View.GONE
    }

    private fun hideError() {
        layoutError.visibility = View.GONE
    }
}

