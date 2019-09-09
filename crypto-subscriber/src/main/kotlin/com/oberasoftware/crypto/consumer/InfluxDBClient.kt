package com.oberasoftware.crypto.consumer

import com.oberasoftware.crypto.common.AssetPriceEvent
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class InfluxDBClient(_connectDetails: ConnectDetails, _databaseName: String) {
    private val connectDetails: ConnectDetails = _connectDetails
    private val databaseName: String = _databaseName

    private companion object {
        val log: Logger = LoggerFactory.getLogger(InfluxDBClient::class.java.canonicalName)
    }

    val influxDB : InfluxDB

    init {
        log.info("Connecting to InfluxDB: {}:{} and creating DB: {}", connectDetails.host, connectDetails.port, databaseName)
        influxDB = InfluxDBFactory.connect("http://${connectDetails.host}:${connectDetails.port}", connectDetails.username, connectDetails.password)
        influxDB.query(Query("CREATE DATABASE $databaseName"))
        influxDB.setDatabase(databaseName)
        influxDB.enableBatch(BatchOptions.DEFAULTS)
    }

    fun publishAssetPrices(prices : List<AssetPriceEvent>) {
        log.debug("Publishing {} prices to InfluxDB", prices.size)

        for(price in prices) {

            influxDB.write(Point.measurement("crypto")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("name", price.id)
                    .tag("exchange", price.exchange)
                    .tag("btcPair", price.btcPair.toString())
                    .tag("pair", price.pairName)
                    .addField("last", price.last)
                    .build())
        }
    }
}