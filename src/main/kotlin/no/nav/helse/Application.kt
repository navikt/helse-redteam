package no.nav.helse

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.helse.model.Overstyring
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Teams
import no.nav.helse.model.holidays
import no.nav.helse.slack.RedTeamSlack
import no.nav.helse.slack.SlackUpdater
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private const val SLACK_CHANNEL = "team-sas"
private const val SLACK_USER_GROUP = "S010U3KQ8LQ"

internal val mapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() = runBlocking { start() }

suspend fun start() {
    val logger = LoggerFactory.getLogger("red-team")
    val slackToken =
        System.getenv("SLACK_BOT_USER_OAUTH_TOKEN") ?: error("Could not find slack token in envvar: SLACK_BOT_USER_OAUTH_TOKEN")
    val bøtte = GCPBøtte()
    val redTeam = setUpRedTeam()
    redTeam.byttUtDagbestemmelserFraFastlager(bøtte.hentOverstyringer())
    val mediator = RedteamMediator(
        SlackUpdater(
            Clock.systemDefaultZone(),
            RedTeamSlack(slackToken, SLACK_CHANNEL, SLACK_USER_GROUP),
            redTeam
        ), redTeam, bøtte
    )
    val ktorServer = ktor(mediator)
    try {
        coroutineScope {
            launch {
                while (true) {
                    try {
                        mediator.update()
                    } catch (e: Exception) {
                        logger.error("Error occurred during update", e)
                    }
                    logger.info("update loop waiting 10 min. hour: ${LocalDateTime.now().hour}")
                    delay(10.minutes)
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

private fun setUpRedTeam(): RedTeam {
    val teams = {
        val teamData = teamDataFromFile()
        Teams(*teamData.toTypedArray())
    }
    return RedTeam(LocalDate.of(2022, 6, 1), teams, holidays())
}

fun ktor(mediator: RedteamMediator) =
    embeddedServer(
        factory = CIO,
        environment = applicationEnvironment {},
        configure = {
            connector {
                port = 8080
            }
        }
    ) { redTeamModule(mediator) }
        .start(wait = false)

fun Application.redTeamModule(mediator: RedteamMediator) {
    configureRouting(mediator)
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
}

fun Application.configureRouting(mediator: RedteamMediator) {
    val logger = LoggerFactory.getLogger("red-team-api")
    routing {
        get("/") {
            call.respondText("TBD red-team")
        }
        get("red-team") {
            val calendar = mediator.redTeamCalendar(now() to now().plusDays(14)).json()
            call.respondText(calendar, ContentType.Application.Json)
        }
        get("red-team/{date}") {
            val date =
                LocalDate.parse(call.parameters["date"] ?: throw IllegalArgumentException("missing parameter: <date>"))
            val day = mediator.teamFor(date).json()
            call.respondText(day, ContentType.Application.Json)
        }
        post("red-team") {
            val overstyringerJson = call.receiveText()
            val overstyringer = mapper.readValue<List<Overstyring>>(overstyringerJson)

            try {
                mediator.override(overstyringer)
            } catch (e: IllegalArgumentException) {
                call.respondText(
                    """{"error": "${e.message}" }""",
                    ContentType.Application.Json,
                    HttpStatusCode.BadRequest
                )
                logger.error("Error during overriding red-team: {}", e.message)
                return@post
            }
            call.respondText("OK")
        }

        get("isalive") {
            call.respondText("OK")
        }
    }
}
