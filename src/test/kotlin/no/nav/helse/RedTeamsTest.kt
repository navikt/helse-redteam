package no.nav.helse

import no.nav.helse.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RedTeamsTest {

    private val team: () -> Teams
        get() = {
            Teams(
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
        kalender.override("Cecilie", 3.januar())
        assertEquals(3.januar("Sondre", "Jakob", "Cecilie"), kalender.teamFor(3.januar()))
        kalender.override("Morten", 3.januar())
        assertEquals(3.januar("Sondre", "Jakob", "Morten"), kalender.teamFor(3.januar()))
        kalender.override("David", 3.januar())
        assertEquals(3.januar("David", "Jakob", "Morten"), kalender.teamFor(3.januar()))
        assertEquals(4.januar("David", "Sindre", "Cecilie"), kalender.teamFor(4.januar()))
    }

    @Test
    fun `override combined with updated team`() {
        val members = mutableListOf("Per", "Pål", "Askeladden")
        val redTeam = RedTeam(startDato, { Teams(TeamDto("Fag", genTeam(*members.toTypedArray()))) })
        val date = 4.januar()
        assertEquals(
            Workday(
                4.januar(),
                listOf(
                    Teams.DayTeam(
                        team = "Fag",
                        redteamMembers = listOf(Teams.RedTeamMember("Pål", "slackid-Pål", "Fag"))
                    )
                )
            ),
            (redTeam.teamFor(date) as Workday)
        )

        redTeam.override(to = "Askeladden", date)
        members.add(0, "Ny fagperson")

        assertEquals(
            Workday(
                4.januar(),
                listOf(
                    Teams.DayTeam(
                        team = "Fag",
                        redteamMembers = listOf(Teams.RedTeamMember("Askeladden", "slackid-Askeladden", "Fag"))
                    )
                )
            ),
            (redTeam.teamFor(date) as Workday)
        )
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
        assertEquals(listOf("David"), (team as Workday).teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })

        val overridesFraBøtta = """{"2024-01-03":[{"team":"Spleiselaget","name":"Christian","slackId":"slackid-Christian"}]}"""
        redTeam.byttUtDagbestemmelserFraFastlager(overridesFraBøtta)

        val overriddenTeam = redTeam.teamFor(LocalDate.of(2024, 1, 3))
        assertEquals(listOf("Christian"), (overriddenTeam as Workday).teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })
    }

    @Test
    fun `skal kunne bli overstyrt av den jsonen som den selv produserer`() {
        val overstyringsdag = LocalDate.of(2024, 1, 3)
        val redTeamAlpha = RedTeam(startDato, team, holidays())
        val teamAlpha = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("David"), teamAlpha.teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })

        redTeamAlpha.override("Christian", overstyringsdag)
        val teamAlphaOverstyrt = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("Christian"), teamAlphaOverstyrt.teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })


        val redTeamBeta = RedTeam(startDato, team, holidays())
        val teamBeta = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("David"), teamBeta.teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })

        // HER SKJER DET VIKTIGE
        redTeamBeta.byttUtDagbestemmelserFraFastlager(redTeamAlpha.dagbestemmelserSomJson())

        println(redTeamAlpha.dagbestemmelserSomJson())

        val teamBetaOverstyrt = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("Christian"), teamBetaOverstyrt.teams.find { it.team == "Spleiselaget" }!!.redteamMembers.map { it.name })

    }

    private fun Int.januar(dev1: String, dev2: String, fag: String) =
        Workday(
            date = LocalDate.of(2022, 1, this),
            teams = listOf(
                Teams.DayTeam(
                    team = "Spleiselaget",
                    redteamMembers = listOf(Teams.RedTeamMember(name = dev1, slackId = "slackid-$dev1", team = "Spleiselaget"))
                ),
                Teams.DayTeam(
                    team = "Speilvendt",
                    redteamMembers = listOf(Teams.RedTeamMember(name = dev2, slackId = "slackid-$dev2", team = "Speilvendt"))
                ),
                Teams.DayTeam(
                    team = "Fag",
                    redteamMembers = listOf(Teams.RedTeamMember(name = fag, slackId = "slackid-$fag", team = "Fag"))
                )
            ).sortedBy { it.team }
        )

    private fun Int.januar() =
        LocalDate.of(2022, 1, this)

}
