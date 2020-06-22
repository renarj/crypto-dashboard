package com.oberasoftware.crypto.producer.kraken

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.google.common.util.concurrent.Uninterruptibles.*
import com.oberasoftware.crypto.producer.AssetPrice
import com.oberasoftware.crypto.producer.CryptoKafkaProducer
import com.oberasoftware.crypto.producer.ICryptoProducer
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

val log: Logger = LoggerFactory.getLogger(KrakenClient::class.java.canonicalName)

class KrakenClient(_kafkaProducer: ICryptoProducer, requiresValidCerts: String) {
    private val kafkaProducer : ICryptoProducer = _kafkaProducer


    private companion object {
        const val ASSET_PAIRS_URL = "https://api.kraken.com/0/public/AssetPairs"
        const val BASE_TICKER_URL = "https://api.kraken.com/0/public/Ticker"
    }

    private val manager : FuelManager

    init {
        log.debug("Requires certification validation: {}", requiresValidCerts)
        if("true".equals(requiresValidCerts, true)) {
            manager = FuelManager()
        } else {
            log.debug("Disabling certification validation")

            val sc = SSLContext.getInstance("TLS")
            val verifier = HostnameVerifier { _, _ -> true }

            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            })
            sc.init(null, trustAllCerts, java.security.SecureRandom())

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(verifier)

            manager = FuelManager().apply {
                socketFactory = sc.socketFactory

                hostnameVerifier = verifier
            }
        }
    }

    fun loop() {
        val assetPairs = retrieveAssetPairs()
        val altNameMap = assetPairs.map { it.id to it.altName }.toMap()

        val requestPairs = assetPairs.map { it.altName }.toList()
        val pairs = requestPairs.joinToString(",")

        while(true) {
            val tickers = retrieveTicker(pairs, altNameMap)
            log.debug("Received {} tickers information: {} pushing to Kafka", tickers.size, tickers)

            kafkaProducer.publishTicker(tickers)

            sleepUninterruptibly(5, TimeUnit.SECONDS)
        }
    }

    fun retrieveAssetPairs() : List<KrakenAssetPair> {
        val l = mutableListOf<KrakenAssetPair>()

        runBlocking {
            val (_, _, result) = manager.get(ASSET_PAIRS_URL).responseObject(KrakenAssetPair.Deserializer())

            log.info("Got a list of asset pairs: {}", result.get())
            l.addAll(result.get())
        }

        return l
    }

    fun retrieveTicker(pairs: String, altNames: Map<String, String>) : List<AssetPrice> {
        val l = mutableListOf<AssetPrice>()
        runBlocking {
            val url = "$BASE_TICKER_URL?pair=$pairs"
            log.debug("Doing asset pair request: {}", url)

            val (_, _, result) = manager.get(url).responseObject(KrakenTicker.Deserializer(altNames))

            val r = result.get()
            l.addAll(r.map { it -> AssetPrice(it.id, "kraken", it.btcPair, it.altName, it.close) }.toMutableList())

            log.debug("Got ticker info: {}", l)
        }

        return l
    }
}