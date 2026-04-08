package no.nav.helse

import no.nav.helse.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RedTeamsTest {

    private val team: () -> Teams
        get() = {
            Teams(
                TeamDto("Utvikling", genTeam("Sondre", "David", "Christian")),
                TeamDto("Fag", genTeam("Morten", "Cecilie"))
            )
        }
    private val startDato = LocalDate.of(2022, 1, 1)

    @Test
    fun teamAt() {
        assertEquals(3.januar("Sondre", "Morten"), RedTeam(startDato, team).teamFor(3.januar()))
    }

    @Test
    fun sequence() {
        assertEquals(
            listOf(
                3.januar("Sondre", "Morten"),
                4.januar("David", "Cecilie")
            ), RedTeam(startDato, team).redTeamCalendar(3.januar() to 4.januar()).days
        )
    }

    @Test
    fun override() {
        val kalender = RedTeam(startDato, team)
        kalender.override(listOf("Cecilie"), "Fag", 3.januar())
        assertEquals(3.januar("Sondre", "Cecilie"), kalender.teamFor(3.januar()))
        kalender.override(listOf("Morten"), "Fag", 3.januar())
        assertEquals(3.januar("Sondre", "Morten"), kalender.teamFor(3.januar()))
        kalender.override(listOf("David"), "Utvikling", 3.januar())
        assertEquals(3.januar("David", "Morten"), kalender.teamFor(3.januar()))
        assertEquals(4.januar("David", "Cecilie"), kalender.teamFor(4.januar()))
    }

    @Test
    fun `two developers from Utvikling via override`() {
        val kalender = RedTeam(startDato, team)
        kalender.override(listOf("Sondre", "David"), "Utvikling", 3.januar())
        assertEquals(
            Workday(
                3.januar(),
                listOf(
                    Teams.DayTeam(
                        team = "Fag",
                        redteamMembers = listOf(Teams.RedTeamMember("Morten", "slackid-Morten", "Fag"))
                    ),
                    Teams.DayTeam(
                        team = "Utvikling",
                        redteamMembers = listOf(
                            Teams.RedTeamMember("Sondre", "slackid-Sondre", "Utvikling"),
                            Teams.RedTeamMember("David", "slackid-David", "Utvikling")
                        )
                    )
                ).sortedBy { it.team }
            ),
            kalender.teamFor(3.januar())
        )
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

        redTeam.override(listOf("Askeladden"), "Fag", date)
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
    fun `overrides for old team names from bucket are ignored, Fag override still applies`() {
        val overstyringsdag = LocalDate.of(2024, 1, 3)
        val redTeam = RedTeam(startDato, team, holidays())

        // Bucket contains overrides written when the three old teams existed
        val legacyOverrides = """
            {"$overstyringsdag":[
                {"team":"Fag","name":"Cecilie","slackId":"slackid-Cecilie"},
                {"team":"Spleiselaget","name":"Sondre","slackId":"slackid-Sondre"},
                {"team":"Speilvendt","name":"Jakob","slackId":"slackid-Jakob"}
            ]}
        """.trimIndent()
        redTeam.byttUtDagbestemmelserFraFastlager(legacyOverrides)

        val dag = redTeam.teamFor(overstyringsdag) as Workday
        // Fag override applies
        assertEquals(listOf("Cecilie"), dag.teams.find { it.team == "Fag" }!!.redteamMembers.map { it.name })
        // Utvikling has no override, falls back to natural rotation
        assertEquals(listOf("David"), dag.teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })
        // No ghost teams appear for the old names
        assertEquals(listOf("Fag", "Utvikling"), dag.teams.map { it.team }.sorted())
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
        assertEquals(listOf("David"), (team as Workday).teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })

        val overridesFraBøtta = """{"2024-01-03":[{"team":"Utvikling","name":"Christian","slackId":"slackid-Christian"}]}"""
        redTeam.byttUtDagbestemmelserFraFastlager(overridesFraBøtta)

        val overriddenTeam = redTeam.teamFor(LocalDate.of(2024, 1, 3))
        assertEquals(listOf("Christian"), (overriddenTeam as Workday).teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })
    }

    @Test
    fun `skal kunne bli overstyrt av den jsonen som den selv produserer`() {
        val overstyringsdag = LocalDate.of(2024, 1, 3)
        val redTeamAlpha = RedTeam(startDato, team, holidays())
        val teamAlpha = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("David"), teamAlpha.teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })

        redTeamAlpha.override(listOf("Christian"), "Utvikling", overstyringsdag)
        val teamAlphaOverstyrt = redTeamAlpha.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("Christian"), teamAlphaOverstyrt.teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })


        val redTeamBeta = RedTeam(startDato, team, holidays())
        val teamBeta = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("David"), teamBeta.teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })

        // HER SKJER DET VIKTIGE
        redTeamBeta.byttUtDagbestemmelserFraFastlager(redTeamAlpha.dagbestemmelserSomJson())

        println(redTeamAlpha.dagbestemmelserSomJson())

        val teamBetaOverstyrt = redTeamBeta.teamFor(overstyringsdag) as Workday
        assertEquals(listOf("Christian"), teamBetaOverstyrt.teams.find { it.team == "Utvikling" }!!.redteamMembers.map { it.name })

    }

    private fun Int.januar(dev: String, fag: String) =
        Workday(
            date = LocalDate.of(2022, 1, this),
            teams = listOf(
                Teams.DayTeam(
                    team = "Fag",
                    redteamMembers = listOf(Teams.RedTeamMember(name = fag, slackId = "slackid-$fag", team = "Fag"))
                ),
                Teams.DayTeam(
                    team = "Utvikling",
                    redteamMembers = listOf(Teams.RedTeamMember(name = dev, slackId = "slackid-$dev", team = "Utvikling"))
                )
            ).sortedBy { it.team }
        )

    private fun Int.januar() =
        LocalDate.of(2022, 1, this)

}
