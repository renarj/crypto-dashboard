package com.oberasoftware.crypto.producer.kraken

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.JsonParser
import com.oberasoftware.crypto.producer.AssetPrice
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KrakenTicker(_id: String, _altName: String, _btcPair: Boolean, _close: Double) {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(KrakenTicker::class.java.canonicalName)
    }

    val id: String = _id
    val altName: String = _altName
    val close: Double = _close
    val btcPair: Boolean = _btcPair

    override fun toString(): String {
        return "KrakenTicker(id='$id', altName='$altName', close=$close, btcPair=$btcPair)"
    }

    class Deserializer(_altNames: Map<String, String>) : ResponseDeserializable<List<KrakenTicker>> {
        private val altNames: Map<String, String> = _altNames

        override fun deserialize(content: String): List<KrakenTicker>? {
            log.debug("Original content: '{}'", content)

            val root = JsonParser().parse(content).asJsonObject
            val result = root.get("result").asJsonObject

            val l = mutableListOf<KrakenTicker>()

            for(key in result.keySet()) {
                val ticker = result.getAsJsonObject(key)
                val ask = ticker.getAsJsonArray("a")[0].asString.toDouble()
                val bid = ticker.getAsJsonArray("b")[0].asString.toDouble()
                val close = ticker.getAsJsonArray("c")[0].asString.toDouble()
                val volume = ticker.getAsJsonArray("v")[1].asString.toDouble()
                val low = ticker.getAsJsonArray("l")[1].asString.toDouble()
                val high = ticker.getAsJsonArray("h")[1].asString.toDouble()
                val altName = altNames.getOrDefault(key, key)

                val btcPair = altName.contains("XBT")

                l.add(KrakenTicker(key, altName, btcPair,close))
            }
            return l
        }
    }
}