package no.nav.helse.slack

import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.AbstractRedTeamTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoField

internal class SlackUpdaterTest: AbstractRedTeamTest() {

    @Test
    fun `updates user group and post message on slack in the morning on a workday`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        var testklokke = testklokke(8, 26)
        val updater = SlackUpdater({ LocalDateTime.now(testklokke) }, slackClient, redTeam())
        testklokke = testklokke(8, 27)

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
        var testklokke = testklokke(8, 26)
        val updater = SlackUpdater({ LocalDateTime.now(testklokke) }, slackClient, redTeam())
        testklokke = testklokke(9, 27)

        updater.update()

        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `post and update next day (testing the date check)`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        var testklokke = testklokke(7, 26)
        val updater = SlackUpdater({ LocalDateTime.now(testklokke) }, slackClient, redTeam())

        updater.update()
        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }

        testklokke = testklokke(8, 26)
        updater.update()
        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
        clearAllMocks()

        testklokke = testklokke(9, 26)
        updater.update()
        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }

        testklokke = testklokke(8, 27)
        updater.update()
        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
    }

    private fun testklokke(hour: Long, dato: Long): Clock {
        val offset = ZoneOffset.ofHours(1)
        return Clock.fixed(
            LocalDateTime.now().with(ChronoField.HOUR_OF_DAY, hour)
                .with(ChronoField.DAY_OF_MONTH, dato).toInstant(offset),
            ZoneId.ofOffset("", offset)
        )
    }
}
