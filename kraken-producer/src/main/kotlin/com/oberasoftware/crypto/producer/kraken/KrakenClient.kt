package com.oberasoftware.crypto.producer.kraken

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.kittinunf.fuel.Fuel
import com.google.common.util.concurrent.Uninterruptibles
import com.google.common.util.concurrent.Uninterruptibles.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

val log: Logger = LoggerFactory.getLogger(KrakenClient::class.java.canonicalName)

fun main(args: Array<String>) {
    CommandLineParser().main(args)
}

class CommandLineParser : CliktCommand() {
    private val topic: String by option(help="Kafka topic to publish to", envvar = "PRODUCER_TOPIC").default("crypto-topic")
    private val kafkaHost: String by option(help="Kafka bootstrap server to connect to", envvar = "KAFKA_HOST").default("localhost")
    private val kafkaPort: Int by option(help = "Kafka bootstrap server port", envvar = "KAFKA_PORT").int().default(9092)

    override fun run() {
        log.info("Starting Kraken to Kafka Producer with Kafka Host: {}:{} publishing to topic: {}", kafkaHost, kafkaPort, topic)
        KrakenClient(topic, kafkaHost, kafkaPort).loop()
    }
}

class KrakenClient(_topic: String, _kafkaHost: String, _kafkaPort: Int) {
    private val topic: String = _topic
    private val host: String = _kafkaHost
    private val port: Int = _kafkaPort

    private companion object {
        const val ASSET_PAIRS_URL = "https://api.kraken.com/0/public/AssetPairs"
        const val BASE_TICKER_URL = "https://api.kraken.com/0/public/Ticker"
    }

    fun loop() {
        val assetPairs = retrieveAssetPairs()
        val altNameMap = assetPairs.map { it.id to it.altName }.toMap()

        val requestPairs = assetPairs.map { it.altName }.toList()
        val pairs = requestPairs.joinToString(",")

        val producer = KrakenKafkaProducer(topic, host, port)

        while(true) {
            val tickers = retrieveTicker(pairs, altNameMap)
            log.debug("Received {} tickers information: {} pushing to Kafka", tickers.size, tickers)

            producer.publishTicker(tickers)

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

    fun retrieveTicker(pairs: String, altNames: Map<String, String>) : List<KrakenTicker> {
        val l = mutableListOf<KrakenTicker>()
        runBlocking {
            val url = "$BASE_TICKER_URL?pair=$pairs"
            log.debug("Doing asset pair request: {}", url)

            val (_, _, result) = Fuel.get(url).responseObject(KrakenTicker.Deserializer(altNames))

            log.debug("Got ticker info: {}", result)
            l.addAll(result.get())
        }

        return l
    }
}