package no.nav.helse

import java.time.LocalDate

class RedteamMediator(
    private val slackClient: RedTeamSlack,
    private val redTeam: RedTeam
) {

    fun teamFor(date: LocalDate) = redTeam.teamFor(date)

    fun override(from: String, to: String, date: LocalDate) {

        redTeam.override(from, to, date)
    }

    fun redTeamCalendar(span: Pair<LocalDate, LocalDate>) = redTeam.redTeamCalendar(span)

}