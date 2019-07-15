package com.oberasoftware.crypto.dash

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping




@Controller
class DashController {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(DashController::class.java.canonicalName)
    }

    @RequestMapping("/")
    fun dash(): String {
        log.debug("Dashboard requested")

        return "index"
    }
}