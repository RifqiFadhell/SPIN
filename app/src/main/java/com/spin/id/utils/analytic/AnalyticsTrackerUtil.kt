package com.spin.id.utils.analytic

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SOURCE_KEY
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SOURCE_VALUE

object AnalyticsTrackerUtil {

  private lateinit var analyticsTracker: FirebaseAnalytics

  fun init(context: Context) {
    analyticsTracker = FirebaseAnalytics.getInstance(context)
  }

  fun logScreenEvent(screenName: String) {
    val bundle = Bundle()
    bundle.putString(PARAM_SOURCE_KEY, PARAM_SOURCE_VALUE)
    analyticsTracker.logEvent(screenName, bundle)
  }
}