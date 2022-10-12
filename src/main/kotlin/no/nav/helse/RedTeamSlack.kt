package no.nav.helse

import com.slack.api.Slack
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.TextStyle
import java.util.*

class RedTeamSlack(private val token: String, private val slackChannel: String, private val userGroup: String) {

    val client get() = Slack.getInstance()

    fun postRedTeam(team: Workday) {
        val dateString = team.date.dayOfMonth.toString() + "." + team.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(":wave: :bomlo: Dagens red-team ($dateString)\n" +
                    " - <@${team.members[0].slackId}>\n" +
                    " - <@${team.members[1].slackId}>\n" +
                    " - <@${team.members[2].slackId}>\n" +
                    "Red-team kan administreres på <https://tbd.intern.nav.no/docs/redteam-wiki/red-team|tbd.intern.nav.no>")
        }
        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun updateReadTeamGroup(team: Workday) {
        val slackIDs = team.members.map { member -> member.slackId }
        val response = client.methods(token).usergroupsUsersUpdate { it.usergroup(userGroup).users(slackIDs) }
        if (!response.isOk) {
            throw RuntimeException("Error occurred when updating group on slack: ${response.error}")
        }
    }
}

fun main() {

    val token = System.getenv("SLACK_TOKEN")
    val redTeam = RedTeam(
        LocalDate.of(2022, 1, 1),
        Team(
            TeamDto("Speilvendt", listOf(MemberDto("Sondre", "UBCJCLFD5"))),
            TeamDto("Spleiselaget", listOf(MemberDto("Christian", "U03KX96MT39"))),
            TeamDto("Fag", listOf(MemberDto("Margrethe", "UMHUJNE5N")))
        )
    )
    RedTeamSlack(token, "team-bømlo", "team-bømlo").updateReadTeamGroup((redTeam.teamFor(now()) as Workday))
}