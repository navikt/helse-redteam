package no.nav.helse.model

import no.nav.helse.model.MemberDto.Companion.names

data class TeamDto(
    val name: String,
    val members: List<MemberDto>
) {
    internal fun memberFor(name: String): Teams.RedTeamMember? {
        val member = members.find { name == it.name } ?: return null
        return Teams.RedTeamMember(member.name, member.slackId, this.name)
    }
}

data class MemberDto(
    val name: String,
    val slackId: String,
) {
    companion object {
        internal fun List<MemberDto>.names() = map { it.name }
    }
}

class Teams(private vararg val groups: TeamDto) {
    data class DayTeam(
        val team: String,
        val redteamMembers: List<RedTeamMember>
    )

    data class RedTeamMember(
        val name: String,
        val slackId: String,
        val team: String,
    )

    internal fun teamAt(count: Int): List<DayTeam> =
        groups.map { group ->
            val member = group.members[count % group.members.size]
            DayTeam(group.name, listOf(RedTeamMember(member.name, member.slackId, group.name)))
        }

    internal fun somRedTeamMembers(navn: List<String>): List<RedTeamMember> {
        return navn.map {
            val toGroup = groups.find { group -> it in group.members.names() } ?: throw IllegalArgumentException("to: $navn does not exist in a group")
            val toMember = toGroup.memberFor(it) ?: throw IllegalArgumentException("to: $navn does not exist in group: $toGroup")
            toMember
        }
    }
    fun groups() = groups.toList()
    fun minLength() = groups.minOf { it.members.size }
    fun maxLength() = groups.maxOf { it.members.size }
}

