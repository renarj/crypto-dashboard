#!/usr/bin/env bash

echo "Waiting for $INFLUXDB_HOST to be up and running"
while true; do
    nc -q 1 -w 5 $INFLUXDB_HOST $INFLUXDB_PORT 2>/dev/null && break
done

echo "InfluxDB is up and running"

java -Djava.security.egd=file:/dev/./urandom -jar crypto-dash.jar