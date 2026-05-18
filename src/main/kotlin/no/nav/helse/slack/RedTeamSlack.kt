package no.nav.helse.slack

import com.slack.api.Slack
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import no.nav.helse.model.Workday
import no.nav.helse.teamDataFromFile

class RedTeamSlack(private val token: String, private val slackChannel: String, private val userGroup: String) {

    private val client: Slack get() = Slack.getInstance()

    fun postRedTeam(team: Workday) {
        val dateString = team.date.format(formatter)
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(":wave: :small_airplane: Red team for $dateString:\n" +
                    team.toPostText() +
                    "Red team kan administreres på <https://tbd.ansatt.nav.no|tbd.ansatt.nav.no>")
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }
    fun postRedTeamOverride(team: Workday) {
        val dateString = team.date.format(formatter)
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(":wave: :small_airplane: Red team har blitt oppdatert for $dateString: :thanks: \n" +
                    team.toPostText() +
                    "Red team kan administreres på <https://tbd.ansatt.nav.no|tbd.ansatt.nav.no>")
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun tullOgFjas() {
        val kandidater = tulleFolk.keys.toList().shuffled().take(2)
        siNoeTull(tulleMessages.shuffled().first()("<@${kandidater.last()}>"))
        siNoeTull(":wave: Morning <@${kandidater.first()}>. Kan ikke du starte meme-ballet med noe lættis denne fredagen?")
    }

    private fun siNoeTull(fjas: String) {
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(fjas)
        }
        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun updateRedTeamGroup(team: Workday) {
        val slackIDs = team.teams.flatMap { it.redteamMembers }.map { it.slackId }
        val response = client.methods(token).usergroupsUsersUpdate { it.usergroup(userGroup).users(slackIDs) }
        if (!response.isOk) {
            throw RuntimeException("Error occurred when updating group on slack: ${response.error}")
        }
    }

    companion object {
        fun Workday.toPostText() = teams.joinToString("") { team ->
            " - ${team.redteamMembers.joinToString { "<@${it.slackId}>" }} (${team.team})\n"
        }

        private val formatter = DateTimeFormatterBuilder()
            .appendText(ChronoField.DAY_OF_MONTH)
            .appendLiteral(". ")
            .appendText(ChronoField.MONTH_OF_YEAR)
            .toFormatter(Locale.getDefault())
    }
}

private val tulleFolk =
    teamDataFromFile()
        .flatMap { it.members }
        .associate { it.slackId to it.name }

internal val tulleMessages: List<(person: String) -> String> = listOf(
    {":wave: Morning :hehege: $it. Har du lest slack i 15 min i dag? Kan du fortelle mer om det?"},
    {":wave: Morning :explodinghead: $it. Kan ikke du starte dagen med en motiverende tale? Det tror jeg mange setter pris på!"},
    {":wave: Morning $it. Hva er din definisjon av en sak?"},
    {":wave: Morning $it. Når synes du det er nytt skjæringstidspunkt og vi trenger ny inntektsmelding? :thinking-ass:"},
    {":wave: Morning $it. Hvordan går det med dine nyttårsforsetter? :pepe-giggles:"},
    {":wave: Morning :ffingerguns: $it. Nå er det snart helg!!! Kan du komme med et par tips til hva dine kolleger kan gjøre i helgen?"},
    {":wave: G'day mate :pirate: $it. Er du en ja-kopp eller en nei-kopp i dag?"},
    {":wave: God morgen, $it :excited:! Du som er så god i det meste, kan du lære oss noe kult?"},
    {":wave: Må itj fårrå nålles, $it :happymarty:! Men del gjerne med oss dagens trønderord og hvorfor det er turan som tælle?"},
    {":wave: Heisann $it :maxi-jam:! Har du tenkt på noen spennende nøtter i det siste? :maxi-nut-cracker: :maxi-excited:"},
    {":wave: Hællæ, $it :christian-king:! På tide med noen Halden-fæcts, kan du nevne tre ting som er bedre i Halden?"},
)
