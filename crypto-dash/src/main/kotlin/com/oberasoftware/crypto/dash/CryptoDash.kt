package com.oberasoftware.crypto.dash

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

val log: Logger = LoggerFactory.getLogger(CryptoDash::class.java.canonicalName)

fun main(args: Array<String>) {
    CommandLineParser().main(args)
}

class CommandLineParser : CliktCommand() {
    private val topic: String by option(help="Kafka topic to publish to", envvar = "PRODUCER_TOPIC").default("crypto-topic")
    private val kafkaHost: String by option(help="Kafka bootstrap server to connect to", envvar = "KAFKA_HOST").default("localhost")
    private val kafkaPort: Int by option(help = "Kafka bootstrap server port", envvar = "KAFKA_PORT").int().default(9092)

    override fun run() {
        log.info("Starting Crypto Dashboard with {} {} {}", kafkaHost, kafkaPort, topic)

        CryptoDash().run()
    }
}

@SpringBootApplication
open class CryptoDash {
    fun run() {
        SpringApplication.run(CryptoDash::class.java)
    }
}