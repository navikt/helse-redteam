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
    private val dagbestemmelser = mutableMapOf<LocalDate, MutableList<TeamMember>>()
    private val weekend = listOf(SATURDAY, SUNDAY)
    private val holidays = extraNonWorkDays.associateBy { it.date }

    fun teamFor(date: LocalDate): Day {
        if (date in holidays) return holidays[date]!!
        if (date.dayOfWeek in weekend) return NonWorkday(date)
        return Workday(date, team.teamAt(antallArbeidsdagerFraSeed(date)).leggPåDagbestemmelser(date).sortedBy { it.team })
    }

    fun override(to: String, date: LocalDate) {
        validateDate(date)
        val toMember= team.somTeamMember(to)
        bestemDag(date, toMember)
    }

    fun redTeamCalendar(span: Pair<LocalDate, LocalDate>): RedTeamCalendarDto {
        require(span.first.isBefore(span.second) || span.first == span.second) {"Invalid date span to generate team for"}
        val redTeams = span.first.datesUntil(span.second).map { teamFor(it) }.toList() + teamFor(span.second)
        return RedTeamCalendarDto( team.groups(), redTeams)
    }

    private fun bestemDag(date: LocalDate, to: TeamMember) {
        dagbestemmelser.getOrPut(date) {
            mutableListOf()
        }.apply {
            removeIf { it.team == to.team }
            add(to)
        }
    }

    private fun validateDate(date: LocalDate) {
        require(teamFor(date) is Workday) { "Trying to override red team for a non-workday: $date" }
    }

    private fun antallArbeidsdagerFraSeed(date: LocalDate) =
        seedDate.datesUntil(date).filter { it.dayOfWeek !in weekend }.filter { it !in holidays }.count().toInt()


    private fun List<TeamMember>.leggPåDagbestemmelser(date: LocalDate): List<TeamMember> {
        val ersatz = dagbestemmelser[date] ?: emptyList()
        return this.map { gammeltMedlem ->
            ersatz.lastOrNull { it.team == gammeltMedlem.team } ?: gammeltMedlem
        }
    }

    fun dagbestemmelserSomJson() = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dagbestemmelser)
    fun byttUtDagbestemmelserFraFastlager(overridesFraBøtta: String) {
        val nyeOverstyringer = jacksonObjectMapper().readTree(overridesFraBøtta)
        val ersatz = nyeOverstyringer.fieldNames().asSequence().map {
                LocalDate.parse(it) to nyeOverstyringer[it].somDagbestemmelser()
        }.toMap()

        dagbestemmelser.clear()
        dagbestemmelser.putAll(ersatz)
    }

    private fun JsonNode.somDagbestemmelser(): MutableList<TeamMember> {
        if (!this.isArray) return mutableListOf()
        return this.map {
            TeamMember(team = it["team"].asText(), name = it["name"].asText(), slackId = it["slackId"].asText())
        }.toMutableList()
    }
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
