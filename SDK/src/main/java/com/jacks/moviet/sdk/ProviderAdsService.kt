package com.jacks.moviet.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shares the launch/return policy across providers; screens depend only on AdsService. */
class ProviderAdsService(private val provider: AdsProvider) : AdsService {
    private var isInitialized = false

    override fun initialize(application: Application) {
        if (isInitialized) return
        isInitialized = true
        val session = InterstitialSession(provider::loadInterstitial, SystemClock::elapsedRealtime)
        application.registerActivityLifecycleCallbacks(InterstitialLifecycle(session))
        provider.initialize(application, session::onSdkReady)
    }

    @Composable
    override fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean) {
        provider.NativeAd(modifier = modifier, slot = slot, compact = compact)
    }
}

private class InterstitialLifecycle(private val session: InterstitialSession) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = session.onActivityCreated()
    override fun onActivityStarted(activity: Activity) = session.onActivityStarted()
    override fun onActivityResumed(activity: Activity) {
        val ad = session.takeForDisplay() ?: return
        if (activity.isFinishing || activity.isDestroyed) ad.release() else ad.show(activity)
    }
    override fun onActivityStopped(activity: Activity) = session.onActivityStopped(activity.isChangingConfigurations)
    override fun onActivityDestroyed(activity: Activity) = session.onActivityDestroyed(activity.isChangingConfigurations)
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
