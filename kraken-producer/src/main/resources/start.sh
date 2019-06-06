#!/usr/bin/env bash

echo "Waiting for $KAFKA_HOST to be up and running"
while true; do
    nc -q 1 -w 5 $KAFKA_HOST $KAFKA_PORT 2>/dev/null && break
done

echo "Kafka is up and running"

java -Djava.security.egd=file:/dev/./urandom -jar kraken-producer.jar