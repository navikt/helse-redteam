package no.nav.helse

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

internal class RedTeamTest {

    private val team = Team(listOf("Sondre", "David", "Christian"), listOf("Jakob", "Sindre"), listOf("Morten", "Cecilie"))
    private val startDato = LocalDate.of(2022, 1, 1)

    @Test
    fun teamAt() {
        assertEquals(1.januar("Sondre", "Jakob", "Morten"), RedTeam(startDato, team).teamFor(1.januar()))
    }
    @Test
    fun sequence() {
        assertEquals(listOf(
            1.januar("Sondre", "Jakob", "Morten"),
            2.januar("David", "Sindre", "Cecilie")
        ), RedTeam(startDato, team).teamsFor(1.januar() to 2.januar()))
    }

    @Test
    fun override() {
        val kalender = RedTeam(startDato, team)
        kalender.override("Morten", "Cecilie", 1.januar())
        kalender.override("Sondre", "David", 1.januar())
        assertEquals(1.januar("Jakob", "Cecilie", "David"), kalender.teamFor(1.januar()))
        assertEquals(2.januar("David", "Sindre", "Cecilie"), kalender.teamFor(2.januar()))
    }

    @Test
    fun `cannot override teams not containing the replacee`() {
        val kalender = RedTeam(startDato, team)
        kalender.override("Cecilie", "Morten", 1.januar())
        assertThrows<IllegalArgumentException> { kalender.teamFor(1.januar()) }
    }


    fun Int.januar(dev1: String, dev2: String, fag:String) =
        Workday(LocalDate.of(2022, 1, this), listOf(dev1, dev2, fag))

    fun Int.januar() =
        LocalDate.of(2022, 1, this)
}