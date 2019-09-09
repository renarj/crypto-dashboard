package com.oberasoftware.crypto.producer.binance

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.JsonParser
import com.oberasoftware.crypto.producer.kraken.KrakenTicker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BinancePrice(_id: String, _last: Double, _btcPair: Boolean) {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(BinancePrice::class.java.canonicalName)
    }

    val id: String = _id
    val last: Double = _last
    val btcPair: Boolean = _btcPair

    override fun toString(): String {
        return "BinancePrice(id='$id', last=$last, btcPair=$btcPair)"
    }

    class Deserializer : ResponseDeserializable<List<BinancePrice>> {
        override fun deserialize(content: String): List<BinancePrice>? {
            log.debug("Original content: '{}'", content)

            val root = JsonParser().parse(content).asJsonArray


            val l = mutableListOf<BinancePrice>()

            for(arrayElement in root) {
                val ticker = arrayElement.asJsonObject;
                val id = ticker["symbol"].asString.replace("BTC", "XBT")
                val last = ticker["price"].asString.toDouble()

                val btcPair = id.contains("XBT")

                l.add(BinancePrice(id, last, btcPair))
            }
            return l
        }
    }

}