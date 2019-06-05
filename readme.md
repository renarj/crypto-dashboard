# Crypto Dashboard
This repository contains a small crypto exchange client (Kraken support for now) to read current prices and put these in a Kafka topic. There is a small consumer that picks up from this Kakfa topic and publishes this to a InfluxDB timeseries DB. From there it is easy to display this in a Grafana dashboard.

## Roadmap

* Add Kubernetes Helm chart for easily running this on your local desktop
* Add support for Coinbase and Binance
* Add configurable asset pairs to be tracked

## License
This software adheres to the MIT X11 license: Copyright (c) 2019 Obera Software

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

There is an additional requirement to the above MIT X11 License: Redistribution of this software in source or binary forms shall be free of all charges or fees to the recipient of this software.