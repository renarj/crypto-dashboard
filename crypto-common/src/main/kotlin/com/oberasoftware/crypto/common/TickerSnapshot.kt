package com.oberasoftware.crypto.common

data class TickerSnapshot(val snapshotName: String, val time: Long, val close: Double, val volume: Double) {

}