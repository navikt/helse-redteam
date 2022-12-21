package no.nav.helse.slack

import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.AbstractRedTeamTest
import org.junit.jupiter.api.Test
import java.time.*
import java.time.temporal.ChronoField

internal class SlackUpdaterTest: AbstractRedTeamTest() {

    @Test
    fun `updates user group and post message on slack in the morning on a workday`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val updater = SlackUpdater({ START_DATE.atTime(8, 0) }, slackClient, redTeam())

        // Sjekk at det bare blir gjort en gang for dagen
        updater.update()
        updater.update()
        updater.update()

        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `does not update or post outside morning`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val updater = SlackUpdater({ START_DATE.atTime(9, 0) }, slackClient, redTeam())

        updater.update()

        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `post and update next day (testing the lock)`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        var testklokke = testklokke(8)
        val updater = SlackUpdater({ LocalDateTime.now(testklokke) }, slackClient, redTeam())

        updater.update()

        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }

        testklokke = testklokke(9)

        updater.update()

        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }

        testklokke = testklokke(8)

        updater.update()

        verify(exactly = 2) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 2) { slackClient.postRedTeam(any()) }
    }

    private fun testklokke(hour: Int): Clock {
        val offset = ZoneOffset.ofHours(1)
        return Clock.fixed(
            LocalDateTime.now().with(ChronoField.HOUR_OF_DAY, hour.toLong()).toInstant(offset),
            ZoneId.ofOffset("", offset)
        )
    }
}
