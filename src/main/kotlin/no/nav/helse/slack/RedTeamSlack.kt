package no.nav.helse.slack

import com.slack.api.Slack
import no.nav.helse.model.Workday
import no.nav.helse.teamDataFromFile
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

class RedTeamSlack(
    private val token: String,
    private val slackChannel: String,
    private val userGroup: String,
) {
    private val client: Slack get() = Slack.getInstance()

    fun postRedTeam(team: Workday) {
        val dateString = team.date.format(formatter)
        val response =
            client.methods(token).chatPostMessage {
                it
                    .channel(slackChannel)
                    .text(
                        ":wave: :small_airplane: Red team for $dateString:\n" +
                            team.toPostText() +
                            "Red team kan administreres på <https://sykepenger-red-team.ansatt.nav.no|sykepenger-red-team.ansatt.nav.no>",
                    )
            }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun postRedTeamOverride(team: Workday) {
        val dateString = team.date.format(formatter)
        val response =
            client.methods(token).chatPostMessage {
                it
                    .channel(slackChannel)
                    .text(
                        ":wave: :small_airplane: Red team har blitt oppdatert for $dateString: :thanks: \n" +
                            team.toPostText() +
                            "Red team kan administreres på <https://sykepenger-red-team.ansatt.nav.no|sykepenger-red-team.ansatt.nav.no>",
                    )
            }

        if (!response.isOk) {
            throw RuntimeException("Error occured when posting to slack: ${response.errors}")
        }
    }

    fun tullOgFjas() {
        val kandidater =
            tulleFolk.keys
                .toList()
                .shuffled()
                .take(2)
        siNoeTull(tulleMessages.shuffled().first()("<@${kandidater.last()}>"))
        siNoeTull(memeMessages.shuffled().first()("<@${kandidater.first()}>"))
    }

    private fun siNoeTull(fjas: String) {
        val response =
            client.methods(token).chatPostMessage {
                it
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
        fun Workday.toPostText() =
            teams.joinToString("") { team ->
                " - ${team.redteamMembers.joinToString { "<@${it.slackId}>" }} (${team.team})\n"
            }

        private val formatter =
            DateTimeFormatterBuilder()
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

internal val tulleMessages: List<(person: String) -> String> =
    listOf(
        { ":wave: God fredag, $it! Hvis uka di var en av appene våre, hvilken ville det vært? Spleis, Spesialist, Spøkelse eller noe helt annet? Begrunn gjerne :thinking-ass:" },
        { ":wave: Morning $it :ffingerguns: Oppsummer uka di som en commit-melding med gitmoji. Eksempel: «:fire: Fjerner alle møter»" },
        { ":wave: Hei $it! Beskriv uka di med tre emojier, så gjetter vi andre hva som skjedde :pepe-giggles:" },
        { ":wave: God morgen $it! Vi trenger en ny app. Finn på navnet og hva den gjør. Eneste regel: navnet må begynne med «sp» :rocket:" },
        { ":wave: Hallo $it! Arbeidsgiverperioden er 16 dager. Hvor lang er din personlige oppstartsperiode før hjernen er på jobb om morgenen? :coffee:" },
        { ":wave: Morning $it :sloth: Hvis denne uka var et dyr, hvilket dyr ville det vært?" },
        { ":wave: God fredag $it! Når er skjæringstidspunktet for helgen din? Kl. 15:00, eller er du der allerede? :thinking-ass:" },
        { ":wave: Hei $it :excited: Du har fått 6G å bruke på helgen. Hva gjør du med pengene? :moneybag:" },
        { ":wave: Morning $it! To sannheter og én løgn om deg. Vi gjetter i tråden :male-detective:" },
        { ":wave: Hei $it :jammin_squirtle: Hvilken sang har gått på repeat hos deg denne uka? Lenke i tråden, takk!" },
        { ":wave: Morning $it :pirate: Hva er ukas lille seier? Alt teller." },
        { ":wave: Hei $it :small_airplane: Hva er det rareste du har fått spørsmål om som red team? Anonymiser gjerne :shushing_face:" },
        { ":wave: God fredag $it :yum: Hva er det beste du har spist denne uka?" },
    )

internal val memeMessages: List<(person: String) -> String> =
    listOf(
        { ":wave: Fredagsmeme! $it åpner: del den beste memen du har sett denne uka. Den trenger ikke handle om jobb. Alle andre fyller på i tråden :ffingerguns:" },
        { ":wave: GIF-fredag! $it starter med en GIF som beskriver uka. Alle andre svarer med en GIF tilbake :pepe-giggles:" },
    )
