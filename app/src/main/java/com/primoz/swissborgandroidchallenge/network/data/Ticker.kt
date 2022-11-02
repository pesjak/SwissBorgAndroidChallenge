package com.primoz.swissborgandroidchallenge.network.data

import com.primoz.swissborgandroidchallenge.R

data class Ticker(
    private val unformattedSymbol: String,
    val dailyChangeRelative: Float,
    val lastPrice: Float,
) {
    val symbol: String = unformattedSymbol
        .removePrefix("t")
        .replace(":", "")
        .replace("USD", "")

    val name: String
        get() {
            return when (unformattedSymbol) {
                "tBTCUSD" -> "Bitcoin"
                "tETHUSD" -> "Ethereum"
                "tCHSB:USD" -> "SwissBorg"
                "tLTCUSD" -> "LiteCoin"
                "tXRPUSD" -> "Ripple"
                "tEOSUSD" -> "EOS"
                "tSANUSD" -> "Santiment"
                "tDATUSD" -> "Datum"
                "tSNTUSD" -> "Status"
                "tDOGE:USD" -> "Dogecoin"
                else -> "UnkownCoin"
            }
        }

    val icon: Int
        get() {
            return when (unformattedSymbol) {
                "tBTCUSD" -> R.drawable.ic_btc
                "tETHUSD" -> R.drawable.ic_eth
                "tCHSB:USD" -> R.drawable.ic_chsb
                "tLTCUSD" -> R.drawable.ic_ltc
                "tXRPUSD" -> R.drawable.ic_xrp
                "tEOSUSD" -> R.drawable.ic_eos
                "tSANUSD" -> R.drawable.ic_san
                "tDATUSD" -> R.drawable.ic_dat
                "tSNTUSD" -> R.drawable.ic_snt
                "tDOGE:USD" -> R.drawable.ic_doge
                else -> R.drawable.ic_broken_image
            }
        }
}
