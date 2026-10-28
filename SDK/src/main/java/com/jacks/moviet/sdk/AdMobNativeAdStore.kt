package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

/** Main-thread cache: one ad per slot, retained across scrolling and configuration changes. */
internal class AdMobNativeAdStore : Application.ActivityLifecycleCallbacks {
    private val slots = NativeAdSlot.entries.associateWith { SlotState() }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var createdActivities = 0
    private var generation = 0

    fun ad(slot: NativeAdSlot): NativeAd? = slots.getValue(slot).ad

    fun load(context: Context, slot: NativeAdSlot) {
        val state = slots.getValue(slot)
        if (state.ad != null || state.loading) return
        state.loading = true
        val requestGeneration = generation
        AdLoader.Builder(context.applicationContext, NATIVE_TEST_UNIT)
            .forNativeAd { loaded ->
                if (requestGeneration != generation) {
                    loaded.destroy()
                } else {
                    state.loading = false
                    state.ad = loaded
                    state.loadedAt = SystemClock.elapsedRealtime()
                    val expiration = Runnable { evict(state) }
                    state.expiration = expiration
                    handler.postDelayed(expiration, MAX_AGE_MILLIS)
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (requestGeneration == generation) state.loading = false
                }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun evict(state: SlotState) {
        state.expiration?.let { handler.removeCallbacks(it) }
        state.expiration = null
        state.ad?.destroy()
        state.ad = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createdActivities++
    }

    override fun onActivityStarted(activity: Activity) {
        // Handler delays pause during deep sleep; elapsed time still expires cached ads on return.
        val now = SystemClock.elapsedRealtime()
        slots.values.filter { it.ad != null && now - it.loadedAt >= MAX_AGE_MILLIS }.forEach(::evict)
    }

    override fun onActivityDestroyed(activity: Activity) {
        createdActivities--
        if (createdActivities != 0 || activity.isChangingConfigurations) return
        generation++
        slots.values.forEach { state ->
            evict(state)
            state.loading = false
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private class SlotState {
        var ad by mutableStateOf<NativeAd?>(null)
        var loadedAt = 0L
        var loading = false
        var expiration: Runnable? = null
    }
}

private const val MAX_AGE_MILLIS = 60 * 60 * 1_000L
private const val NATIVE_TEST_UNIT = "ca-app-pub-3940256099942544/2247696110"
