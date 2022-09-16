package no.nav.helse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TeamTest {



    @Test
    fun nextDay() {
        val team = Team("Spleiselaget" to listOf("Sondre", "David", "Christian"), "Speilvendt" to listOf("Jakob", "Sindre"), "Fag" to listOf("Morten", "Cecilie"))

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
        val team = Team("Spleiselaget" to listOf("Sondre", "David", "Christian"), "Speilvendt" to listOf("Jakob", "Sindre"), "Fag" to listOf("Morten", "Cecilie"))
        assertEquals(2, team.minLength())
    }

    @Test
    fun maxCycle() {
        val team = Team("Spleiselaget" to listOf("Sondre", "David", "Christian"), "Speilvendt" to listOf("Jakob", "Sindre"), "Fag" to listOf("Morten", "Cecilie"))
        assertEquals(3, team.maxLength())
    }
}