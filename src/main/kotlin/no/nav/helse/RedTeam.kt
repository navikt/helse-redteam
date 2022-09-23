package no.nav.helse

import no.nav.helse.Team.*
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate


class RedTeam(
    private var startDate: LocalDate,
    private val team: Team,
    extraNonWorkDays: List<NonWorkday> = emptyList()
) {
    private val overrides = mutableMapOf<LocalDate, List<Swap>>()
    private val weekend = listOf(SATURDAY, SUNDAY)
    private val holidays = extraNonWorkDays.associateBy { it.date }

    fun teamFor(date: LocalDate): Day {
        if(date in holidays) return holidays[date]!!
        if (date.dayOfWeek in weekend) return NonWorkday(date, date.dayOfWeek.name)
        return Workday(date, team.teamAt(datesTo(date)).applySwaps(date).sortedBy { it.team })
    }

    fun override(from: String, to: String, date: LocalDate) {
        validateDate(date, from, to)
        team.validate(from, to)
        addSwap(date, from, to)
    }

    fun redTeamCalendar(span: Pair<LocalDate, LocalDate>): RedTeamCalendarDto {
        require(span.first.isBefore(span.second) || span.first == span.second) {"Invalid date span to generate team for"}
        val redTeams = span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
        return RedTeamCalendarDto( team.groups(), redTeams)
    }

    private fun addSwap(date: LocalDate, from: String, to: String) {
        overrides.compute(date) { _, value ->
            return@compute value?.plus(Swap(from, to)) ?: mutableListOf(
                Swap(
                    from,
                    to
                )
            )
        }
    }

    private fun validateDate(date: LocalDate, from: String, to: String) {
        require(teamFor(date) is Workday) { "Trying to override red team for a non-workday: $date" }
        require(from in (teamFor(date) as Workday).members.map { it.name })
        { "from: $from in swap(from: $from, to: $to) is not red-team at date: $date" }
    }

    private fun datesTo(date: LocalDate) =
        startDate.datesUntil(date).filter { it.dayOfWeek !in weekend }.filter { it !in holidays }.count().toInt()


    private fun List<TeamMember>.applySwaps(date: LocalDate) =
        overrides.getOrElse(date) { emptyList() }.fold(this) { acc, swap ->  acc.tryReplaceWith(swap) }

    private fun List<TeamMember>.tryReplaceWith(swap: Swap) =
        map { TeamMember(it.team,  if(swap.from == it.name ) swap.to else it.name) }
}

data class Swap(val from: String, val to: String)


data class RedTeamCalendarDto(
    val teams: List<TeamDto>,
    val days: List<Day>
)

interface Day

data class Workday(val date: LocalDate, val members: List<TeamMember>): Day {
}
data class NonWorkday(val date: LocalDate, val name: String): Day {
}