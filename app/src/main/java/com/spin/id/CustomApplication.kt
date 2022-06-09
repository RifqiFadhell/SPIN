package com.spin.id

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import com.readystatesoftware.chuck.ChuckInterceptor
import com.spin.id.api.ApiConstant
import com.spin.id.api.ApiFactory
import com.spin.id.utils.analytic.AnalyticsTrackerUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

class CustomApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        app = this

        val hostnameVerifier = HostnameVerifier { hostname, session -> true }

        Amplitude.getInstance().initialize(this, BuildConfig.AMPLITUDE_TOKEN)
            .enableForegroundTracking(app)

        AnalyticsTrackerUtil.init(this)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addNetworkInterceptor(ChuckInterceptor(app))
            .connectTimeout(ApiConstant.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstant.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstant.TIMEOUT, TimeUnit.SECONDS)
            .hostnameVerifier(hostnameVerifier)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        //TODO : UBAH BASE URL
        apiClient = Retrofit.Builder().baseUrl(BuildConfig.ENDPOINTS)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
            .create(ApiFactory::class.java)

        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                Log.d("LOG_TAG", "onConversionDataSuccess")
                for (attrName in conversionData.keys) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData[attrName])
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d("LOG_TAG", "error getting conversion data: $errorMessage")
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                Log.d("LOG_TAG", "onAppOpenAttribution")
                for (attrName in attributionData.keys) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData[attrName])
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d("LOG_TAG", "error onAttributionFailure : $errorMessage")
            }
        }

        AppsFlyerLib.getInstance().init(BuildConfig.AF_DEV_KEY, conversionListener, this)
        AppsFlyerLib.getInstance().start(this)
    }

    companion object {

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        var app: CustomApplication? = null
        var apiClient: ApiFactory? = null
        private lateinit var firebaseAnalytics: FirebaseAnalytics
    }
}