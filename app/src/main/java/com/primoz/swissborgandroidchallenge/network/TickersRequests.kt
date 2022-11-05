package com.primoz.swissborgandroidchallenge.network

import com.primoz.swissborgandroidchallenge.network.data.Ticker

interface TickersRequests {

    suspend fun getTickers(): Result<List<Ticker>>
}
