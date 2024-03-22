package no.nav.helse

import no.nav.helse.model.RedTeam
import no.nav.helse.slack.SlackUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class RedteamMediator(
    private val slackUpdater: SlackUpdater,
    private val redTeam: RedTeam,
    private val bøtte: Bøtte = object : Bøtte {}
) {

    private val logger: Logger = LoggerFactory.getLogger("red-team-mediator")

    fun override(from: String, to: String, date: LocalDate) {
        logger.info("Override from $from to $to on date: {} started", date)
        redTeam.override(to, date)
        bøtte.lagreDagbestemmelser(redTeam.dagbestemmelserSomJson())
        slackUpdater.handleOverride(date)
        logger.info("Override from $from to $to on date: {} completed", date)
    }

    fun update() {
        slackUpdater.update()
    }

    fun teamFor(date: LocalDate) = redTeam.teamFor(date)

    fun redTeamCalendar(span: Pair<LocalDate, LocalDate>) = redTeam.redTeamCalendar(span)

}