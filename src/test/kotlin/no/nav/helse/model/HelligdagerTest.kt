package no.nav.helse.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class HelligdagerTest {
    // Alle datoer fra den tidligere helligdagskalender.json (2019–2027)
    private val tidligereKalender =
        """
        01.01.2019 14.04.2019 18.04.2019 19.04.2019 21.04.2019 22.04.2019 01.05.2019 17.05.2019 30.05.2019 09.06.2019 10.06.2019 25.12.2019 26.12.2019
        01.01.2020 05.04.2020 09.04.2020 10.04.2020 12.04.2020 13.04.2020 01.05.2020 17.05.2020 21.05.2020 31.05.2020 01.06.2020 25.12.2020 26.12.2020
        01.01.2021 28.03.2021 01.04.2021 02.04.2021 04.04.2021 05.04.2021 01.05.2021 13.05.2021 17.05.2021 23.05.2021 24.05.2021 25.12.2021 26.12.2021
        01.01.2022 10.04.2022 14.04.2022 15.04.2022 17.04.2022 18.04.2022 01.05.2022 17.05.2022 26.05.2022 05.06.2022 06.06.2022 25.12.2022 26.12.2022
        01.01.2023 02.04.2023 06.04.2023 07.04.2023 09.04.2023 10.04.2023 01.05.2023 17.05.2023 18.05.2023 28.05.2023 29.05.2023 25.12.2023 26.12.2023
        01.01.2024 24.03.2024 28.03.2024 29.03.2024 31.03.2024 01.04.2024 01.05.2024 09.05.2024 17.05.2024 19.05.2024 20.05.2024 25.12.2024 26.12.2024
        01.01.2025 13.04.2025 17.04.2025 18.04.2025 20.04.2025 21.04.2025 01.05.2025 17.05.2025 29.05.2025 08.06.2025 09.06.2025 25.12.2025 26.12.2025
        01.01.2026 29.03.2026 02.04.2026 03.04.2026 05.04.2026 06.04.2026 01.05.2026 14.05.2026 17.05.2026 24.05.2026 25.05.2026 25.12.2026 26.12.2026
        01.01.2027 21.03.2027 25.03.2027 26.03.2027 28.03.2027 29.03.2027 01.05.2027 06.05.2027 16.05.2027 17.05.2027 25.12.2027 26.12.2027
        """.trim()
            .split(Regex("\\s+"))
            .map { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }
            .toSet()

    @Test
    fun `gir samme helligdager som tidligere kalender for 2019 til 2027`() {
        val beregnet =
            LocalDate
                .of(2019, 1, 1)
                .datesUntil(LocalDate.of(2028, 1, 1))
                .filter { erHelligdag(it) }
                .toList()
                .toSet()
        assertEquals(tidligereKalender, beregnet)
    }

    @Test
    fun `første påskedag på tidligste og seneste mulige dato`() {
        assertEquals(LocalDate.of(2008, 3, 23), førstePåskedag(2008))
        assertEquals(LocalDate.of(2038, 4, 25), førstePåskedag(2038))
    }

    @Test
    fun `kirkens unntak for påskefullmånen`() {
        // Søndag etter fullmånen blir 26. april, som er etter siste mulige dato
        assertEquals(LocalDate.of(1981, 4, 19), førstePåskedag(1981))
        // Fullmånen er søndag 18. april
        assertEquals(LocalDate.of(1954, 4, 18), førstePåskedag(1954))
    }

    @Test
    fun `påskeberegningen avviser år utenfor 1900 til 2199`() {
        assertThrows<IllegalArgumentException> { førstePåskedag(1899) }
        assertThrows<IllegalArgumentException> { førstePåskedag(2200) }
    }

    @Test
    fun `julaften og nyttårsaften er ikke helligdager`() {
        assertFalse(erHelligdag(LocalDate.of(2026, 12, 24)))
        assertFalse(erHelligdag(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `første påskedag stemmer med den kompakte Meeus-Jones-Butcher-formelen for 1900 til 2199`() {
        (1900..2199).forEach { år -> assertEquals(meeusJonesButcher(år), førstePåskedag(år), "år $år") }
    }

    private fun meeusJonesButcher(år: Int): LocalDate {
        val a = år % 19
        val b = år / 100
        val c = år % 100
        val h = (19 * a + b - b / 4 - (b - (b + 8) / 25 + 1) / 3 + 15) % 30
        val l = (32 + 2 * (b % 4) + 2 * (c / 4) - h - c % 4) % 7
        val m = (a + 11 * h + 22 * l) / 451
        return LocalDate.of(år, 3, 22).plusDays((h + l - 7 * m).toLong())
    }
}
