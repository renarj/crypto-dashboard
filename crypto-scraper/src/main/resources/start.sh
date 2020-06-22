#!/usr/bin/env bash

echo "Waiting for $RBQ_HOST to be up and running"
while true; do
    nc -q 1 -w 5 $RBQ_HOST $RBQ_PORT 2>/dev/null && break
done

echo "RBQ is up and running"

java -Djava.security.egd=file:/dev/./urandom -jar crypto-scraper.jar