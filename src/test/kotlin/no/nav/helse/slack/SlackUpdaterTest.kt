package no.nav.helse.slack

import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.AbstractRedTeamTest
import no.nav.helse.model.NonWorkday
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

internal class SlackUpdaterTest: AbstractRedTeamTest() {

    @Test
    fun `updates user group and post message on slack in the morning on a workday`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val testklokke = MutableClock(tidspunkt(8, 26))
        val updater = SlackUpdater(testklokke, slackClient, redTeam())
        testklokke.nyttTidspunkt(8, 27)

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
        val testklokke = MutableClock(tidspunkt(8, 26))
        val updater = SlackUpdater(testklokke, slackClient, redTeam())
        testklokke.nyttTidspunkt(9, 27)

        updater.update()

        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `post and update next day (testing the date check)`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val testklokke = MutableClock(tidspunkt(7, 26))
        val updater = SlackUpdater(testklokke, slackClient, redTeam())

        updater.update()
        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }

        testklokke.nyttTidspunkt(8, 26)
        updater.update()
        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
        clearAllMocks()

        testklokke.nyttTidspunkt(9, 26)
        updater.update()
        verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 0) { slackClient.postRedTeam(any()) }

        testklokke.nyttTidspunkt(8, 27)
        updater.update()
        verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
        verify(exactly = 1) { slackClient.postRedTeam(any()) }
    }

    @Test
    fun `handles non-workdays correctly`() {
        val slackClient = mockk<RedTeamSlack>(relaxUnitFun = true)
        val starttidspunkt = tidspunkt(7, 23, 12)
        val testklokke = MutableClock(starttidspunkt)
        val updater = SlackUpdater(
            testklokke,
            slackClient,
            redTeam(listOf(NonWorkday(LocalDate.of(2022, 12, 26), "2. juledag")))
        )

        fun assertPoster(tidspunkt: Instant) {
            testklokke.nyttTidspunkt(tidspunkt)
            updater.update()
            verify(exactly = 1) { slackClient.updateRedTeamGroup(any()) }
            verify(exactly = 1) { slackClient.postRedTeam(any()) }
            clearAllMocks()
        }

        fun assertPosterIkke(tidspunkt: Instant) {
            testklokke.nyttTidspunkt(tidspunkt)
            updater.update()
            verify(exactly = 0) { slackClient.updateRedTeamGroup(any()) }
            verify(exactly = 0) { slackClient.postRedTeam(any()) }
        }

        assertPoster(tidspunkt(8, 23, 12))

        assertPosterIkke(tidspunkt(8, 24, 12)) // lørdag

        assertPosterIkke(tidspunkt(8, 25, 12)) // søndag (OG helligdag)

        assertPosterIkke(tidspunkt(8, 26, 12)) // 2. juledag

        assertPoster(tidspunkt(8, 27, 12))
    }

}
