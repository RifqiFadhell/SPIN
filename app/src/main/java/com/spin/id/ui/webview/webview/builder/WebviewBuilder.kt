package com.spin.id.ui.webview.webview.builder

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.spin.id.api.response.feed.list.FeedData
import com.spin.id.ui.webview.webview.WebActivity
import com.spin.id.ui.webview.WebConstant.DATA
import com.spin.id.ui.webview.webview.constant.AppSourceConstant
import com.spin.id.ui.webview.webview.constant.HttpMethodConstant

class WebviewBuilder(context: Context) {
    private val mContext: Context = context

    private var url: String? = null
    private var baseUrl: String? = null
    private var title: String? = null
    private var like: String? = null
    private var comment: String? = null
    private var token: String? = null
    private var customValue: String? = null
    private var customKey: String? = null
    private var clientSecret: String? = null
    private var errorConnectionText: String? = null
    private var errorConnectionTextButton: String? = null
    private var errorServerText: String? = null
    private var errorServerTextButton: String? = null
    private var versionCode: String = ""
    private var appSource: String = ""
    private var data: FeedData? = null

    private var layerType: Int = 0
    private var cacheMode: Int = 0
    private var httpMethod: Int = 0

    private var javascriptEnabled = true
    private var allowFileAccess = true
    private var allowContentAccess = true
    private var domStorageEnabled = true
    private var useTokenFromIntent = false
    private var isDebug = false
    private var isButtonRetryVisible = true

    private var listIntentFlag: MutableList<Int> = ArrayList()
    private var errorConnectionImage: Bitmap? = null
    private var errorServerImage: Bitmap? = null


    //  Mandatory
    fun setUrl(urlValue: String) = apply { this.url = urlValue }

    fun setTitle(title: String) = apply { this.title = title }

    fun setLike(like: String) = apply { this.like = like }

    fun setComment(comment: String) = apply { this.comment = comment }

    fun setBaseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }

    fun setHttpMethod(@HttpMethodConstant.Value httpMethod: Int) =
        apply { this.httpMethod = httpMethod }

    fun setDataFeed(data: FeedData) = apply { this.data = data }

    //  Mandatory Headers
    fun setHeaderAuthToken(token: String) = apply { this.token = token }

    //  Mandatory Headers For Firebase Log Event
    fun setVersionCode(versionCode: String) = apply { this.versionCode = versionCode }

    //  Mandatory Headers For Firebase Log Event
    fun setAppSource(@AppSourceConstant.Value appSource: String) =
        apply { this.appSource = appSource }

    fun setHeaderClientSecret(clientSecret: String) = apply { this.clientSecret = clientSecret }

    //  Additional Headers
    fun setHeaderCustomKey(customKey: String, customValue: String) = apply {
        this.customKey = customKey
        this.customValue = customValue
        this.useTokenFromIntent = true
    }

    fun setFlagForIntent(flag: Int) = apply {
        listIntentFlag.add(flag)
    }

    fun setIsDebug(isDebug: Boolean) = apply { this.isDebug = isDebug }

    //  Additional WebSetting
    fun setWebSettingLayerType(layerType: Int) = apply { this.layerType = layerType }

    fun setWebSettingCacheMode(cacheMode: Int) = apply { this.cacheMode = cacheMode }
    fun setWebSettingJavaScriptEnable(javascriptEnabled: Boolean) =
        apply { this.javascriptEnabled = javascriptEnabled }

    fun setWebSettingAllowFileAccess(allowFileAccess: Boolean) =
        apply { this.allowFileAccess = allowFileAccess }

    fun setWebSettingAllowContentAccess(allowContentAccess: Boolean) =
        apply { this.allowContentAccess = allowContentAccess }

    fun setWebSettingDomStorageEnabled(domStorageEnabled: Boolean) =
        apply { this.domStorageEnabled = domStorageEnabled }

    fun setErrorConnectionImage(bitmap: Bitmap) = apply { errorConnectionImage = bitmap }

    fun setErrorConnectionText(errorMessage: String) = apply { errorConnectionText = errorMessage }

    fun setErrorConnectionTextButton(errorTextButton: String) =
        apply { errorConnectionTextButton = errorTextButton }

    fun setErrorServerImage(bitmap: Bitmap) = apply { errorServerImage = bitmap }

    fun setErrorServerText(errorMessage: String) = apply { errorServerText = errorMessage }

    fun setErrorServerTextButton(errorTextButton: String) =
        apply { errorServerTextButton = errorTextButton }

    fun setVisibilityButtonRetry(isVisble: Boolean) = apply { this.isButtonRetryVisible = isVisble }

    fun show() {
        val dataModel = DataModel()
        dataModel.url = url
        dataModel.dataFeed = data
        dataModel.baseUrl = baseUrl
        dataModel.title = title
        dataModel.like = like
        dataModel.comment = comment
        dataModel.token = token
        dataModel.layerType = layerType
        dataModel.cacheMode = cacheMode
        dataModel.allowContentAccess = allowContentAccess
        dataModel.allowFileAccess = allowFileAccess
        dataModel.customKey = customKey
        dataModel.customValue = customValue
        dataModel.cacheMode = cacheMode
        dataModel.domStorageEnabled = domStorageEnabled
        dataModel.useTokenFromIntent = useTokenFromIntent
        dataModel.clientSecret = clientSecret
        dataModel.httpMethod = httpMethod
        dataModel.versionCode = versionCode
        dataModel.appSource = appSource
        dataModel.isDebug = isDebug
        dataModel.errorConnectionImage = errorConnectionImage
        dataModel.errorConnectionText = errorConnectionText
        dataModel.errorConnectionTextButton = errorConnectionTextButton
        dataModel.errorServerImage = errorServerImage
        dataModel.errorServerText = errorServerText
        dataModel.errorServerTextButton = errorServerTextButton
        dataModel.isButtonRetryVisible = isButtonRetryVisible

        val intent = Intent(mContext, WebActivity::class.java)
        intent.putExtra(DATA, dataModel)
        if (!listIntentFlag.isNullOrEmpty()) {
            for (i in listIntentFlag.indices) {
                if (i == 0) {
                    intent.flags = listIntentFlag[i]
                } else {
                    intent.flags = intent.flags or listIntentFlag[i]
                }
            }
        }
        mContext.startActivity(intent)
    }
}
