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
class AdMobAdsService : AdsService by ProviderAdsService(AdMobAdsProvider())

/** Only this adapter and its native views/cache know Google Mobile Ads types. */
class AdMobAdsProvider : AdsProvider {
    private var isSdkReady by mutableStateOf(false)
    private val nativeAdStore = AdMobNativeAdStore()

    private lateinit var application: Application

    override fun initialize(application: Application, onReady: () -> Unit) {
        this.application = application
        application.registerActivityLifecycleCallbacks(nativeAdStore)
        val mainHandler = Handler(Looper.getMainLooper())
        Thread({
            MobileAds.initialize(application) {
                mainHandler.post {
                    isSdkReady = true
                    onReady()
                }
            }
        }, "moviet-ads-init").start()
    }

    override fun loadInterstitial(onResult: (InterstitialAdHandle?) -> Unit) {
        loadAdMobInterstitial(application, onResult)
    }

    @Composable
    override fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean) {
        AdMobNativeAd(modifier = modifier, isSdkReady = isSdkReady, slot = slot, compact = compact, adStore = nativeAdStore)
    }
}
