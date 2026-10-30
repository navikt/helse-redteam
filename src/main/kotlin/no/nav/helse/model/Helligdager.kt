package no.nav.helse.model

import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.Month.APRIL
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import java.time.Month.MARCH
import java.time.Month.MAY
import java.time.MonthDay
import java.time.temporal.TemporalAdjusters

// Helligdager etter helligdagsfredloven § 2 og høytidsdager etter lov om 1. og 17. mai som offentlige høytidsdager.
private val fasteHelligdager =
    listOf(
        MonthDay.of(JANUARY, 1), // 1. nyttårsdag
        MonthDay.of(MAY, 1), // Offentlig høytidsdag
        MonthDay.of(MAY, 17), // Grunnlovsdag
        MonthDay.of(DECEMBER, 25), // 1. juledag
        MonthDay.of(DECEMBER, 26), // 2. juledag
    )

private val dagerFraFørstePåskedag =
    listOf(
        -7L, // Palmesøndag
        -3L, // Skjærtorsdag
        -2L, // Langfredag
        0L, // 1. påskedag
        1L, // 2. påskedag
        39L, // Kristi himmelfartsdag
        49L, // 1. pinsedag
        50L, // 2. pinsedag
    )

fun erHelligdag(dato: LocalDate) = dato in helligdagerIÅr(dato.year)

private fun helligdagerIÅr(år: Int): List<LocalDate> = fasteHelligdager.map { it.atYear(år) } + førstePåskedag(år).let { dagerFraFørstePåskedag.map(it::plusDays) }

// 1. påskedag er første søndag etter den kirkelige påskefullmånen, men aldri senere enn 25. april
internal fun førstePåskedag(år: Int): LocalDate {
    val søndagEtterFullmåne = påskefullmåne(år).with(TemporalAdjusters.next(SUNDAY))
    return if (MonthDay.from(søndagEtterFullmåne).isAfter(MonthDay.of(APRIL, 25))) {
        søndagEtterFullmåne.minusWeeks(1)
    } else {
        søndagEtterFullmåne
    }
}

/**
 * Den kirkelige påskefullmånen er første fullmåne på eller etter 21. mars, hentet fra kirkens
 * månetabell og ikke fra astronomien. Forenklet utgave av den gregorianske algoritmen til
 * Meeus/Jones/Butcher, som bare gjelder for årene 1900–2199.
 */
private fun påskefullmåne(år: Int): LocalDate {
    require(år in 1900..2199) { "Påskeberegningen støtter bare årene 1900–2199, fikk $år" }
    // Månefasene gjentar seg på samme datoer hvert 19. år (Metons syklus)
    val plassIMånesyklus = år % 19
    // Fullmånen kommer 11 dager tidligere for hvert år i syklusen (19 ≡ -11 mod 30). 24 er startpunktet
    // i syklusen, og summerer kalenderens korreksjoner for skuddår og månefaser fram til 1900–2199.
    val dagerEtter21Mars = (19 * plassIMånesyklus + 24) % 30
    val fullmånedato = LocalDate.of(år, MARCH, 21).plusDays(dagerEtter21Mars.toLong())

    // Kirkens tabell flytter fullmånen 18. april til 17. april, slik at to år i samme 19-årssyklus ikke får samme fullmånedato
    return if (MonthDay.from(fullmånedato) == MonthDay.of(APRIL, 18)) {
        fullmånedato.minusDays(1)
    } else {
        fullmånedato
    }
}
