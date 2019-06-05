package com.oberasoftware.crypto.consumer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.google.gson.Gson
import com.oberasoftware.crypto.common.AssetPriceEvent
import java.util.Arrays
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

val log: Logger = LoggerFactory.getLogger(KafkaCryptoConsumer::class.java.canonicalName)

fun main(args: Array<String>) {
    CommandLineParser().main(args)
}

class CommandLineParser : CliktCommand() {
    private val topic: String by option(help="Kafka topic to publish to").default("crypto-topic")
    private val kafkaHost: String by option(help="Kafka bootstrap server to connect to").default("localhost")
    private val kafkaPort: Int by option(help = "Kafka bootstrap server port").int().default(9092)
    private val kafkaGroup: String by option(help="In case desired it can subscribe as part of a different Kafka group").default("crypto-tickers")

    private val influxHost: String by option(help = "InfluxDB Host to write series to").default("localhost")
    private val influxPort: Int by option(help="InfluxDB port to write series to").int().default(8086)
    private val influxUser: String by option(help="InfluxDB username for writing").default("root")
    private val influxPass: String by option(help="InfluxDB password for writing").default("root")
    private val influxDatabase: String by option(help="InfluxDB database name").default("cryptoseries")

    override fun run() {
        log.info("Starting Crypto Kafka Consumer with Kafka Host: {}:{} consuming from topic: {}", kafkaHost, kafkaPort, topic)

        val kafka = ConnectDetails(kafkaHost, kafkaPort, "", "")
        val influx = ConnectDetails(influxHost, influxPort, influxUser, influxPass)

        KafkaCryptoConsumer(kafka, topic, kafkaGroup, influx, influxDatabase).consume()
    }
}

class ConnectDetails(_host: String, _port: Int, _username: String, _password: String) {
    val host: String = _host
    val port: Int = _port
    val username: String = _username
    val password: String = _password
}

class KafkaCryptoConsumer(_kafkaDetails: ConnectDetails, _topic: String, _group: String, _influxDetails: ConnectDetails, _influxDatabase: String) {
    private val kafkaDetails: ConnectDetails = _kafkaDetails
    private val influxDetails: ConnectDetails = _influxDetails
    private val influxDatabase: String = _influxDatabase
    private val topic: String = _topic
    private val group: String = _group

    fun consume() {
        log.info("Starting consuming")

        val props = Properties()
        props["bootstrap.servers"] = "${kafkaDetails.host}:${kafkaDetails.port}"
        props["group.id"] = group
        props["enable.auto.commit"] = "true"
        props["auto.commit.interval.ms"] = "1000"
        props["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["value.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        val consumer = KafkaConsumer<String, String>(props)

        val influxClient = InfluxDBClient(influxDetails, influxDatabase)

        consumer.subscribe(Arrays.asList(topic))
        while (true) {
            val records = consumer.poll(Duration.ofMillis(1000))

            val l = mutableListOf<AssetPriceEvent>()
            for (record in records) {
                log.debug("Received Kafka event for: {} with value: {}", record.key(), record.value())

                val ap = Gson().fromJson<AssetPriceEvent>(record.value(), AssetPriceEvent::class.java)
                l.add(ap)
            }

            if (l.isNotEmpty()) {
                log.debug("Writing to InfluxDB {}", l.size)
                influxClient.publishAssetPrices(l)
            }
        }

    }
}