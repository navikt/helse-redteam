package no.nav.helse

import no.nav.helse.Team.*

data class TeamDto(
    val name: String,
    val members: List<String>
)
class Team(private vararg val groups: TeamDto) {

    data class TeamMember(
        val team: String,
        val name: String
    )

    internal fun teamAt(count: Int): List<TeamMember> = groups.map { group -> TeamMember(group.name, group.members[count % group.members.size]) }

    internal fun teamAt(swaps: List<Swap>, count: Int): List<TeamMember> {
        val nextTeam = teamAt(count)
        validate(swaps,  nextTeam.map { it.name })
        return nextTeam.replaceWith(swaps).sortedBy { it.team }
    }

    private fun validate(swaps: List<Swap>) {
        swaps.forEach { swap ->
            require(groups.find { swap.from in it.members } == groups.find { swap.to in it.members })
            { "Invalid swap: ${swap.from} and ${swap.to} not in same group" }
        }
    }

    private fun validate(swaps: List<Swap>, nextTeam: List<String>) {
        validate(swaps)
        swaps.map { it.from }.forEach { replaced ->
            require(replaced in nextTeam) { "Invalid swap: replaced $replaced not in the team $nextTeam" }
        }
    }

    fun groups() = groups.toList()
    fun minLength() = groups.minOf { it.members.size }
    fun maxLength() = groups.maxOf { it.members.size }
}

fun List<TeamMember>.replaceWith(swaps: List<Swap>): List<TeamMember> {
    return map { TeamMember(it.team,  (swaps.find { swap -> swap.from == it.name }?.to ?: it.name)) }
}
