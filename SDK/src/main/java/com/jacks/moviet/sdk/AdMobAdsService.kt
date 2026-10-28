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
    private var initialized = false
    private var ready by mutableStateOf(false)
    private val nativeAds = AdMobNativeAdStore()

    override fun initialize(application: Application) {
        if (initialized) return
        initialized = true
        val visits = PreferencesVisitStore(application.getSharedPreferences(PREFERENCES, 0))
        val interstitials = AdMobInterstitialController(application, SecondVisitPolicy(visits))
        application.registerActivityLifecycleCallbacks(interstitials)
        application.registerActivityLifecycleCallbacks(nativeAds)
        val mainHandler = Handler(Looper.getMainLooper())
        Thread({
            MobileAds.initialize(application) {
                mainHandler.post {
                    ready = true
                    interstitials.onSdkReady()
                }
            }
        }, "moviet-ads-init").start()
    }

    @Composable
    override fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean) {
        AdMobNativeAd(modifier = modifier, ready = ready, slot = slot, compact = compact, store = nativeAds)
    }
}

private const val PREFERENCES = "ads_visits"
