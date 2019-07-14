package com.oberasoftware.crypto.dash.jasdb

import org.springframework.stereotype.Component
import com.oberasoftware.jasdb.core.utils.StringUtils.stringNotEmpty
import com.oberasoftware.jasdb.api.session.DBSession
import com.oberasoftware.jasdb.api.exceptions.JasDBStorageException
import com.oberasoftware.jasdb.rest.client.RestDBSession
import com.oberasoftware.jasdb.service.local.LocalDBSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value


@Component
class JasDBSessionFactory {
    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(JasDBSessionFactory::class.java.canonicalName)
    }

    @Value("\${jasdb.mode:local}")
    private val jasdbMode: String? = null

    @Value("\${jasdb.wipe.startup:false}")
    private val wipeStartup: Boolean = false

    @Value("\${jasdb.host:}")
    private val jasdbHost: String? = null

    @Value("\${jasdb.post:7050}")
    private val jasdbPort: Int = 0

    @Value("\${jasdb.instance:default}")
    private val jasdbInstance: String? = null


    @Throws(JasDBStorageException::class)
    fun createSession(): DBSession {
        val session: DBSession
        if (stringNotEmpty(jasdbMode) && jasdbMode == "rest") {
            LOG.debug("Creating JasDB REST session to host: {} port: {} instance: {}", jasdbHost, jasdbPort, jasdbInstance)
            session = RestDBSession(jasdbInstance, jasdbHost, jasdbPort)
        } else {
            LOG.debug("Creating JasDB Local session to instance: {}", jasdbInstance)
            session = LocalDBSession(jasdbInstance)
        }

        return session
    }
}