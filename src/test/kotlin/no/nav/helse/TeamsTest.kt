package no.nav.helse

import no.nav.helse.model.MemberDto
import no.nav.helse.model.Teams
import no.nav.helse.model.TeamDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TeamsTest {


    val team = Teams(
        TeamDto("Utvikling", genTeam("Sondre", "David", "Christian")),
        TeamDto("Fag", genTeam("Morten", "Cecilie"))
    )

    @Test
    fun nextDay() {
        assertEquals(
            listOf("Sondre", "Morten"),
            team.teamAt(0).map { it.redteamMembers.single().name }
        )
        assertEquals(
            listOf("David", "Cecilie"),
            team.teamAt(1).map { it.redteamMembers.single().name }
        )
        assertEquals(
            listOf("Christian", "Morten"),
            team.teamAt(2).map { it.redteamMembers.single().name }
        )
    }

    @Test
    fun minCycle() {
        assertEquals(2, team.minLength())
    }

    @Test
    fun maxCycle() {
        assertEquals(3, team.maxLength())
    }
}

internal fun genTeam(vararg names: String) = names.map { MemberDto(it, "slackid-$it") }
