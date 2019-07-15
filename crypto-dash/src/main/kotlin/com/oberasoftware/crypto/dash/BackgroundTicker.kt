package com.oberasoftware.crypto.dash

import com.google.common.util.concurrent.Uninterruptibles
import com.google.common.util.concurrent.Uninterruptibles.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class BackgroundTicker(private val tickerService: TickerService, private val messagingTemplate: SimpMessagingTemplate) : ApplicationListener<ContextRefreshedEvent>, Runnable {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(BackgroundTicker::class.java.canonicalName)
    }

    private val thread = Thread(this)

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        log.info("Starting background thread")
        thread.start()
    }

    override fun run() {
        while(!thread.isInterrupted) {
            val assets = tickerService.retrieveTickers()

            log.debug("Sending: {} tickers to websocket", assets.size)
            messagingTemplate.convertAndSend("/topic/tickers", assets)

            sleepUninterruptibly(5, TimeUnit.SECONDS)
        }
    }
}