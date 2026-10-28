package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd

/** AdMob implementation. No AdMob types escape the SDK module. */
class AdMobAdsService : AdsService, Application.ActivityLifecycleCallbacks {
    private var initialized = false
    private var ready by mutableStateOf(false)
    private var interstitial: InterstitialAd? = null
    private var activeActivities = 0
    private var configurationChange = false
    private var showThisForeground = false
    private lateinit var returnPolicy: SecondReturnPolicy

    override fun initialize(application: Application) {
        if (initialized) return
        initialized = true
        val preferences = application.getSharedPreferences(PREFERENCES, 0)
        returnPolicy = SecondReturnPolicy(object : SecondReturnPolicy.VisitStore {
            override var foregroundCount: Int
                get() = preferences.getInt(FOREGROUND_COUNT, 0)
                set(value) { preferences.edit().putInt(FOREGROUND_COUNT, value).apply() }
            override var consumed: Boolean
                get() = preferences.getBoolean(CONSUMED, false)
                set(value) { preferences.edit().putBoolean(CONSUMED, value).apply() }
        })
        application.registerActivityLifecycleCallbacks(this)
        val mainHandler = Handler(Looper.getMainLooper())
        Thread {
            MobileAds.initialize(application) {
                mainHandler.post {
                    ready = true
                    if (!preferences.getBoolean(CONSUMED, false)) preloadInterstitial(application)
                }
            }
        }.start()
    }

    private fun preloadInterstitial(application: Application) {
        if (interstitial != null) return
        InterstitialAd.load(
            application,
            INTERSTITIAL_TEST_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            },
        )
    }

    override fun onActivityStarted(activity: Activity) {
        if (activeActivities++ != 0) return
        if (configurationChange) {
            configurationChange = false
            return
        }
        showThisForeground = returnPolicy.onForeground()
    }

    override fun onActivityResumed(activity: Activity) {
        if (!showThisForeground) return
        showThisForeground = false
        val ad = interstitial ?: return
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad.fullScreenContentCallback = null
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad.fullScreenContentCallback = null
            }
        }
        ad.show(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        activeActivities--
        if (activeActivities == 0) configurationChange = activity.isChangingConfigurations
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    @Composable
    override fun NativeAd(modifier: Modifier) {
        val context = androidx.compose.ui.platform.LocalContext.current
        var ad by remember { mutableStateOf<NativeAd?>(null) }
        DisposableEffect(context, ready) {
            if (!ready) return@DisposableEffect onDispose { }
            var disposed = false
            AdLoader.Builder(context, NATIVE_TEST_UNIT)
                .forNativeAd { loaded ->
                    if (disposed) loaded.destroy() else ad = loaded
                }
                .build()
                .loadAd(AdRequest.Builder().build())
            onDispose {
                disposed = true
                ad?.destroy()
                ad = null
            }
        }
        ad?.let { loaded ->
            AndroidView(
                modifier = modifier,
                factory = { createNativeAdView(it) },
                update = { bindNativeAd(it, loaded) },
            )
        }
    }
}

private const val PREFERENCES = "ads_visits"
private const val FOREGROUND_COUNT = "foreground_count"
private const val CONSUMED = "interstitial_consumed"
private const val INTERSTITIAL_TEST_UNIT = "ca-app-pub-3940256099942544/1033173712"
private const val NATIVE_TEST_UNIT = "ca-app-pub-3940256099942544/2247696110"
