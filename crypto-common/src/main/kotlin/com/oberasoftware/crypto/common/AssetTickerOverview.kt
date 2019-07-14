package com.oberasoftware.crypto.common

data class AssetTickerOverview(val id: String, val pairName: String, val current: TickerSnapshot, val snapshots: List<TickerSnapshot>) {

}