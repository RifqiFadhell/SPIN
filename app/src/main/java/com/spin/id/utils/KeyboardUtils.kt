package com.spin.id.utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager

internal object KeyboardUtils {

  fun hideKeyboard(context: Context?, view: View?) {
    if (context != null && view != null) {
      val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
  }

  fun showKeyboard(context: Context?, view: View?) {
    if (context != null && view != null) {
      val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
      inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
  }
}