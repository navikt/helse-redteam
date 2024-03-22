package no.nav.helse.model

import no.nav.helse.model.MemberDto.Companion.names

data class TeamDto(
    val name: String,
    val members: List<MemberDto>
) {
    internal fun memberFor(name: String): Team.TeamMember? {
        val member = members.find { name == it.name } ?: return null
        return Team.TeamMember(this.name, member.name, member.slackId)
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

class Team(private vararg val groups: TeamDto) {

    data class TeamMember(
        val team: String,
        val name: String,
        val slackId: String
    )

    internal fun teamAt(count: Int): List<TeamMember> =
        groups.map { group ->
            val member = group.members[count % group.members.size]
            TeamMember(group.name, member.name, member.slackId)
        }

    internal fun somTeamMember(navn: String): TeamMember {
        val toGroup = groups.find { group -> navn in group.members.names() } ?: throw IllegalArgumentException("to: $navn does not exist in a group")
        val toMember = toGroup.memberFor(navn) ?: throw IllegalArgumentException("to: $navn does not exist in group: $toGroup")
        return toMember
    }
    fun groups() = groups.toList()
    fun minLength() = groups.minOf { it.members.size }
    fun maxLength() = groups.maxOf { it.members.size }
}

