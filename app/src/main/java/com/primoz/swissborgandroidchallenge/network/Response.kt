package com.primoz.swissborgandroidchallenge.network

import java.util.*

sealed class Response<out T> {
    object Loading : Response<Nothing>()

    data class Success<T>(
        var data: T
    ) : Response<T>()

    data class Error<T>(
        var key: String = UUID.randomUUID().toString(),
        var data: T? = null,
        val message: String
    ) : Response<T>()
}
