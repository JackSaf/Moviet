package com.jacks.moviet.sdk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun AdMobNativeAd(
    modifier: Modifier,
    ready: Boolean,
    slot: NativeAdSlot,
    compact: Boolean,
    store: AdMobNativeAdStore,
) {
    val context = LocalContext.current
    val rowHeight = (if (compact) COMPACT_HEIGHT else ROW_HEIGHT) *
        LocalDensity.current.fontScale.coerceAtLeast(1f)
    val ad = store.ad(slot)
    LaunchedEffect(ready, slot, store, ad) {
        if (ready && ad == null) store.load(context, slot)
    }

    // The slot has the same size before and after the ad loads.
    Box(modifier = modifier.fillMaxWidth().height(rowHeight)) {
        if (ad != null) {
            key(ad, compact) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context -> createNativeAdView(context, compact).also { bindNativeAd(it, ad, compact) } },
                    onRelease = { it.destroy() },
                )
            }
        }
    }
}

private val ROW_HEIGHT = 152.dp
private val COMPACT_HEIGHT = 264.dp
