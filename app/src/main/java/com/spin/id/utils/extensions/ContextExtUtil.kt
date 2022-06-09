package com.spin.id.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.spin.id.R
import com.spin.id.ui.webview.webview.builder.WebviewBuilder
import com.spin.id.ui.webview.webview.constant.AppSourceConstant
import com.spin.id.ui.webview.webview.constant.HttpMethodConstant
import kotlinx.android.synthetic.main.general_error_layout.*

fun Context.getScreenWidthInDPs(): Int {
    val dm = DisplayMetrics()
    val windowManager = this.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(dm)
    return Math.round(dm.widthPixels / dm.density)
}

fun Context.getActionBarHeight(): Int {
    val textSizeAttr = intArrayOf(R.attr.actionBarSize)
    val a = this.obtainStyledAttributes(TypedValue().data, textSizeAttr)
    val height = a.getDimensionPixelSize(0, 0)
    a.recycle()
    return height
}

fun Context.showOkDialog(
    message: String,
    positiveText: String,
    listener: DialogInterface.OnClickListener?
) {
    val builder = AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(positiveText, listener)
        .setCancelable(false)
    val mAlertBuilder = builder.create()
    mAlertBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
    mAlertBuilder.show()
}

fun Context.showErrorDialog() {
    val builder = AlertDialog.Builder(this)
        .setView(R.layout.general_error_layout)
        .setCancelable(false)
    val mAlertBuilder = builder.create()
    mAlertBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
    mAlertBuilder.show()
    mAlertBuilder.buttonConfirm.setOnClickListener { mAlertBuilder.dismiss() }
}

fun Activity.showOkDialog(
    message: String,
    positiveText: String,
    listener: DialogInterface.OnClickListener?
) {
    val builder = AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(positiveText, listener)
        .setCancelable(false)
    val mAlertBuilder = builder.create()
    mAlertBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
    mAlertBuilder.show()
}

fun Activity.showToast(message: String, long: Int) {
    Toast.makeText(this, message, long).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showYesNoDialog(
    message: String, positiveText: String, negativeText: String,
    yesListener: DialogInterface.OnClickListener
) {
    lateinit var alertBuilder: AlertDialog
    val builder = AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(positiveText, yesListener)
        .setNegativeButton(negativeText) { _, _ -> alertBuilder.dismiss() }
    alertBuilder = builder.create()
    alertBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
    alertBuilder.show()
}

fun Context.signOutGoogle() {
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    mGoogleSignInClient?.signOut()?.addOnCompleteListener {
        Log.e("AUTHENTICATE", "Berhasil sign out")
    }
}

fun Context.signOutFacebook() {
    LoginManager.getInstance().logOut()
}

fun Context.goToWebView(url: String, title: String) {
    WebviewBuilder(this)
        .setUrl(url)
        .setBaseUrl(url)
        .setTitle(title)
        .setVersionCode("0")
        .setHttpMethod(HttpMethodConstant.Value.GET)
        .setIsDebug(true)
        .setAppSource(AppSourceConstant.Value.SPIN_ANDROID)
        .show()
}
