package com.primoz.swissborgandroidchallenge.network

import com.primoz.swissborgandroidchallenge.network.data.Ticker
import javax.inject.Inject

class BitFinexClient @Inject constructor(
    private val bitFinexAPI: BitFinexAPI
) {
    suspend fun getTickers(
        searchQuery: String = ""
    ) = kotlin.runCatching {
        val exampleSymbols = "tBTCUSD,tETHUSD,tCHSB:USD,tLTCUSD,tXRPUSD,tEOSUSD,tSANUSD,tDATUSD,tSNTUSD,tDOGE:USD"
        bitFinexAPI.getTickers(exampleSymbols).map {
            Ticker(
                unformattedSymbol = it.asJsonArray[0].asString,
                dailyChangeRelative = it.asJsonArray[9].asFloat,
                lastPrice = it.asJsonArray[10].asFloat,
            )
        }
    }
}
