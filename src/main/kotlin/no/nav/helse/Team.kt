package no.nav.helse

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

    internal fun validate(from: String, to: String) {
            require(groups.find { from in it.members } == groups.find { to in it.members })
            { "Invalid swap: $from and $to not in same group" }
    }

    fun groups() = groups.toList()
    fun minLength() = groups.minOf { it.members.size }
    fun maxLength() = groups.maxOf { it.members.size }
}

