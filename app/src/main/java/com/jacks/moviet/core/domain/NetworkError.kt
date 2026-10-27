package com.jacks.moviet.core.domain

sealed interface NetworkError {
    data object InvalidRequest : NetworkError
    data object Connection : NetworkError
    data object InvalidResponse : NetworkError
    data class Http(val statusCode: Int) : NetworkError
    data class Omdb(val message: String?) : NetworkError
}
