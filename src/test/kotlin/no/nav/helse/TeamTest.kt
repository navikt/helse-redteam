package no.nav.helse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TeamTest {


    val team = Team(
        TeamDto("Spleiselaget", genTeam("Sondre", "David", "Christian")),
        TeamDto("Speilvendt", genTeam("Jakob", "Sindre")),
        TeamDto("Fag", genTeam("Morten", "Cecilie"))
    )

    @Test
    fun nextDay() {

        assertEquals(
            listOf("Sondre", "Jakob", "Morten"),
            team.teamAt(0).map { it.name }
        )
        assertEquals(
            listOf("David", "Sindre", "Cecilie"),
            team.teamAt(1).map { it.name }
        )
        assertEquals(
            listOf("Christian", "Jakob", "Morten"),
            team.teamAt(2).map { it.name }
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
