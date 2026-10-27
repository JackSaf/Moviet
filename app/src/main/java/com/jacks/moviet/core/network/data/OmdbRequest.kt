package com.jacks.moviet.core.network.data

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.Response

internal suspend fun <T : Any, R> omdbRequest(
    request: suspend () -> Response<T>,
    mapResponse: (T) -> NetworkResult<R>,
): NetworkResult<R> = try {
    val response = request()
    if (!response.isSuccessful) {
        response.errorBody()?.close()
        NetworkResult.Failure(NetworkError.Http(response.code()))
    } else {
        response.body()?.let(mapResponse)
            ?: NetworkResult.Failure(NetworkError.InvalidResponse)
    }
} catch (_: SerializationException) {
    NetworkResult.Failure(NetworkError.InvalidResponse)
} catch (_: IOException) {
    NetworkResult.Failure(NetworkError.Connection)
}
