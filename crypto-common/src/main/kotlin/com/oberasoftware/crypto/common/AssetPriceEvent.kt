package com.oberasoftware.crypto.common

class AssetPriceEvent(_id: String, _exchange: String, _pairName: String, _btcPair: Boolean, _last: Double) {
    val id: String = _id
    val exchange: String = _exchange
    val pairName: String = _pairName
    val last: Double = _last
    val btcPair: Boolean = _btcPair
}