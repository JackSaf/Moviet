package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Provider adapter. All calls and callbacks except SDK initialization run on the main thread. */
interface AdsProvider {
    /** Initialize once, then call onReady once on the main thread. Own native-ad lifecycle cleanup. */
    fun initialize(application: Application, onReady: () -> Unit)

    /** Complete each request once, with an owned ad or null on failure. Never retain an Activity. */
    fun loadInterstitial(onResult: (InterstitialAdHandle?) -> Unit)

    /** Own the placement's view, bounded native cache, expiry and disposal. */
    @Composable
    fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean)
}

/** Single-use ad. After show, the provider releases resources on dismissal or display failure. */
interface InterstitialAdHandle {
    val maxAgeMillis: Long
    fun show(activity: Activity)

    /** Release an unshown ad. Must be safe to call once without showing it. */
    fun release()
}
