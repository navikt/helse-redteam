package no.nav.helse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TeamTest {



    @Test
    fun nextDay() {
        val team = Team(listOf("Sondre", "David", "Christian"), listOf("Jakob", "Sindre"), listOf("Morten", "Cecilie"))

        assertEquals(
            listOf("Sondre", "Jakob", "Morten"),
            team.next()
        )
        assertEquals(
            listOf("David", "Sindre", "Cecilie"),
            team.next()
        )
        assertEquals(
            listOf("Christian", "Jakob", "Morten"),
            team.next()
        )
    }

    @Test
    fun minCycle() {
        val team = Team(listOf("Sondre", "David", "Christian"), listOf("Jakob", "Sindre"), listOf("Morten", "Cecilie"))
        assertEquals(2, team.minLength())
    }

    @Test
    fun maxCycle() {
        val team = Team(listOf("Sondre", "David", "Christian"), listOf("Jakob", "Sindre"), listOf("Morten", "Cecilie"))
        assertEquals(3, team.maxLength())
    }
}