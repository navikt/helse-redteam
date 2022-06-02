package no.nav.helse

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

fun main() = runBlocking { start() }

suspend fun start() {
    val logger = LoggerFactory.getLogger("helse-repos")
    val ktorServer = ktor()
    try {
        coroutineScope {
            launch {
                while (true) {
                    delay(1.minutes)
                }
            }
        }

    } finally {
        val gracePeriod = 5000L
        val forcefulShutdownTimeout = 30000L
        logger.info("shutting down ktor, waiting $gracePeriod ms for workers to exit. Forcing shutdown after $forcefulShutdownTimeout ms")
        ktorServer.stop(gracePeriod, forcefulShutdownTimeout)
        logger.info("ktor shutdown complete: end of life. goodbye.")
    }
}


fun ktor() = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
    configureRouting()
}.start(wait = false)


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("TBD red-team")
        }
        get("repos") {
            call.respondText("{}", ContentType.Application.Json)
        }

        get("isalive") {
            call.respondText("OK")
        }
    }
}