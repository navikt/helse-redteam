package no.nav.helse

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.slack.RedTeamSlack
import no.nav.helse.slack.SlackUpdater
import org.junit.jupiter.api.Test

internal class RedteamMediatorTest: AbstractRedTeamTest() {

    @Test
    fun `same day override updates slackGroup`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)

        val mediator = RedteamMediator(
            slackUpdater = SlackUpdater( { START_DATE.atStartOfDay()}, slackClient, redTeam()),
            redTeam = redTeam()
        )
        mediator.override("Sondre", "David", START_DATE)
        verify { slackClient.updateReadTeamGroup(any()) }
    }

    @Test
    fun `not same day override does not trigger update to slackGroup`() {
        val slackClient = mockk<RedTeamSlack>()
        every { slackClient.updateReadTeamGroup(any()) } returns Unit

        val mediator = RedteamMediator(
            slackUpdater = SlackUpdater( { START_DATE.atStartOfDay()}, slackClient, redTeam()),
            redTeam = redTeam()
        )
        mediator.override("David", "Sondre", START_DATE.plusDays(1))
        verify(exactly = 0) { slackClient.updateReadTeamGroup(any()) }
    }
}


