package com.primoz.swissborgandroidchallenge

import com.primoz.swissborgandroidchallenge.network.TickersRequests
import com.primoz.swissborgandroidchallenge.network.data.Ticker

class TestTickersApi : TickersRequests {

    override suspend fun getTickers(): Result<List<Ticker>> {
        return Result.success(exampleList)
    }

    companion object {
        val exampleList = listOf(
            Ticker(
                unformattedSymbol = "tBTCUSD",
                dailyChangePercentage = 0.5f,
                lastPrice = 21000f,
                high = 22000f,
                low = 20000f
            ),
            Ticker(
                unformattedSymbol = "tDOGE:USD",
                dailyChangePercentage = -0.44f,
                lastPrice = 0.11f,
                high = 0.13f,
                low = 0.12f
            ),
            Ticker(
                unformattedSymbol = "tEOSUSD",
                dailyChangePercentage = -0.6f,
                lastPrice = 1.1f,
                high = 1.2f,
                low = 1f
            ),
            Ticker(
                unformattedSymbol = "tETHUSD",
                dailyChangePercentage = -1.35f,
                lastPrice = 1600f,
                high = 1670f,
                low = 1600f
            )
        )
    }
}
