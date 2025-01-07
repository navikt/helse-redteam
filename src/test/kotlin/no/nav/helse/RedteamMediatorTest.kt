package no.nav.helse

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.model.Overstyring
import no.nav.helse.slack.RedTeamSlack
import no.nav.helse.slack.SlackUpdater
import org.junit.jupiter.api.Test

internal class RedteamMediatorTest: AbstractRedTeamTest() {

    @Test
    fun `same day override updates slackGroup`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)

        val mediator = RedteamMediator(
            slackUpdater = SlackUpdater(testklokke(START_DATE), slackClient, redTeam()),
            redTeam = redTeam()
        )
        mediator.override(listOf(Overstyring(START_DATE, "Spleiselaget", listOf("Sondre"))))
        verify { slackClient.updateRedTeamGroup(any()) }
    }

    @Test
    fun `not same day override does not trigger update to slackGroup`() {
        val slackClient = mockk<RedTeamSlack>()
        every { slackClient.updateRedTeamGroup(any()) } returns Unit

        val mediator = RedteamMediator(
            slackUpdater = SlackUpdater(testklokke(START_DATE), slackClient, redTeam()),
            redTeam = redTeam()
        )
        mediator.override(listOf(Overstyring(START_DATE.plusDays(1), "Spleiselaget", listOf("Sondre"))))
        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
    }
}


