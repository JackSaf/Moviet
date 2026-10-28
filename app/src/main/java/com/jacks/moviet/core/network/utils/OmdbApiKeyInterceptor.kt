package com.jacks.moviet.core.network.utils

import com.jacks.moviet.core.network.OmdbConfig
import okhttp3.Interceptor
import okhttp3.Response

class OmdbApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Keep credentials at the API boundary even if this client is accidentally
        // reused for a poster or another service.
        if (!request.url.isHttps || request.url.host != OmdbConfig.HOST || request.url.port != 443) {
            return chain.proceed(request)
        }
        val url = request.url.newBuilder()
            .setQueryParameter(OmdbConfig.API_KEY_PARAMETER, apiKey)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}
