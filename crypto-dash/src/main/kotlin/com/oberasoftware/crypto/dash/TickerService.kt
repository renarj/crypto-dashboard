package com.oberasoftware.crypto.dash

import com.oberasoftware.crypto.common.AssetPriceEvent
import com.oberasoftware.crypto.common.AssetTickerOverview
import com.oberasoftware.crypto.common.TickerPrice
import com.oberasoftware.crypto.common.TickerSnapshot
import org.springframework.stereotype.Component

interface TickerService {

    fun retrieveTickers(timeFrames: List<String>):List<AssetTickerOverview>

    fun retrieveTickers():List<AssetTickerOverview>

    fun retrieveTickers(timeFrame: String): Map<String, TickerSnapshot>

    fun retrieveTicker(assetId: String, timeFrames: List<String>):AssetTickerOverview

    fun retrieveTicker(assetId: String):AssetTickerOverview
}