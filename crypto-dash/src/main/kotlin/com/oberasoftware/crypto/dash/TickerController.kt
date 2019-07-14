package com.oberasoftware.crypto.dash

import com.oberasoftware.crypto.common.AssetTickerOverview
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TickerController(private val tickerService: TickerService) {
    @GetMapping("/tickers")
    fun retrieveTickers() : List<AssetTickerOverview> {
        return tickerService.retrieveTickers()
    }
}