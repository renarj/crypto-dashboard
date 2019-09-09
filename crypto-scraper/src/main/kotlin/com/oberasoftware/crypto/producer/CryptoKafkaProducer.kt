package com.oberasoftware.crypto.producer

import com.google.gson.Gson
import com.oberasoftware.crypto.common.AssetPriceEvent
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


class CryptoKafkaProducer(_topic: String, _host: String, _port: Int) {
    private val topic: String = _topic
    private val host: String = _host
    private val port: Int = _port

    private companion object {
        val log: Logger = LoggerFactory.getLogger(CryptoKafkaProducer::class.java.canonicalName)
    }
    private val props = Properties()
    private val producer: KafkaProducer<String, String>

    init {
        props["bootstrap.servers"] = "$host:$port"
        props["acks"] = "0"
        props["retries"] = 0
        props["batch.size"] = 1
        props["linger.ms"] = 1
        props["buffer.memory"] = 33554432
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"

        producer = KafkaProducer(props)
    }

    fun publishTicker(tickers : List<AssetPrice>) {
        log.debug("Sending {} tickers to Kafka", tickers.size)
        for(ticker in tickers) {
            val ap = AssetPriceEvent(ticker.id, ticker.exchange, ticker.pairName, ticker.btcPair, ticker.last)
            val json = Gson().toJson(ap)

            val r = ProducerRecord<String, String>(topic, ap.id, json)
            producer.send(r)
        }
        log.debug("Completed sending ticker information to Kafka")
    }
}