package no.nav.helse

import no.nav.helse.model.Overstyring
import no.nav.helse.model.RedTeam
import no.nav.helse.slack.SlackUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

open class RedteamMediator(
    private val slackUpdater: SlackUpdater,
    private val redTeam: RedTeam,
    private val bøtte: Bøtte = object : Bøtte {}
) {

    private val logger: Logger = LoggerFactory.getLogger("red-team-mediator")

    fun override(overstyringer: List<Overstyring>) {
        logger.info("Overriding the following overstyringer: $overstyringer")
        overstyringer.forEach { overstyring ->
            val dato = overstyring.date
            redTeam.override(overstyring.redteamMembers, overstyring.team, dato)
            slackUpdater.handleOverride(dato)
        }
        bøtte.lagreDagbestemmelser(redTeam.dagbestemmelserSomJson())
        logger.info("Completed override of the following overstyringer: $overstyringer")
    }

    fun update() {
        slackUpdater.update()
    }

    fun teamFor(date: LocalDate) = redTeam.teamFor(date)

    open fun redTeamCalendar(span: Pair<LocalDate, LocalDate>) = redTeam.redTeamCalendar(span)

}
