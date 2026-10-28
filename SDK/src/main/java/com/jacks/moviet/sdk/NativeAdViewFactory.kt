package com.jacks.moviet.sdk

import android.content.Context
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.MediaView

// AndroidView provides no parent at creation; Compose supplies the measured bounds.
@SuppressLint("InflateParams")
internal fun createNativeAdView(context: Context, compact: Boolean): NativeAdView =
    LayoutInflater.from(context).inflate(
        if (compact) R.layout.native_ad_compact else R.layout.native_ad,
        null,
    ) as NativeAdView

internal fun bindNativeAd(view: NativeAdView, ad: NativeAd, compact: Boolean) {
    val headline = view.findViewById<TextView>(R.id.ad_headline)
    val body = view.findViewById<TextView>(R.id.ad_body)
    val image = view.findViewById<ImageView>(R.id.ad_image)
    val media = view.findViewById<MediaView>(R.id.ad_media)
    val action = view.findViewById<Button>(R.id.ad_action)
    val mainImage = ad.images.firstOrNull()?.drawable
    val icon = ad.icon?.drawable

    view.headlineView = headline
    view.bodyView = body
    view.mediaView = media
    view.imageView = image.takeIf { ad.mediaContent == null && mainImage != null }
    view.iconView = image.takeIf { ad.mediaContent == null && mainImage == null && icon != null }
    view.callToActionView = action
    media.mediaContent = ad.mediaContent
    media.visibility = if (ad.mediaContent == null) View.GONE else View.VISIBLE
    headline.text = ad.headline
    headline.maxLines = if (compact) 1 else 2
    body.text = ad.body
    body.visibility = if (ad.body == null) View.GONE else View.VISIBLE
    image.setImageDrawable(mainImage ?: icon)
    image.visibility = if (ad.mediaContent != null || (mainImage == null && icon == null)) View.GONE else View.VISIBLE
    action.text = ad.callToAction
    action.visibility = if (ad.callToAction == null) View.GONE else View.VISIBLE
    // Google Mobile Ads handles clicks on registered assets; do not install listeners.
    view.setNativeAd(ad)
}
