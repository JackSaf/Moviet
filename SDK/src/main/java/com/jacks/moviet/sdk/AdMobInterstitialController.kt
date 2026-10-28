package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/** Main-thread adapter between app lifecycle policy and AdMob's single-use ads. */
internal class AdMobInterstitialController(
    private val application: Application,
    private val visitPolicy: SecondVisitPolicy,
) : Application.ActivityLifecycleCallbacks {
    private val foregrounds = ForegroundTracker()
    private var ready = false
    private var loading = false
    private var interstitial: InterstitialAd? = null
    private var loadedAt = 0L
    private var showThisForeground = false

    fun onSdkReady() {
        ready = true
        preload()
    }

    private fun preload() {
        if (!ready || loading || visitPolicy.isConsumed) return
        if (interstitial != null && isFresh()) return
        interstitial = null
        loading = true
        InterstitialAd.load(
            application,
            INTERSTITIAL_TEST_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    // A late result must not retain an ad after its only display opportunity.
                    if (!visitPolicy.isConsumed) {
                        interstitial = ad
                        loadedAt = SystemClock.elapsedRealtime()
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitial = null
                }
            },
        )
    }

    private fun isFresh(): Boolean = SystemClock.elapsedRealtime() - loadedAt < MAX_AD_AGE_MILLIS

    override fun onActivityStarted(activity: Activity) {
        if (!foregrounds.onActivityStarted()) return
        showThisForeground = visitPolicy.onForeground()
        // Preload during the initial visit, never after the trigger.
        preload()
    }

    override fun onActivityResumed(activity: Activity) {
        if (!showThisForeground) return
        showThisForeground = false
        val ad = interstitial?.takeIf { isFresh() }
        interstitial = null
        if (ad == null || activity.isFinishing || activity.isDestroyed) return
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
        foregrounds.onActivityStopped(activity.isChangingConfigurations)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

private const val INTERSTITIAL_TEST_UNIT = "ca-app-pub-3940256099942544/1033173712"
private const val MAX_AD_AGE_MILLIS = 60 * 60 * 1_000L
