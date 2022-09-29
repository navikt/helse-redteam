package no.nav.helse

import no.nav.helse.MemberDto.Companion.names

data class TeamDto(
    val name: String,
    val members: List<MemberDto>
) {
    internal fun memberFor(name: String): Team.TeamMember? {
        val member = members.find { name == it.name } ?: return null
        return Team.TeamMember(this.name, member.name, member.slackId)
    }
}

class MemberDto(
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

    internal fun swap(from: String, to: String): Pair<TeamMember, TeamMember> {
        val fromGroup = groups.find { group -> from in group.members.names() } ?: throw IllegalArgumentException("from: $from does not exist in a group")
        val toGroup = groups.find { group -> from in group.members.names() } ?: throw IllegalArgumentException("to: $from does not exist in a group")
        require( fromGroup == toGroup) { "Invalid swap: $from and $to not in same group" }
        val fromMember = fromGroup.memberFor(from) ?: throw IllegalArgumentException("from: $from does not exist in group: $fromGroup")
        val toMember = toGroup.memberFor(to) ?: throw IllegalArgumentException("to: $from does not exist in group: $toGroup")
        return Pair(fromMember, toMember)
    }
    fun groups() = groups.toList()
    fun minLength() = groups.minOf { it.members.size }
    fun maxLength() = groups.maxOf { it.members.size }
}

