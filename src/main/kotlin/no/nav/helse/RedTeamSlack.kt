package no.nav.helse

import com.slack.api.Slack
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.TextStyle
import java.util.*

class RedTeamSlack(private val token: String) {

    val client get() = Slack.getInstance()

    fun postRedTeam(team: Workday) {
        val dateString = team.date.dayOfMonth.toString() + "." + team.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val response = client.methods(token).chatPostMessage { it
            .channel("team-bømlo")
            .text(":wave: :bomlo: Dagens red-team ($dateString)\n" +
                    " - <@${team.members[0].slackId}>\n" +
                    " - <@${team.members[1].slackId}>\n" +
                    " - <@${team.members[2].slackId}>\n" +
                    "Red-team kan administreres på <https://tbd.intern.nav.no/docs/redteam-wiki/red-team|tbd.intern.nav.no>")
        }
        if (!response.isOk) {
            throw IllegalStateException("Error occured when posting to slack: ${response.errors}")
        }
    }
}

fun main() {

    val token = System.getenv("SLACK_TOKEN")
    val redTeam = RedTeam(
        LocalDate.of(2022, 1, 1),
        Team(
            TeamDto("Speilvendt", listOf(MemberDto("Sondre", ""), MemberDto("Jakob", ""))),
            TeamDto("Spleiselaget", listOf(MemberDto("Christian", ""))),
            TeamDto("Fag", listOf(MemberDto("Margrethe", "")))
        )
    )
    RedTeamSlack(token).postRedTeam((redTeam.teamFor(now()) as Workday))

    // val response2 = slack.methods(token).usergroupsUsersUpdate { it.usergroup("red-team").users(d) }
}