package com.primoz.swissborgandroidchallenge.network

import com.primoz.swissborgandroidchallenge.network.data.Ticker
import javax.inject.Inject

class BitFinexClient @Inject constructor(
    private val bitFinexAPI: BitFinexAPI
) {
    suspend fun getTickers() = kotlin.runCatching {
        val exampleSymbols = "tBTCUSD,tETHUSD,tCHSB:USD,tLTCUSD,tXRPUSD,tEOSUSD,tSANUSD,tDATUSD,tSNTUSD,tDOGE:USD"
        bitFinexAPI.getTickers(exampleSymbols).map {
            val data = it.asJsonArray
            Ticker(
                unformattedSymbol = data[0].asString,
                dailyChangePercentage = data[6].asFloat * 100,
                lastPrice = data[7].asFloat,
                volume = data[8].asFloat,
                high = data[9].asFloat,
                low = data[10].asFloat,
            )
        }
    }
}
