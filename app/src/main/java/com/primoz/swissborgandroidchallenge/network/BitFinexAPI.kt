package com.primoz.swissborgandroidchallenge.network

import com.google.gson.JsonArray
import retrofit2.http.GET
import retrofit2.http.Query

interface BitFinexAPI {

    @GET("tickers")
    suspend fun getTickers(
        @Query("symbols") symbols: String = ""
    ): JsonArray
}
