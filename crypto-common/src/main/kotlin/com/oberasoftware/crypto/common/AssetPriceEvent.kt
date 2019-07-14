package com.oberasoftware.crypto.common

class AssetPriceEvent(_id: String, _pairName: String, _ask: Double, _bid: Double, _close: Double, _volume24Hours: Double, _low24H: Double, _high24H: Double) {
    val id: String = _id
    val pairName: String = _pairName
    val ask: Double = _ask
    val bid: Double = _bid
    val close: Double = _close
    val volume: Double = _volume24Hours
    val low: Double = _low24H
    val high: Double = _high24H
}