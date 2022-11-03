package com.primoz.swissborgandroidchallenge.network

sealed class Response<out T> {
    object Loading : Response<Nothing>()

    data class Success<T>(
        var data: T
    ) : Response<T>()

    data class Error<T>(
        var data: T? = null,
        val message: String
    ) : Response<T>()
}
