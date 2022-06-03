package no.nav.helse

import java.time.LocalDate

class Kalender(private var date: LocalDate, private val team: Team) {
    private val overrides = mutableMapOf<LocalDate, List<Swap>>()

    fun next() = (overrides[date]?.let { Dag(date, team.next(it)) } ?: Dag(date, team.next())).also { date = date.plusDays(1) }

    fun override(from: String, to: String, date: LocalDate) {
        overrides.compute(date) { _, value -> return@compute value?.plus(Swap(from, to)) ?: mutableListOf(Swap(from, to)) }
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