package no.nav.helse.model

import no.nav.helse.model.Team.*
import no.nav.helse.mapper
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class RedTeam(
    /** brukes _kun_ til å generere utkast til red-team. Sikrer en slags determinisme */ private var seedDate: LocalDate,
    private val getTeam: () -> Team,
    extraNonWorkDays: List<NonWorkday> = emptyList()
) {
    private val team get() = getTeam()
    private val overrides = mutableMapOf<LocalDate, List<Pair<TeamMember, TeamMember>>>()
    private val weekend = listOf(SATURDAY, SUNDAY)
    private val holidays = extraNonWorkDays.associateBy { it.date }

    fun teamFor(date: LocalDate): Day {
        if (date in holidays) return holidays[date]!!
        if (date.dayOfWeek in weekend) return NonWorkday(date)
        return Workday(date, team.teamAt(antallArbeidsdagerFraSeed(date)).applySwaps(date).sortedBy { it.team })
    }

    fun override(from: String, to: String, date: LocalDate) {
        validateDate(date, from, to)
        val (fromMember, toMember) = team.swap(from, to)
        addSwap(date, fromMember, toMember)
    }

    fun redTeamCalendar(span: Pair<LocalDate, LocalDate>): RedTeamCalendarDto {
        require(span.first.isBefore(span.second) || span.first == span.second) {"Invalid date span to generate team for"}
        val redTeams = span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
        return RedTeamCalendarDto( team.groups(), redTeams)
    }

    private fun addSwap(date: LocalDate, from: TeamMember, to: TeamMember) {
        overrides.compute(date) { _, value ->
            return@compute value?.plus(Pair(from, to)) ?: mutableListOf(
                Pair(
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

    private fun antallArbeidsdagerFraSeed(date: LocalDate) =
        seedDate.datesUntil(date).filter { it.dayOfWeek !in weekend }.filter { it !in holidays }.count().toInt()


    private fun List<TeamMember>.applySwaps(date: LocalDate) =
        overrides.getOrElse(date) { emptyList() }.fold(this) { acc, swap ->  acc.tryReplaceWith(swap) }

    private fun List<TeamMember>.tryReplaceWith(swap: Pair<TeamMember, TeamMember>) =
        map { if(swap.first.name == it.name ) swap.second else it }
}

data class Swap(val from: String, val to: String)


data class RedTeamCalendarDto(
    val teams: List<TeamDto>,
    val days: List<Day>
) {
    fun json(): String = mapper.writeValueAsString(this)
}

interface Day {
    fun json(): String = mapper.writeValueAsString(this)
}

data class Workday(val date: LocalDate, val members: List<TeamMember>): Day
data class NonWorkday(val date: LocalDate): Day
