package com.oberasoftware.crypto.producer

data class AssetPrice(val id: String, val exchange: String, val btcPair: Boolean, val pairName: String, val last: Double)