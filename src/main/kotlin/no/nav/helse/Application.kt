package no.nav.helse

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

private val mapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() = runBlocking { start() }

suspend fun start() {
    val logger = LoggerFactory.getLogger("helse-repos")
    val redTeam = RedTeam(LocalDate.of(2022, 6, 1), team)
    val ktorServer = ktor(redTeam)
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


fun ktor(redTeam: RedTeam) = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
    configureRouting(redTeam)
}.start(wait = false)


fun Application.configureRouting(redTeam: RedTeam) {
    routing {
        get("/") {
            call.respondText("TBD red-team")
        }
        get("red-team") {
            val calender = redTeam.teamsFor(LocalDate.now() to LocalDate.now().plusDays(30))
            val json = mapper.writeValueAsString(calender)
            call.respondText(json, ContentType.Application.Json)
        }

        get("isalive") {
            call.respondText("OK")
        }
    }
}

val team = Team(
    listOf("David", "Maxi", "Simen", "Håkon", "Hege", "Christian", "Sondre"),
    listOf("Jakob", "Jonas", "Joakim", "Øvind", "Sindre", "Eirik"),
    listOf("Morten T", "Cecilie", "Asma", "Margrethe", "Rita", "Øystein", "Solveig", "Morten N")
)