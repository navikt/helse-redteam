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
                    "Red team kan administreres på <https://tbd.ansatt.nav.no/docs/redteam-wiki/red-team|tbd.ansatt.nav.no>")
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }
    fun postRedTeamOverride(team: Workday) {
        val dateString = team.date.format(formatter)
        val response = client.methods(token).chatPostMessage { it
            .channel(slackChannel)
            .text(":wave: :bomlo: Red team har blitt oppdatert for $dateString: :thanks: \n" +
                    " - <@${team.members[0].slackId}> (${team.members[0].team})\n" +
                    " - <@${team.members[1].slackId}> (${team.members[1].team})\n" +
                    " - <@${team.members[2].slackId}> (${team.members[2].team})\n" +
                    "Red team kan administreres på <https://tbd.ansatt.nav.no/docs/redteam-wiki/red-team|tbd.ansatt.nav.no>")
        }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun tullOgFjas() {
        val kandidater = tulleFolk.keys.shuffled().take(2)
        siNoeTull(tulleMessages.shuffled().first()(kandidater.last()))
        siNoeTull(":wave: Morning ${kandidater.first()}. Kan ikke du starte meme-ballet med noe lættis denne fredagen?")
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
        {
            Team(
                TeamDto("Speilvendt", listOf(MemberDto("Sondre", "UBCJCLFD5"))),
                TeamDto("Spleiselaget", listOf(MemberDto("Christian", "U03KX96MT39"))),
                TeamDto("Fag", listOf(MemberDto("Margrethe", "UMHUJNE5N")))
            )
        }
    )
    RedTeamSlack(token, "team-bømlo", "team-bømlo").updateRedTeamGroup((redTeam.teamFor(now()) as Workday))
}

private val tulleFolk = mapOf(
    "<@U5LJ6JHLL>" to "David",
    "<@U01HXSKBDJ7>" to "Hege",
    "<@U8G3WN6M6>" to "MortenT",
    "<@U01DGUE8DLL>" to "Simen",
    "<@UK6TD930C>" to "Jakopp",
    "<@UUQQ1EHBN>" to "Marte",
    "<@UEHPCUFCJ>" to "Maxi",
    "<@US0C415LZ>" to "Christian",
    "<@UDWEJT5PW>" to "Håkon",
    "<@U029VUUS1CJ>" to "Eirik",
    "<@U040GTABBSM>" to "Elias",
    "<@U04RTT5Q80J>" to "Martin",
    "<@U6VPRN57C>" to "Camilla",
    "<@U05D2LNF9HB>" to "Amalie",
    "<@UCKU5BJBW>" to "Kjetil",
    "<@U05KPGBFB51>" to "Trine",
    "<@U016FGR81CN>" to "Jonas",
    "<@U6QCP24SF>" to "Øyvind",
    "<@UAHN8TBD3>" to "Solveig",
    "<@U025P2PGW2W>" to "Øydis",
    "<@UM6FRDCBU>" to "Aminet",
    "<@UMHEW1BS8>" to "Emine",
    "<@UMHUJNE5N>" to "MortenN"
)

private val tulleMessages: List<(person: String) -> String> = listOf(
    {":wave: Morning :hehege: $it. Har du lest slack i 15 min i dag? Kan du fortelle mer om det?"},
    {":wave: Morning :explodinghead: $it. Kan ikke du starte dagen med en motiverende tale? Det tror jeg mange setter pris på!"},
    {":wave: Morning $it. Hva er din definisjon av en sak?"},
    {":wave: Hallais :digimorty: $it. Alle vet du har et tørt ordspill på lur, kan du ikke dele ett?!"},
    {":wave: Morning :ffingerguns: $it. Nå er det snart helg!!! Kan du komme med et par tips til hva dine kolleger kan gjøre i helgen?"},
    {":wave: G'day mate :pirate: $it. Er du en ja-kopp eller en nei-kopp i dag?"},
    {":wave: God morgen, $it :excited:! Du som er så god i det meste, kan du lære oss noe kult?"},
    {":wave: Må itj fårrå nålles, $it :happymarty:! Men del gjerne med oss dagens trønderord og hvorfor det er turan som tælle?"},
    {":wave: Heisann $it :maxi-jam:! Har du tenkt på noen spennende nøtter i det siste? :maxi-nut-cracker: :maxi-excited:"},
    {":wave: Hællæ, $it :christian-king:! På tide med noen Halden-fæcts, kan du nevne tre ting som er bedre i Halden?"},
    {":wave: Så var fredag igjen vettu, eller hur? Kan ikke $it informere oss om hvor mange skritt han gikk i går :walking-dogs:? Vi er så himla spent på denne gåmatta!"}
)
