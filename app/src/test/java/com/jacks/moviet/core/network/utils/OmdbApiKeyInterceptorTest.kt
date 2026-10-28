package com.jacks.moviet.core.network.utils

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OmdbApiKeyInterceptorTest {
    @Test
    fun `does not attach credentials to other hosts or insecure origins`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(OmdbApiKeyInterceptor(apiKey = "fixture-key"))
            .addInterceptor { chain ->
                assertNull(chain.request().url.queryParameter("apikey"))
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("Fixture")
                    .body("{}".toResponseBody())
                    .build()
            }.build()
        listOf(
            "https://images.example.com/poster.jpg",
            "https://www.omdbapi.com.example.com/",
            "http://www.omdbapi.com/",
            "https://www.omdbapi.com:8443/",
        ).forEach { url ->
            client.newCall(Request.Builder().url(url).build()).execute().close()
        }
    }

    @Test
    fun `adds one configured API key while preserving other query parameters`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(OmdbApiKeyInterceptor(apiKey = "fixture-key"))
            .addInterceptor { chain ->
                val request = chain.request()
                assertEquals(listOf("fixture-key"), request.url.queryParameterValues("apikey"))
                assertEquals("A & B", request.url.queryParameter("s"))
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("Fixture")
                    .body("{}".toResponseBody())
                    .build()
            }
            .build()
        listOf(
            "https://www.omdbapi.com/?s=A%20%26%20B",
            "https://www.omdbapi.com/?s=A%20%26%20B&apikey=old&apikey=duplicate",
        ).forEach { url ->
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                assertEquals(200, response.code)
            }
        }
    }
}
