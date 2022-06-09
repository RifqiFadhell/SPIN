package com.spin.id.ui.webview.webview.javascripinterface

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.spin.id.ui.webview.webview.WebActivity
import com.spin.id.ui.webview.WebConstant


abstract class GeneralJavaScriptInterface(
    private val activity: Activity, private val webView: WebView) {

    @JavascriptInterface
    fun closePage() {
        activity.runOnUiThread { activity.finish() }
    }

    @JavascriptInterface
    fun backPage() {
        activity.runOnUiThread {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                activity.finish()
            }
        }
    }

    @JavascriptInterface
    fun reloadPage() {
        webView.reload()
    }

    @JavascriptInterface
    fun redirectToHomeUrl() {
        WebActivity.redirectToHomeUrl = true
    }

    @JavascriptInterface
    fun closeWebView() {
        WebActivity.closeWebView = true
    }

    @JavascriptInterface
    fun openExternalWebBrowser(url: String) {
        var urlWeb = url
        if (!urlWeb.startsWith("http://") && !urlWeb.startsWith("https://")) {
            urlWeb = "http://$urlWeb"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWeb))
        activity.startActivity(intent)
    }

    @JavascriptInterface
    fun setSpecificUrlOnBack(
        url: String, httpMethodType: Int, headerKey: String, headerValue: String
    ) {
        WebActivity.redirectToHomeUrl = true
        WebActivity.homeUrl = url
        WebActivity.httpMethod = httpMethodType

        WebActivity.additionalHttpHeaders?.clear()
        WebActivity.additionalHttpHeaders?.set(headerKey, headerValue)
    }

    @JavascriptInterface
    fun goToSpecificUrl(url: String, httpMethodType: Int, headerKey: String, headerValue: String) {

        WebActivity.homeUrl = url
        WebActivity.httpMethod = httpMethodType

        WebActivity.additionalHttpHeaders?.clear()
        WebActivity.additionalHttpHeaders?.set(headerKey, headerValue)

        reloadHomeUrl()
    }

    private fun reloadHomeUrl() {

        when (WebActivity.httpMethod) {
            WebConstant.HTTP_METHOD_GET_VALUE -> {
                webView.post {
                    webView.clearHistory()
                    WebActivity.additionalHttpHeaders?.let {
                        webView.loadUrl(
                            WebActivity.homeUrl.toString(),
                            it
                        )
                    }
                }
            }
            WebConstant.HTTP_METHOD_POST_VALUE -> {
                webView.post {
                    webView.clearHistory()
                    webView.postUrl(WebActivity.homeUrl.toString(), ByteArray(0))
                }
            }
        }
    }
}