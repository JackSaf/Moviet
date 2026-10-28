package com.jacks.moviet.sdk

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The app's provider-neutral entry point for advertising. Implementations own their ad resources. */
interface AdsService {
    /** Call once from Application.onCreate on the main thread; repeated calls must be harmless. */
    fun initialize(application: Application)

    /** Releases the placement's view on disposal; implementations may retain a bounded ad cache. */
    @Composable
    fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean)
}

enum class NativeAdSlot { FIRST, SECOND }
