package com.jacks.moviet.sdk

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

internal fun createNativeAdView(context: Context): NativeAdView =
    LayoutInflater.from(context).inflate(R.layout.native_ad, null) as NativeAdView

internal fun bindNativeAd(view: NativeAdView, ad: NativeAd) {
    val headline = view.findViewById<TextView>(R.id.ad_headline)
    val body = view.findViewById<TextView>(R.id.ad_body)
    val icon = view.findViewById<ImageView>(R.id.ad_icon)
    val action = view.findViewById<Button>(R.id.ad_action)

    view.headlineView = headline
    view.bodyView = body
    view.iconView = icon
    view.callToActionView = action
    headline.text = ad.headline
    body.text = ad.body
    body.visibility = if (ad.body == null) View.GONE else View.VISIBLE
    icon.setImageDrawable(ad.icon?.drawable)
    icon.visibility = if (ad.icon == null) View.GONE else View.VISIBLE
    action.text = ad.callToAction
    action.visibility = if (ad.callToAction == null) View.GONE else View.VISIBLE
    action.isEnabled = false // Google Mobile Ads handles clicks on registered assets.
    view.setNativeAd(ad)
}
