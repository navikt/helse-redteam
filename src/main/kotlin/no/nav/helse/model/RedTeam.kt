package no.nav.helse.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    // dette skal være en list over de team-medlemmene som har redteam-ansvar på en bestemt dag. Det bør være _tre_ per dag. List vil nok ikke inneholde de som er algoritmisk bestemt
    private val faktiskRedTeam = mutableMapOf<LocalDate, List<TeamMember>>()
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
    }

    private fun antallArbeidsdagerFraSeed(date: LocalDate) =
        seedDate.datesUntil(date).filter { it.dayOfWeek !in weekend }.filter { it !in holidays }.count().toInt()


    private fun List<TeamMember>.applySwaps(date: LocalDate) =
        overrides.getOrElse(date) { emptyList() }.fold(this) { acc, swap ->  acc.tryReplaceWith(swap) }

    private fun List<TeamMember>.tryReplaceWith(swap: Pair<TeamMember, TeamMember>) =
        map { if(swap.first.name == it.name ) swap.second else it }

    fun overstyringerSomJson() = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(overrides)
    fun byttUtOverstyringer(overridesFraBøtta: String) {
        val nyeOverstyringer = jacksonObjectMapper().readTree(overridesFraBøtta)
        val ersatz = nyeOverstyringer.fieldNames().asSequence().map {
                LocalDate.parse(it) to nyeOverstyringer[it].somOverstyringer()
        }.toMap()
        overrides.clear()
        overrides.putAll(ersatz)

        faktiskRedTeam.clear()
        faktiskRedTeam.putAll(ersatz.sistePerDag())
    }

    private fun JsonNode.somOverstyringer():List<Pair<TeamMember, TeamMember>> {
        if (!this.isArray) return emptyList()
        return this.map {
            it.somOverstyringspar()
        }
    }
    private fun JsonNode.somOverstyringspar(): Pair<TeamMember, TeamMember> {
        return Pair(this["first"].somTeamSwap(), this["second"].somTeamSwap())
    }

    private fun JsonNode.somTeamSwap(): TeamMember =
        TeamMember(team = this["team"].asText(), name = this["name"].asText(), slackId = this["slackId"].asText())
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

fun Map<LocalDate, List<Pair<TeamMember, TeamMember>>>.sistePerDag(): Map<LocalDate, List<TeamMember>> =
    this.keys.associateWith { dag ->
        this[dag]!!.groupBy { it.second.team }.values.map { it.last().second }
    }