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
    private var createdActivityCount = 0
    private var cacheGeneration = 0

    fun ad(slot: NativeAdSlot): NativeAd? = slots.getValue(slot).ad

    fun load(context: Context, slot: NativeAdSlot) {
        val slotState = slots.getValue(slot)
        if (slotState.ad != null || slotState.isLoading) return
        slotState.isLoading = true
        slotState.shouldRetryLoad = false
        val requestGeneration = cacheGeneration
        AdLoader.Builder(context.applicationContext, NATIVE_TEST_UNIT)
            .forNativeAd { loadedAd ->
                if (requestGeneration != cacheGeneration) {
                    loadedAd.destroy()
                    return@forNativeAd
                }
                slotState.isLoading = false
                slotState.ad = loadedAd
                slotState.loadedAtMillis = SystemClock.elapsedRealtime()
                val expirationCallback = Runnable { evict(slotState) }
                slotState.expirationCallback = expirationCallback
                handler.postDelayed(expirationCallback, MAX_AGE_MILLIS)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (requestGeneration != cacheGeneration) return
                    slotState.isLoading = false
                    slotState.shouldRetryLoad = true
                }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun evict(slotState: SlotState) {
        slotState.expirationCallback?.let { handler.removeCallbacks(it) }
        slotState.expirationCallback = null
        slotState.ad?.destroy()
        slotState.ad = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createdActivityCount++
    }

    override fun onActivityStarted(activity: Activity) {
        // Handler delays pause during deep sleep; elapsed time still expires cached ads on return.
        val now = SystemClock.elapsedRealtime()
        for ((slot, slotState) in slots) {
            if (slotState.ad != null && now - slotState.loadedAtMillis >= MAX_AGE_MILLIS) evict(slotState)
            if (slotState.shouldRetryLoad) load(activity, slot)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        createdActivityCount--
        if (createdActivityCount != 0 || activity.isChangingConfigurations) return
        cacheGeneration++
        slots.values.forEach { slotState ->
            evict(slotState)
            slotState.isLoading = false
            slotState.shouldRetryLoad = false
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private class SlotState {
        var ad by mutableStateOf<NativeAd?>(null)
        var loadedAtMillis = 0L
        var isLoading = false
        var shouldRetryLoad = false
        var expirationCallback: Runnable? = null
    }
}

private const val MAX_AGE_MILLIS = 60 * 60 * 1_000L
private const val NATIVE_TEST_UNIT = "ca-app-pub-3940256099942544/2247696110"
