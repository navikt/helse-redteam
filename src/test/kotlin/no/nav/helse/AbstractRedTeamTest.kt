package no.nav.helse

import no.nav.helse.model.MemberDto
import no.nav.helse.model.NonWorkday
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Team
import no.nav.helse.model.TeamDto
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAmount

open class AbstractRedTeamTest {

    protected open val START_DATE: LocalDate = LocalDate.of(2022, 1, 3)

    internal fun redTeam(extraNonWorkDays: List<NonWorkday> = emptyList()) = RedTeam(
        START_DATE,
        Team(
            TeamDto("Spleiselaget", genTeam("Sondre", "David", "Christian")),
            TeamDto("Speilvendt",   genTeam("Jakob", "Sindre")),
            TeamDto("Fag",          genTeam("Morten", "Cecilie"))
        ),
        extraNonWorkDays,
    )

    private fun genTeam(vararg names: String) = names.map { MemberDto(it, "slackid-$it") }

    protected fun testklokke(dato: LocalDate): Clock {
        val zone = ZoneId.of("Europe/Oslo")
        return Clock.fixed(dato.atStartOfDay(zone).toInstant(), zone)
    }

    class MutableClock(private var instant: Instant) : Clock() {
        fun nyttTidspunkt(instant: Instant) {
            this.instant = instant
        }

        operator fun plusAssign(amount: TemporalAmount) {
            instant = instant.plus(amount)
        }

        fun nyttTidspunkt(hour: Long, date: Long, month: Long) {
            instant = tidspunkt(hour, date, month)
        }

        override fun instant() = instant
        override fun getZone(): ZoneId = ZoneId.of("Europe/Oslo")
        override fun withZone(zoneId: ZoneId): Clock = throw UnsupportedOperationException()
    }

    companion object {
        fun tidspunkt(hour: Long, date: Long, month: Long = 1): Instant =
            LocalDateTime.now().run {
                with(ChronoField.YEAR, 2022)
                    .with(ChronoField.MONTH_OF_YEAR, month)
                    .with(ChronoField.DAY_OF_MONTH, date)
                    .with(ChronoField.HOUR_OF_DAY, hour)
                    .with(ChronoField.MINUTE_OF_HOUR, 0)
                    .toInstant(ZoneOffset.ofHours(1))
            }
    }

}
