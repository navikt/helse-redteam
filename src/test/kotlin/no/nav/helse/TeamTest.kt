package no.nav.helse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TeamTest {


    val team = Team(
        TeamDto("Spleiselaget", listOf("Sondre", "David", "Christian")),
        TeamDto("Speilvendt", listOf("Jakob", "Sindre")),
        TeamDto("Fag", listOf("Morten", "Cecilie"))
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