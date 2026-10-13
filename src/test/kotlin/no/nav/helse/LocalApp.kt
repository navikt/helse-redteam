package no.nav.helse

import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Teams
import no.nav.helse.model.holidays
import no.nav.helse.slack.SlackUpdater
import java.time.LocalDate

// Starter Ktor-appen lokalt på http://localhost:8080 uten Slack og GCP-bøtte. Teamdata leses fra
// test-team-data.json. Ktor serverer frontenden fra static/ på classpath, akkurat som i prod, så kjør
// `pnpm run build` i frontend/ først (se frontend/README.md).
fun main(): Unit =
    runBlocking {
        val redTeam =
            RedTeam(
                LocalDate.of(2022, 6, 1),
                { Teams(*teamDataFromFile().toTypedArray()) },
                holidays(),
            )
        val slackUpdater: SlackUpdater = mockk(relaxed = true)
        val mediator = RedteamMediator(slackUpdater, redTeam, object : Bøtte {})
        ktor(mediator)
        awaitCancellation()
    }
