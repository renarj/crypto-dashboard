package com.oberasoftware.crypto.producer.kraken

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.JsonParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KrakenAssetPair(_id: String, _altName: String, _basePair: String) {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(KrakenAssetPair::class.java.canonicalName)
    }

    val id: String = _id
    val altName: String = _altName
    private val basePair: String = _basePair

    class Deserializer: ResponseDeserializable<List<KrakenAssetPair>> {
        override fun deserialize(content: String): List<KrakenAssetPair>? {
            log.debug("Original content: '{}'", content)

            val root = JsonParser().parse(content).asJsonObject
            val result = root.get("result").asJsonObject

            val l = mutableListOf<KrakenAssetPair>()

            for(k in result.keySet().iterator()) {
                val pair = result.get(k).asJsonObject

                val altName = pair.getAsJsonPrimitive("altname").asString
                val base = pair.getAsJsonPrimitive("base").asString

                l.add(KrakenAssetPair(k, altName, base))
            }

            return l
        }
    }

    override fun toString(): String {
        return "KrakenAssetPair(id='$id', altName='$altName', basePair='$basePair')"
    }
}