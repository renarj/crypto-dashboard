package com.oberasoftware.crypto.producer.kraken

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.kittinunf.fuel.Fuel
import com.google.common.util.concurrent.Uninterruptibles.*
import com.oberasoftware.crypto.producer.AssetPrice
import com.oberasoftware.crypto.producer.CryptoKafkaProducer
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val log: Logger = LoggerFactory.getLogger(KrakenClient::class.java.canonicalName)

class KrakenClient(_kafkaProducer: CryptoKafkaProducer) {
    private val kafkaProducer : CryptoKafkaProducer = _kafkaProducer

    private companion object {
        const val ASSET_PAIRS_URL = "https://api.kraken.com/0/public/AssetPairs"
        const val BASE_TICKER_URL = "https://api.kraken.com/0/public/Ticker"
    }

    fun loop() {
        val assetPairs = retrieveAssetPairs()
        val altNameMap = assetPairs.map { it.id to it.altName }.toMap()

        val requestPairs = assetPairs.map { it.altName }.toList()
        val pairs = requestPairs.joinToString(",")

        while(true) {
            val tickers = retrieveTicker(pairs, altNameMap)
            log.debug("Received {} tickers information: {} pushing to Kafka", tickers.size, tickers)

            kafkaProducer.publishTicker(tickers)

            sleepUninterruptibly(5, TimeUnit.SECONDS)
        }
    }

    fun retrieveAssetPairs() : List<KrakenAssetPair> {
        val l = mutableListOf<KrakenAssetPair>()

        runBlocking {
            val (_, _, result) = Fuel.get(ASSET_PAIRS_URL).responseObject(KrakenAssetPair.Deserializer())

            log.info("Got a list of asset pairs: {}", result.get())
            l.addAll(result.get())
        }

        return l
    }

    fun retrieveTicker(pairs: String, altNames: Map<String, String>) : List<AssetPrice> {
        val l = mutableListOf<AssetPrice>()
        runBlocking {
            val url = "$BASE_TICKER_URL?pair=$pairs"
            log.debug("Doing asset pair request: {}", url)

            val (_, _, result) = Fuel.get(url).responseObject(KrakenTicker.Deserializer(altNames))

            val r = result.get()
            l.addAll(r.map { it -> AssetPrice(it.id, "kraken", it.btcPair, it.altName, it.close) }.toMutableList())

            log.debug("Got ticker info: {}", l)
        }

        return l
    }
}