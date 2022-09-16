package no.nav.helse

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

internal val mapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() = runBlocking { start() }



suspend fun start() {
    val logger = LoggerFactory.getLogger("red-team")
    val teamData = teamDataFromFile()
    val team = Team(teamData[0], teamData[1], teamData[2])

    val redTeam = RedTeam(LocalDate.of(2022, 6, 1), team, holidays())
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
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
}.start(wait = false)


fun Application.configureRouting(redTeam: RedTeam) {
    routing {
        get("/") {
            call.respondText("TBD red-team")
        }
        get("red-team") {
            val calender = redTeam.redTeamCalendar(LocalDate.now() to LocalDate.now().plusDays(30))
            val json = mapper.writeValueAsString(calender)
            call.respondText(json, ContentType.Application.Json)
        }
        get("red-team/{date}") {
            val date = LocalDate.parse(call.parameters["date"]) ?: throw IllegalArgumentException("missing parameter: <date>")
            val calender = redTeam.teamFor(date)
            val json = mapper.writeValueAsString(calender)
            call.respondText(json, ContentType.Application.Json)
        }
        post("red-team/{date}") {
            val date = LocalDate.parse(call.parameters["date"]) ?: throw IllegalArgumentException("missing parameter: <date>")
            val swap = call.receive<Swap>()
            redTeam.override(swap.from, swap.to, date)
            val response = mapper.writeValueAsString(redTeam.teamFor(date))
            call.respondText(response, ContentType.Application.Json)
        }

        get("isalive") {
            call.respondText("OK")
        }
    }
}

//val team = Team(
//    listOf("David", "Maxi", "Simen", "Håkon", "Hege", "Marthe", "Helene"),
//    listOf("Jakob", "Jonas","Øvind", "Stephen", "Sindre", "Eirik", "Christian", "Sondre", "Elias"),
//    listOf("Morten T", "Cecilie", "Asma", "Margrethe", "Rita", "Øystein", "Solveig", "Morten N")
//)