package com.jacks.moviet.sdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun AdMobNativeAd(
    modifier: Modifier,
    isSdkReady: Boolean,
    slot: NativeAdSlot,
    compact: Boolean,
    adStore: AdMobNativeAdStore,
) {
    val context = LocalContext.current
    val rowHeight = (if (compact) COMPACT_HEIGHT else ROW_HEIGHT) *
        LocalDensity.current.fontScale.coerceAtLeast(1f)
    val ad = adStore.ad(slot)
    LaunchedEffect(isSdkReady, slot, adStore, ad) {
        if (isSdkReady && ad == null) adStore.load(context, slot)
    }

    // Keep the load effect composed even when the placement occupies no space.
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = ad != null,
        enter = expandVertically(
            animationSpec = tween(REVEAL_DURATION_MILLIS),
            expandFrom = Alignment.Top,
        ) + fadeIn(animationSpec = tween(REVEAL_DURATION_MILLIS)),
        // The store destroys expired ads immediately; never retain their views for an exit.
        exit = ExitTransition.None,
    ) {
        val loadedAd = ad ?: return@AnimatedVisibility
        key(loadedAd, compact) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(rowHeight),
                factory = { context -> createNativeAdView(context, compact).also { bindNativeAd(it, loadedAd, compact) } },
                onRelease = { it.destroy() },
            )
        }
    }
}

private const val REVEAL_DURATION_MILLIS = 300
private val ROW_HEIGHT = 152.dp
private val COMPACT_HEIGHT = 264.dp
