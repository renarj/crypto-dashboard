package com.oberasoftware.crypto.dash.influxdb

import com.oberasoftware.crypto.common.AssetTickerOverview
import com.oberasoftware.crypto.common.TickerSnapshot
import com.oberasoftware.crypto.dash.TickerService
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.data.influxdb.InfluxDBTemplate
import java.time.ZonedDateTime
import org.influxdb.dto.BoundParameterQuery.QueryBuilder.newQuery as newQuery


@Component
class InfluxDBTickerService(private val template: InfluxDBTemplate<Point>) : TickerService {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(InfluxDBTickerService::class.java.canonicalName)

        val defaultTimeFrames:List<String> = listOf("1h", "4h", "12h", "24h", "7d")
    }

    override fun retrieveTickers(): List<AssetTickerOverview> {
        return retrieveTickers(defaultTimeFrames)
    }

    override fun retrieveTickers(timeFrames: List<String>): List<AssetTickerOverview> {

        val m = mutableMapOf<String, MutableList<TickerSnapshot>>()
        for(tf in timeFrames) {
            val mTF = retrieveTickers(tf)

            for(pair in mTF) {
                m.putIfAbsent(pair.key, mutableListOf())
                val l = m[pair.key]
                l?.add(pair.value)
            }
        }

        val latest = retrieveCurrentTickers()
        val l = mutableListOf<AssetTickerOverview>()
        for ((k, v) in m) {
            val latestValue = latest.getOrElse(k, { TickerSnapshot("unknown", 0, 0.0, 0.0) })

            l += AssetTickerOverview(k, k, latestValue, v)
        }

        return l.sortedByDescending { it.current.volume }
    }

    override fun retrieveTickers(timeFrame: String): Map<String, TickerSnapshot> {
        val query = newQuery("SELECT last(close), volume FROM crypto WHERE time < now() - $timeFrame group by pair")
                .forDatabase("cryptoseries")
                .create()

        return retrieveTickers(timeFrame, query)
    }

    private fun retrieveCurrentTickers() : Map<String, TickerSnapshot> {
        val query = newQuery("SELECT last(close), volume FROM crypto group by pair")
                .forDatabase("cryptoseries")
                .create()

        return retrieveTickers("latest", query)

    }

    private fun retrieveTickers(label: String, query: Query): Map<String, TickerSnapshot> {
        val result = template.query(query)
        log.debug("We got: {} results", result.results.size)

        val m = mutableMapOf<String, TickerSnapshot>()
        if(!result.hasError() && result.results.size > 0) {
            for(s in result.results[0].series) {
                val valid = s.values[0][0] != null && s.values[0][1] != null && s.values[0][2] != null

                if(s.tags.contains("pair") && valid) {
                    val pair = s.tags["pair"] as String

                    val time = s.values[0][0] as String
                    val zonedDateTime = ZonedDateTime.parse(time)

                    val close: Double = s.values[0][1] as Double
                    val volume = s.values[0][2] as Double

                    m[pair] = TickerSnapshot(label, zonedDateTime.toEpochSecond(), close, volume)
                    log.debug("Found a pair with ticker snapshot: {}", m[pair])
                } else {
                    log.warn("Result {} is invalid, missing pair or null result", s)
                }
            }

        }

        return m
    }

    override fun retrieveTicker(assetId: String, timeFrames: List<String>): AssetTickerOverview {
        return AssetTickerOverview("", "", TickerSnapshot("", 0, 0.0, 0.0), emptyList())
    }

    override fun retrieveTicker(assetId: String): AssetTickerOverview {
        return AssetTickerOverview("", "", TickerSnapshot("", 0, 0.0, 0.0), emptyList())
    }
}