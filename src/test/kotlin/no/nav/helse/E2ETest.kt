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
import no.nav.helse.model.Team
import no.nav.helse.model.TeamDto
import no.nav.helse.slack.SlackUpdater
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class E2ETest {

    @Test
    fun `red team member can be overriden`() = testApplication {
        val slackUpdater: SlackUpdater = mockk(relaxed = true)

        application {
            install(ContentNegotiation) {
                jackson()
            }
            val getRedTeam = {
                Team(
                    TeamDto("Speilvendt", listOf(MemberDto("Sondre", "slack1"), MemberDto("Jakob", "slack2"))),
                    TeamDto("Spleiselaget", listOf(MemberDto("Christian", "slack3"))),
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

        val response = client.post("/red-team/2022-01-03") {
            contentType(ContentType.Application.Json)
            setBody("""{ "from": "Sondre", "to": "Jakob" }""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val redTeam = client.get("/red-team/2022-01-03").bodyAsText()

        assertTrue("Jakob" in parsedTeam(redTeam))
    }


    private fun parsedTeam(json: String) = jacksonObjectMapper().readTree(json)["members"].map { it["name"].asText() }
}
