package com.oberasoftware.crypto.producer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.oberasoftware.crypto.producer.binance.BinanceClient
import com.oberasoftware.crypto.producer.kraken.KrakenClient
import com.oberasoftware.crypto.producer.kraken.log
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    CommandLineParser().main(args)
}

class CommandLineParser : CliktCommand() {
    private val topic: String by option(help="Kafka topic to publish to", envvar = "PRODUCER_TOPIC").default("crypto-topic")
    private val kafkaHost: String by option(help="Kafka bootstrap server to connect to", envvar = "KAFKA_HOST").default("localhost")
    private val kafkaPort: Int by option(help = "Kafka bootstrap server port", envvar = "KAFKA_PORT").int().default(9092)

    override fun run() {
        log.info("Starting Scraper to Kafka Producer with Kafka Host: {}:{} publishing to topic: {}", kafkaHost, kafkaPort, topic)

        val producer = CryptoKafkaProducer(topic, kafkaHost, kafkaPort)

        log.info("Starting threads for Binance and Kraken scrapers")
        val krakenRunner = Runnable { KrakenClient(producer).loop() }
        val binanceRunner = Runnable { BinanceClient(producer).loop() }

        val krakenThread = Thread(krakenRunner)
        val binanceThread = Thread(binanceRunner)

        krakenThread.start()
        binanceThread.start()

        krakenThread.join()
//
//        val threadPool = Executors.newFixedThreadPool(4)
//        threadPool.submit(krakenRunner)
//        threadPool.submit(binanceRunner)
//
//        threadPool.



        log.info("Threads started")
    }
}