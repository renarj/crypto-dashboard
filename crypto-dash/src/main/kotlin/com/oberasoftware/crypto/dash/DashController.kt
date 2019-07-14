package com.oberasoftware.crypto.dash

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping




@Controller
class DashController(private val tickerService: TickerService) {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(DashController::class.java.canonicalName)
    }

    @RequestMapping("/")
    fun dash(): String {
        log.info("Dashboard requested")

        val l = tickerService.retrieveTickers()
        for(i in l) {
            log.info("Got Pricing for ticker: {} prices: {}", i.id, i.snapshots)
        }

        return "index"
    }
}