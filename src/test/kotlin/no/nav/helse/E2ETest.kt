package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class E2ETest {

    @Test
    fun `red team member can be overriden`() = testApplication {
        application {
            install(ContentNegotiation) {
                jackson()
            }
            configureRouting(RedTeam(LocalDate.of(2022, 1, 1), Team(listOf("Sondre", "Jakob"), listOf("Christian"), listOf("Margrethe"))))
        }

        val response = client.post("/red-team/2022-01-03") {
            contentType(ContentType.Application.Json)
            setBody("""{ "from": "Sondre", "to": "Jakob" }""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val redTeam = client.get("/red-team/2022-01-03").bodyAsText()

        assertTrue( "Jakob" in parsedTeam(redTeam))
    }


    fun parsedTeam(json: String) = jacksonObjectMapper().readTree(json)["members"].map { it.asText() }
}