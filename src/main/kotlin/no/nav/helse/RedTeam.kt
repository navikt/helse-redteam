package no.nav.helse

import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate


class RedTeam(private var startDate: LocalDate, private val team: Team) {
    private val overrides = mutableMapOf<LocalDate, List<Swap>>()
    private val weekend = listOf(SATURDAY, SUNDAY)

    fun teamFor(date: LocalDate): Day {
        if(date.dayOfWeek in weekend) return NonWorkday(date, date.dayOfWeek.name)
        return Workday(date, team.teamAt(overrides.getOrElse(date) { emptyList() }, datesTo(date)))
    }

    fun override(from: String, to: String, date: LocalDate) {
        overrides.compute(date) { _, value -> return@compute value?.plus(Swap(from, to)) ?: mutableListOf(Swap(from, to)) }
    }

    fun teamsFor(span: Pair<LocalDate, LocalDate>): List<Day> {
        require(span.first.isBefore(span.second))
        return span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
    }

    private fun datesTo(date: LocalDate) = startDate.datesUntil(date).filter { it.dayOfWeek !in weekend }.count().toInt()
}

data class Swap(val from: String, val to: String)

interface Day

data class Workday(val date: LocalDate, val members: List<String>): Day {
}
data class NonWorkday(private val date: LocalDate, private val name: String): Day {
}