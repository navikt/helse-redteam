package no.nav.helse

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

internal val mapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() = runBlocking { start() }


suspend fun start() {
    val logger = LoggerFactory.getLogger("red-team")
    val slackToken = System.getenv("SLACK_TOKEN") ?: throw IllegalStateException("Cloud not find slack token in envvar: SLACK_TOKEN")
    val slackClient = RedTeamSlack(slackToken)
    val teamData = teamDataFromFile()
    val team = Team(teamData[0], teamData[1], teamData[2])

    val redTeam = RedTeam(LocalDate.of(2022, 6, 1), team, holidays())
    val ktorServer = ktor(redTeam)
    try {
        coroutineScope {
            launch {

                val postTime = 8
                var locked = false
                while (true) {
                    val redTeamForDay = redTeam.teamFor(now())
                    if (LocalDateTime.now().hour == postTime && (redTeamForDay is Workday) && !locked) {
                        // Skal flyttes ut i Naisjob etterhvert, try for å ikke ta ned resten av appen ved exceptions
                        try {
                            slackClient.postRedTeam(redTeamForDay)
                        } catch (e: Exception) {
                            logger.error("Error occurred attempting to post to slack API", e)
                        }
                        locked = true
                    } else if (LocalDateTime.now().hour != postTime && locked) {
                        locked = false
                    } else {
                        logger.info("slack client loop waiting 10 min. hour: ${LocalDateTime.now().hour}, locked: $locked")
                        delay(10.minutes)
                    }
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
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        filter { call ->
            !call.request.path().startsWith("/isalive")
        }
    }
}.start(wait = false)


fun Application.configureRouting(redTeam: RedTeam) {
    routing {
        get("/") {
            call.respondText("TBD red-team")
        }
        get("red-team") {
            val calender = redTeam.redTeamCalendar(now() to now().plusDays(30))
            val json = mapper.writeValueAsString(calender)
            call.respondText(json, ContentType.Application.Json)
        }
        get("red-team/{date}") {
            val date =
                LocalDate.parse(call.parameters["date"]) ?: throw IllegalArgumentException("missing parameter: <date>")
            val calender = redTeam.teamFor(date)
            val json = mapper.writeValueAsString(calender)
            call.respondText(json, ContentType.Application.Json)
        }
        post("red-team/{date}") {
            val date =
                LocalDate.parse(call.parameters["date"]) ?: throw IllegalArgumentException("missing parameter: <date>")
            val swapJson = call.receiveText()
            val swap = mapper.readValue<Swap>(swapJson)
            try {
                redTeam.override(swap.from, swap.to, date)
            } catch (e: IllegalArgumentException) {
                call.respondText("""{"error": "${e.message}" }""", ContentType.Application.Json, HttpStatusCode.BadRequest)
            }
            val response = mapper.writeValueAsString(redTeam.teamFor(date))
            call.respondText(response, ContentType.Application.Json)
        }

        get("isalive") {
            call.respondText("OK")
        }
    }
}