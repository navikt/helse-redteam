package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.helse.model.MemberDto
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Teams
import no.nav.helse.model.TeamDto
import no.nav.helse.slack.SlackUpdater
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class E2ETest {

    @Test
    fun `red team members can be overriden`() = testApplication {
        val slackUpdater: SlackUpdater = mockk(relaxed = true)

        application {
            install(ContentNegotiation) {
                jackson()
            }
            val getRedTeam = {
                Teams(
                    TeamDto("Speilvendt", listOf(MemberDto("Elias", "slack1"), MemberDto("Jakob", "slack2"))),
                    TeamDto("Spleiselaget", listOf(MemberDto("Håkon", "slack3"), MemberDto("Amalie", "slack3"))),
                    TeamDto("Fag", listOf(MemberDto("Margrethe", "slack5")))
                )
            }
            configureRouting(
                RedteamMediator(
                    slackUpdater = slackUpdater,
                    redTeam = RedTeam(LocalDate.of(2022, 1, 1), getRedTeam)
                )
            )
        }

        val response = client.post("/red-team") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val redTeam = client.get("/red-team").bodyAsText()

        val redteamForDato = jacksonObjectMapper().readTree(redTeam)["days"].single { it["date"].asText() == "2025-01-07" }["teams"].flatMap { it["redteamMembers"] }.map { it["name"].asText() }
        assertEquals(setOf("Margrethe", "Håkon", "Amalie", "Elias"), redteamForDato.toSet())

        val redteamForDato2 = jacksonObjectMapper().readTree(redTeam)["days"].single { it["date"].asText() == "2025-01-08" }["teams"].flatMap { it["redteamMembers"] }.map { it["name"].asText() }
        assertEquals(setOf("Margrethe", "Håkon", "Jakob"), redteamForDato2.toSet())
    }

    @Language("JSON")
    val jsonBody = """
        [
          {
            "date": "2025-01-07",
            "team": "Spleiselaget",
            "redteamMembers": [
              "Håkon",
              "Amalie"
            ]
          },
          {
            "date": "2025-01-07",
            "team": "Fag",
            "redteamMembers": ["Margrethe"]
          },
          {
            "date": "2025-01-07",
            "team": "Speilvendt",
            "redteamMembers": [
              "Elias"
            ]
          },
        {
          "date": "2025-01-08",
          "team": "Spleiselaget",
          "redteamMembers": [
            "Håkon"
          ]
        }
        ]
        """.trimIndent()
}
