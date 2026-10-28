package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/** Main-thread lifecycle adapter; caches at most one ad and never waits to display it. */
internal class AdMobInterstitialController(
    private val application: Application,
) : Application.ActivityLifecycleCallbacks {
    private val foregrounds = ForegroundTracker()
    private val visitPolicy = SecondVisitPolicy()
    private var createdActivityCount = 0
    private var isSdkReady = false
    private var isLoading = false
    private var cachedAd: InterstitialAd? = null
    private var adLoadedAtMillis = 0L
    private var shouldShowOnResume = false

    fun onSdkReady() {
        isSdkReady = true
        preload()
    }

    private fun preload() {
        if (!isSdkReady || isLoading || createdActivityCount == 0 || visitPolicy.isConsumed) return
        if (cachedAd != null && isFresh()) return
        cachedAd = null
        isLoading = true
        InterstitialAd.load(
            application,
            INTERSTITIAL_TEST_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    if (createdActivityCount > 0 && !visitPolicy.isConsumed) {
                        cachedAd = ad
                        adLoadedAtMillis = SystemClock.elapsedRealtime()
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    cachedAd = null
                    Log.w(TAG, "Interstitial load failed: ${error.code} ${error.message}")
                }
            },
        )
    }

    private fun isFresh(): Boolean = SystemClock.elapsedRealtime() - adLoadedAtMillis < MAX_AD_AGE_MILLIS

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createdActivityCount++
    }

    override fun onActivityStarted(activity: Activity) {
        if (foregrounds.onActivityStarted()) {
            shouldShowOnResume = visitPolicy.onForeground()
        }
        preload()
    }

    override fun onActivityResumed(activity: Activity) {
        if (!shouldShowOnResume) return
        shouldShowOnResume = false
        val ad = cachedAd?.takeIf { isFresh() }
        cachedAd = null
        if (ad == null || activity.isFinishing || activity.isDestroyed) return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown")
            }

            override fun onAdDismissedFullScreenContent() {
                ad.fullScreenContentCallback = null
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad.fullScreenContentCallback = null
                Log.w(TAG, "Interstitial show failed: ${error.code} ${error.message}")
            }
        }
        ad.show(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        foregrounds.onActivityStopped(activity.isChangingConfigurations)
        if (!foregrounds.isInForeground && !activity.isChangingConfigurations) shouldShowOnResume = false
    }

    override fun onActivityDestroyed(activity: Activity) {
        createdActivityCount--
        if (createdActivityCount != 0 || activity.isChangingConfigurations) return
        // Closing the last Activity ends the launch even if Android retains the process.
        visitPolicy.reset()
        shouldShowOnResume = false
        cachedAd = null
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}

private const val TAG = "MovietAds"
private const val INTERSTITIAL_TEST_UNIT = "ca-app-pub-3940256099942544/1033173712"
private const val MAX_AD_AGE_MILLIS = 60 * 60 * 1_000L
