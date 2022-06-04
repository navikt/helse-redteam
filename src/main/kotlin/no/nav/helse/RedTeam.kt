package no.nav.helse

import java.time.LocalDate

class RedTeam(private var startDate: LocalDate, private val team: Team) {
    private val overrides = mutableMapOf<LocalDate, List<Swap>>()

    fun teamFor(date: LocalDate): Day =
        Workday(date, team.teamAt(overrides.getOrElse(date) { emptyList() }, startDate.datesUntil(date).count().toInt()))

    fun override(from: String, to: String, date: LocalDate) {
        overrides.compute(date) { _, value -> return@compute value?.plus(Swap(from, to)) ?: mutableListOf(Swap(from, to)) }
    }

    fun teamsFor(span: Pair<LocalDate, LocalDate>): List<Day> {
        require(span.first.isBefore(span.second))
        return span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
    }
}

data class Swap(val from: String, val to: String)

interface Day

data class Workday(val date: LocalDate, val members: List<String>): Day {
}
data class NonWorkday(private val date: LocalDate, private val members: List<String>): Day {
}