package com.jacks.moviet.sdk

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The app's provider-neutral entry point for advertising. */
interface AdsService {
    fun initialize(application: Application)

    @Composable
    fun NativeAd(modifier: Modifier)
}
