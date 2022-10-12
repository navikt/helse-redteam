package no.nav.helse.slack

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.AbstractRedTeamTest
import org.junit.jupiter.api.Test
import java.time.*

internal class SlackUpdaterTest: AbstractRedTeamTest() {

    @Test
    fun `updates user group and post message on slack in the morning on a workday`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val updater = SlackUpdater({ START_DATE.atTime(8, 0) }, slackClient, redTeam())

        // Sjekk at det bare blir gjort en gang for dagen
        updater.update()
        updater.update()
        updater.update()

        verify(exactly = 1) { slackClient.updateReadTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `does not update or post outside morning`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val updater = SlackUpdater({ START_DATE.atTime(9, 0) }, slackClient, redTeam())

        updater.update()

        verify(exactly = 0) { slackClient.updateReadTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `post and update next day (testing the lock)`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val clock = mockk<Clock>()
        every { clock.instant() } returns startDateAt(8).instant()
        every { clock.zone } returns  startDateAt(8).zone
        val updater = SlackUpdater({ LocalDateTime.now(clock) }, slackClient, redTeam())

        updater.update()

        verify(exactly = 1) { slackClient.updateReadTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }

        every { clock.instant() } returns startDateAt(9).instant()
        every { clock.zone } returns  startDateAt(9).zone

        updater.update()

        verify(exactly = 1) { slackClient.updateReadTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }

        every { clock.instant() } returns startDateAt(8).instant()
        every { clock.zone } returns  startDateAt(8).zone

        updater.update()

        verify(exactly = 2) { slackClient.updateReadTeamGroup(any()) }
        verify(exactly = 2) { slackClient.postRedTeam(any()) }

    }

    private fun startDateAt(hour: Int): Clock {
        val offset = ZoneOffset.ofHours(1)
        return Clock.fixed(START_DATE.atTime(hour, 0).toInstant(offset), ZoneId.ofOffset("", offset))
    }
}