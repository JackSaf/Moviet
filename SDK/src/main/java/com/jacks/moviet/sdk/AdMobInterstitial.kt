package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

internal fun loadAdMobInterstitial(application: Application, onResult: (InterstitialAdHandle?) -> Unit) {
    InterstitialAd.load(application, INTERSTITIAL_TEST_UNIT, AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) = onResult(AdMobInterstitial(ad))
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Interstitial load failed: ${error.code}")
                onResult(null)
            }
        },
    )
}

private class AdMobInterstitial(private var ad: InterstitialAd?) : InterstitialAdHandle {
    override val maxAgeMillis = 60 * 60 * 1_000L

    override fun show(activity: Activity) {
        val loadedAd = ad ?: return
        ad = null
        loadedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { loadedAd.fullScreenContentCallback = null }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadedAd.fullScreenContentCallback = null
                Log.w(TAG, "Interstitial show failed: ${error.code}")
            }
        }
        loadedAd.show(activity)
    }

    override fun release() {
        ad?.fullScreenContentCallback = null
        ad = null // AdMob interstitials have no destroy API.
    }
}

private const val TAG = "MovietAds"
private const val INTERSTITIAL_TEST_UNIT = "ca-app-pub-3940256099942544/1033173712"
