package com.spin.id.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment


fun Activity.getScreenHeight(): Int {
    val metrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.heightPixels
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.setupActionBarWithBackButton(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
    }
}

fun Activity.goToActivity(cls: Class<*>, bundle: Bundle? = null) {
    if (bundle != null) {
        startActivity(Intent(this, cls).putExtras(bundle))
    } else {
        startActivity(Intent(this, cls))
    }
}