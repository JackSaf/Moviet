package com.jacks.moviet.sdk

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds

/** AdMob entry point. Provider types and resource ownership stay inside this module. */
class AdMobAdsService : AdsService {
    private var isInitialized = false
    private var isSdkReady by mutableStateOf(false)
    private val nativeAdStore = AdMobNativeAdStore()

    override fun initialize(application: Application) {
        if (isInitialized) return
        isInitialized = true
        val interstitialController = AdMobInterstitialController(application)
        application.registerActivityLifecycleCallbacks(interstitialController)
        application.registerActivityLifecycleCallbacks(nativeAdStore)
        val mainHandler = Handler(Looper.getMainLooper())
        Thread({
            MobileAds.initialize(application) {
                mainHandler.post {
                    isSdkReady = true
                    interstitialController.onSdkReady()
                }
            }
        }, "moviet-ads-init").start()
    }

    @Composable
    override fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean) {
        AdMobNativeAd(modifier = modifier, isSdkReady = isSdkReady, slot = slot, compact = compact, adStore = nativeAdStore)
    }
}
