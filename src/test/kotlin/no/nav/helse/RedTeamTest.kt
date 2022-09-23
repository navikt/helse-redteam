package no.nav.helse

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

internal class RedTeamTest {

    private val team = Team(
        TeamDto("Spleiselaget", listOf("Sondre", "David", "Christian")),
        TeamDto("Speilvendt", listOf("Jakob", "Sindre")),
        TeamDto("Fag", listOf("Morten", "Cecilie"))
    )
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
        assertEquals(4.januar("David", "Sindre", "Cecilie"), kalender.teamFor(4.januar()))
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
        assertEquals(NonWorkday(2.januar(), "SUNDAY"), kalender.teamFor(2.januar()))
    }

    @Test
    fun `no red-team on holidays`() {
        val kalender = RedTeam(startDato, team, holidays())
        assertEquals(NonWorkday(1.januar(), "1. nyttårsdag"), kalender.teamFor(1.januar()))
    }


    fun Int.januar(dev1: String, dev2: String, fag: String) =
        Workday(
            LocalDate.of(2022, 1, this),
            listOf(
                Team.TeamMember("Spleiselaget", dev1),
                Team.TeamMember("Speilvendt", dev2),
                Team.TeamMember("Fag", fag)).sortedBy { it.team })

    fun Int.januar() =
        LocalDate.of(2022, 1, this)
}