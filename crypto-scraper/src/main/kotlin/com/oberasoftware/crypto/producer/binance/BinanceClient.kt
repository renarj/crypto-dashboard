package com.oberasoftware.crypto.producer.binance

import com.github.kittinunf.fuel.Fuel
import com.google.common.util.concurrent.Uninterruptibles
import com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly
import com.oberasoftware.crypto.producer.AssetPrice
import com.oberasoftware.crypto.producer.CryptoKafkaProducer
import com.oberasoftware.crypto.producer.kraken.KrakenClient
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val log: Logger = LoggerFactory.getLogger(BinanceClient::class.java.canonicalName)

class BinanceClient(_kafkaProducer: CryptoKafkaProducer) {
    private val kafkaProducer: CryptoKafkaProducer = _kafkaProducer

    private companion object {
        const val PRICE_URL = "https://api.binance.com/api/v3/ticker/price"
    }

    fun loop() {
        while(true) {
            val tickers = retrieveTicker()
            log.debug("Received {} tickers information: {} pushing to Kafka", tickers.size, tickers)

            kafkaProducer.publishTicker(tickers)
            sleepUninterruptibly(5, TimeUnit.SECONDS)
        }
    }

    fun retrieveTicker() : List<AssetPrice> {
        val l = mutableListOf<AssetPrice>()
        runBlocking {
            val url = "$PRICE_URL"
            log.debug("Requesting Binance prices: {}", url)

            val (_, _, result) = Fuel.get(url).responseObject(BinancePrice.Deserializer())

            val r = result.get()
            l.addAll(r.map { it -> AssetPrice(it.id, "binance", it.btcPair, it.id, it.last) }.toMutableList())

            log.debug("Got ticker info: {}", l)
        }

        return l
    }

}