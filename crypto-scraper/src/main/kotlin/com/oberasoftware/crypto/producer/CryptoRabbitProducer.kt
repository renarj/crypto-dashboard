package com.oberasoftware.crypto.producer

import com.google.gson.Gson
import com.oberasoftware.crypto.common.AssetPriceEvent
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xerial.snappy.buffer.DefaultBufferAllocator


class CryptoRabbitProducer(_topic: String, _host: String, _port: Int) : ICryptoProducer {
    private val topic: String = _topic
    private val host: String = _host
    private val port: Int = _port

    private companion object {
        val log: Logger = LoggerFactory.getLogger(CryptoRabbitProducer::class.java.canonicalName)
    }

    private val connection: Connection
    private val channel: Channel

    private val publishContext = newSingleThreadContext("publishContext")

    init {
        val factory = ConnectionFactory()
        factory.host = host
        factory.port = port
        connection = factory.newConnection()
        channel = connection.createChannel()
        channel.exchangeDeclare(topic, "fanout")
    }

    override fun publishTicker(tickers : List<AssetPrice>) = runBlocking{

        withContext(publishContext) {
            log.debug("Sending {} tickers to RabbitMQ", tickers.size)
            for(ticker in tickers) {
                val ap = AssetPriceEvent(ticker.id, ticker.exchange, ticker.pairName, ticker.btcPair, ticker.last)
                val json = Gson().toJson(ap)

                channel.basicPublish(topic, "", null, json.toByteArray(Charsets.UTF_8))
//            val r = ProducerRecord<String, String>(topic, ap.id, json)
//            producer.send(r)
            }
            log.debug("Completed sending ticker information to RabbitMQ")
        }
    }


}