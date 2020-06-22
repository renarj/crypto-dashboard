package com.oberasoftware.crypto.producer

interface ICryptoProducer {
    fun publishTicker(tickers : List<AssetPrice>)
}