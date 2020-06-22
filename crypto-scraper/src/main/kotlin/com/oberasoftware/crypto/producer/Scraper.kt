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
    private val topic: String by option(help="topic to publish to", envvar = "PRODUCER_TOPIC").default("crypto-topic")
    private val rbqHost: String by option(help="Message Topic Server Host", envvar = "RBQ_HOST").default("localhost")
    private val rbqPort: Int by option(help = "Message Topic Server port", envvar = "RBQ_PORT").int().default(5672)
    private val requiresValidCerts: String by option(help = "Bypass certificate validation", envvar = "VALID_CERTS").default("true")

    override fun run() {
        log.info("Starting Scraper to Producer with Host: {}:{} publishing to topic: {}", rbqHost, rbqPort, topic)

//        val producer = CryptoKafkaProducer(topic, kafkaHost, kafkaPort)
        val producer = CryptoRabbitProducer(topic, rbqHost, rbqPort)

        log.info("Starting threads for Binance and Kraken scrapers")
        val krakenRunner = Runnable { KrakenClient(producer, requiresValidCerts).loop() }
        val binanceRunner = Runnable { BinanceClient(producer).loop() }

        val krakenThread = Thread(krakenRunner)
        val binanceThread = Thread(binanceRunner)

        krakenThread.start()
        binanceThread.start()
        log.info("Threads started")

        //wait forever until system exit
        krakenThread.join()
    }
}