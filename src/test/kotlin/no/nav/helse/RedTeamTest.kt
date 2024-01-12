package no.nav.helse

import no.nav.helse.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class RedTeamTest {

    private val team: () -> Team
        get() = {
            Team(
                TeamDto("Spleiselaget", genTeam("Sondre", "David", "Christian")),
                TeamDto("Speilvendt", genTeam("Jakob", "Sindre")),
                TeamDto("Fag", genTeam("Morten", "Cecilie"))
            )
        }
    private val startDato = LocalDate.of(2022, 1, 1)

    @Test
    fun teamAt() {
        assertEquals(3.januar("Sondre", "Jakob", "Morten"), RedTeam(startDato, team).teamFor(3.januar()))
    }

    @Test
    fun sequence() {
        assertEquals(
            listOf(
                3.januar("Sondre", "Jakob", "Morten"),
                4.januar("David", "Sindre", "Cecilie")
            ), RedTeam(startDato, team).redTeamCalendar(3.januar() to 4.januar()).days
        )
    }

    @Test
    fun override() {
        val kalender = RedTeam(startDato, team)
        kalender.override("Morten", "Cecilie", 3.januar())
        assertEquals(3.januar("Sondre", "Jakob", "Cecilie"), kalender.teamFor(3.januar()))
        kalender.override("Cecilie", "Morten", 3.januar())
        assertEquals(3.januar("Sondre", "Jakob", "Morten"), kalender.teamFor(3.januar()))
        kalender.override("Sondre", "David", 3.januar())
        assertEquals(3.januar("David", "Jakob", "Morten"), kalender.teamFor(3.januar()))
        assertEquals(4.januar("David", "Sindre", "Cecilie"), kalender.teamFor(4.januar()))
    }

    @Test
    fun `override combined with updated team`() {
        val fagTeam = mutableListOf("Fag 1", "Fag 2", "Fag 3")
        val redTeam = RedTeam(startDato, { Team(TeamDto("Fag", genTeam(*fagTeam.toTypedArray()))) })
        val date = 4.januar()
        assertEquals(
            listOf(Team.TeamMember("Fag", "Fag 2", "slackid-Fag 2")),
            (redTeam.teamFor(date) as Workday).members
        )

        redTeam.override(from = "Fag 2", to = "Fag 3", date)
        fagTeam.add(0, "Ny fagperson")

        // I skrivende stund feiler denne fordi overrides blir 'ugyldige' hvis rekkefølgen på teammedlemmene endres.
        assertThrows<AssertionError> {
            assertEquals(
                listOf(Team.TeamMember("Fag", "Fag 3", "slackid-Fag 3")),
                (redTeam.teamFor(date) as Workday).members
            )
        }
    }

    @Test
    fun `cannot override teams not containing the replacee`() {
        val kalender = RedTeam(startDato, team)
        assertThrows<IllegalArgumentException> { kalender.override("Cecilie", "Morten", 3.januar()) }
        kalender.teamFor(3.januar())
    }

    @Test
    fun `cannot override members not in the same group`() {
        val kalender = RedTeam(startDato, team)
        assertThrows<IllegalArgumentException> { kalender.override("Morten", "Sondre", 3.januar()) }
        kalender.teamFor(3.januar())
    }

    @Test
    fun `no red-team on weekends`() {
        val kalender = RedTeam(startDato, team)
        assertEquals(NonWorkday(2.januar()), kalender.teamFor(2.januar()))
    }

    @Test
    fun `no red-team on holidays`() {
        val kalender = RedTeam(startDato, team, holidays())
        assertEquals(NonWorkday(1.januar()), kalender.teamFor(1.januar()))
    }


    private fun Int.januar(dev1: String, dev2: String, fag: String) =
        Workday(
            LocalDate.of(2022, 1, this),
            listOf(
                Team.TeamMember("Spleiselaget", dev1, "slackid-$dev1"),
                Team.TeamMember("Speilvendt", dev2, "slackid-$dev2"),
                Team.TeamMember("Fag", fag, "slackid-$fag")
            ).sortedBy { it.team })

    private fun Int.januar() =
        LocalDate.of(2022, 1, this)

}
