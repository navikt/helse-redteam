package no.nav.helse

import java.time.LocalDate

class RedTeam(private var startDate: LocalDate, private val team: Team) {
    private val overrides = mutableMapOf<LocalDate, List<Swap>>()

    fun teamFor(date: LocalDate) =
        Dag(date, team.teamAt(overrides.getOrElse(date) { emptyList() }, startDate.datesUntil(date).count().toInt()))

    fun override(from: String, to: String, date: LocalDate) {
        overrides.compute(date) { _, value -> return@compute value?.plus(Swap(from, to)) ?: mutableListOf(Swap(from, to)) }
    }

    fun teamsFor(span: Pair<LocalDate, LocalDate>): List<Dag> {
        require(span.first.isBefore(span.second))
        return span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
    }
}

data class Swap(val from: String, val to: String)

class Dag(private val date: LocalDate, private val members: List<String>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dag

        if (date != other.date) return false
        if (members != other.members) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + members.hashCode()
        return result
    }

    override fun toString(): String {
        return "Dag(date=$date, members=$members)"
    }


}