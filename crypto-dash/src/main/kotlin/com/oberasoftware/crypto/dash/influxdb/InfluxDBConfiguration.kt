package com.oberasoftware.crypto.dash.influxdb

import org.influxdb.dto.Point
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.influxdb.DefaultInfluxDBTemplate
import org.springframework.data.influxdb.InfluxDBConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.influxdb.converter.PointConverter
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.data.influxdb.InfluxDBProperties


@Configuration
@EnableConfigurationProperties(InfluxDBProperties::class)
open class InfluxDBConfiguration {
    @Bean
    open fun connectionFactory(properties: InfluxDBProperties): InfluxDBConnectionFactory {
        return InfluxDBConnectionFactory(properties)
    }

    @Bean
    open fun influxDBTemplate(connectionFactory: InfluxDBConnectionFactory): InfluxDBTemplate<Point> {
        /*
     * You can use your own 'PointCollectionConverter' implementation, e.g. in case
     * you want to use your own custom measurement object.
     */
        return InfluxDBTemplate<Point>(connectionFactory, PointConverter())
    }

//    @Bean
//    open fun defaultTemplate(connectionFactory: InfluxDBConnectionFactory): DefaultInfluxDBTemplate {
//        /*
//     * If you are just dealing with Point objects from 'influxdb-java' you could
//     * also use an instance of class DefaultInfluxDBTemplate.
//     */
//        return DefaultInfluxDBTemplate(connectionFactory)
//    }
}