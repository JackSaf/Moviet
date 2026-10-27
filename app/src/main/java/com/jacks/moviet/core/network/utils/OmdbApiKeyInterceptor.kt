package com.jacks.moviet.core.network.utils

import com.jacks.moviet.core.network.OmdbConfig
import okhttp3.Interceptor
import okhttp3.Response

class OmdbApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .setQueryParameter(OmdbConfig.API_KEY_PARAMETER, apiKey)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}
