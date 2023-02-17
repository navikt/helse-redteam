package no.nav.helse.slack

import com.slack.api.Slack
import no.nav.helse.model.*
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

class RedTeamSlack(private val token: String, private val slackChannel: String, private val userGroup: String) {

    private val client: Slack get() = Slack.getInstance()

    fun postRedTeam(team: Workday) {
        val dateString = team.date.format(formatter)
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(":wave: :bomlo: Red team for $dateString:\n" +
                    " - <@${team.members[0].slackId}> (${team.members[0].team})\n" +
                    " - <@${team.members[1].slackId}> (${team.members[1].team})\n" +
                    " - <@${team.members[2].slackId}> (${team.members[2].team})\n" +
                    "Red team kan administreres på <https://tbd.intern.nav.no/docs/redteam-wiki/red-team|tbd.intern.nav.no>")
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun tulleMedNoen() {
        val message = tulleMessages.shuffled().first()
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(message)
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun updateRedTeamGroup(team: Workday) {
        val slackIDs = team.members.map { member -> member.slackId }
        val response = client.methods(token).usergroupsUsersUpdate { it.usergroup(userGroup).users(slackIDs) }
        if (!response.isOk) {
            throw RuntimeException("Error occurred when updating group on slack: ${response.error}")
        }
    }

    companion object {
        private val formatter = DateTimeFormatterBuilder()
            .appendText(ChronoField.DAY_OF_MONTH)
            .appendLiteral(". ")
            .appendText(ChronoField.MONTH_OF_YEAR)
            .toFormatter(Locale.getDefault())
    }
}

// For manuell slack-posting, aka. testing.
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
    RedTeamSlack(token, "team-bømlo", "team-bømlo").updateRedTeamGroup((redTeam.teamFor(now()) as Workday))
}

private val tulleMessages = listOf(
    ":wave: Morning :hehege: <@U01HXSKBDJ7>. Har du lest slack i 15 min i dag?\n\n" +
            "Kan du fortelle mer om det?"
)
