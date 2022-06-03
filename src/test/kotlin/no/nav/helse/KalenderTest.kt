package no.nav.helse

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

internal class KalenderTest {

    private val team = Team(listOf("Sondre", "David", "Christian"), listOf("Jakob", "Sindre"), listOf("Morten", "Cecilie"))
    private val startDato = LocalDate.of(2022, 1, 1)

    @Test
    fun next() {
        assertEquals(1.januar("Sondre", "Jakob", "Morten"), Kalender(startDato, team).next())
    }

    @Test
    fun override() {
        val kalender = Kalender(startDato, team)
        kalender.override("Morten", "Cecilie", 1.januar())
        kalender.override("Sondre", "David", 1.januar())
        assertEquals(1.januar("Jakob", "Cecilie", "David"), kalender.next())
        assertEquals(2.januar("David", "Sindre", "Cecilie"), kalender.next())
    }

    @Test
    fun `cannot override teams not containing the replacee`() {
        val kalender = Kalender(startDato, team)
        kalender.override("Cecilie", "Morten", 1.januar())
        assertThrows<IllegalArgumentException> { kalender.next() }
    }


    fun Int.januar(dev1: String, dev2: String, fag:String) =
        Dag(LocalDate.of(2022, 1, this), listOf(dev1, dev2, fag))

    fun Int.januar() =
        LocalDate.of(2022, 1, this)
}