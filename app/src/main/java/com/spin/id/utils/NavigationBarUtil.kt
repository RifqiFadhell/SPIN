package com.spin.id.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi


class NavigationBarUtil {

  companion object {

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setWhiteNavigationBar(@NonNull activity: Activity, @NonNull dialog: Dialog) {
      val window = dialog.window

      if (window != null) {
        val metrics = DisplayMetrics()
        window.windowManager.defaultDisplay.getMetrics(metrics)

        val dimDrawable = GradientDrawable()
        // ...customize your dim effect here

        val navigationBarDrawable = GradientDrawable()
        navigationBarDrawable.shape = GradientDrawable.RECTANGLE
        navigationBarDrawable.setColor(activity.window.navigationBarColor)

        val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)

        val windowBackground = LayerDrawable(layers)
        windowBackground.setLayerInsetTop(1, metrics.heightPixels - 3)

        window.setBackgroundDrawable(windowBackground)
      }
    }
  }

}