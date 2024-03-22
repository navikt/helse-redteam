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

        assertEquals(
            listOf(Team.TeamMember("Fag", "Fag 3", "slackid-Fag 3")),
            (redTeam.teamFor(date) as Workday).members
        )
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

    @Test
    fun `skal kunne overstyre alt via én enkelt json fil`() {
        val redTeam = RedTeam(startDato, team, holidays())
        val team = redTeam.teamFor(LocalDate.of(2024, 1, 3))
        assertEquals("David", (team as Workday).members.find { it.team == "Spleiselaget" }!!.name)

        val overridesFraBøtta = """{"2024-01-03":[{"team":"Spleiselaget","name":"Christian","slackId":"slackid-Christian"}]}"""
        redTeam.byttUtOverstyringer(overridesFraBøtta)

        val overriddenTeam = redTeam.teamFor(LocalDate.of(2024, 1, 3))
        assertEquals("Christian", (overriddenTeam as Workday).members.find { it.team == "Spleiselaget" }!!.name)
    }

    @Test
    fun `skal kunne bli overstyrt av den jsonen som den selv produserer`() {
        val overstyringsdag = LocalDate.of(2024, 1, 3)
        val redTeamAlpha = RedTeam(startDato, team, holidays())
        val teamAlpha = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals("David", teamAlpha.members.find { it.team == "Spleiselaget" }!!.name)

        redTeamAlpha.override("David", "Christian", overstyringsdag)
        val teamAlphaOverstyrt = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals("Christian", teamAlphaOverstyrt.members.find { it.team == "Spleiselaget" }!!.name)

        val redTeamBeta = RedTeam(startDato, team, holidays())
        val teamBeta = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals("David", teamBeta.members.find { it.team == "Spleiselaget" }!!.name)

        // HER SKJER DET VIKTIGE
        redTeamBeta.byttUtOverstyringer(redTeamAlpha.nyeOverstyringerSomJson())

        println(redTeamAlpha.nyeOverstyringerSomJson())

        val teamBetaOverstyrt = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals("Christian", teamBetaOverstyrt.members.find { it.team == "Spleiselaget" }!!.name)
    }

    @Test
    fun `siste per dag`() {
        val a_a = Team.TeamMember("a-team", "a-name", "a-slack")
        val a_b = Team.TeamMember("a-team", "b-name", "b-slack")
        val a_c = Team.TeamMember("a-team", "c-name", "c-slack")

        val b_a = Team.TeamMember("b-team", "a-name", "a-slack")
        val b_b = Team.TeamMember("b-team", "b-name", "b-slack")
        val b_c = Team.TeamMember("b-team", "c-name", "c-slack")

        val map:Map<LocalDate, List<Pair<Team.TeamMember, Team.TeamMember>>> = mapOf(
            1.januar() to listOf(a_a to a_b, a_b to a_c, b_b to b_a, b_a to b_c),
            2.januar() to listOf(a_a to a_b, a_b to a_c, a_b to a_a),
        )
        val actual = map.sistePerDag()
        assertEquals(mapOf(
            1.januar() to listOf(a_c, b_c),
            2.januar() to listOf(a_a),
        ), actual)
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
