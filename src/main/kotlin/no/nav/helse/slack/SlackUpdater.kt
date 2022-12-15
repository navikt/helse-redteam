package no.nav.helse.slack

import no.nav.helse.model.RedTeam
import no.nav.helse.model.Workday
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

class SlackUpdater(
    private val clock: () -> LocalDateTime,
    private val slackClient: RedTeamSlack,
    private val redTeam: RedTeam
) {
    val logger = LoggerFactory.getLogger("red-team-slack-updater")
    private val today get() = clock().toLocalDate()
    private val postTime = 8
    private var locked = false

    fun handleOverride(overrideDate: LocalDate) {
        if (overrideDate == today) {
            slackClient.updateReadTeamGroup(redTeam.teamFor(today) as Workday)
        }
    }

    fun update() {
        val redTeamForDay = redTeam.teamFor(today)
        if (clock().hour == postTime && (redTeamForDay is Workday) && !locked) {
            try {
                slackClient.postRedTeam(redTeamForDay)
                logger.info("Todays red team has been posted to slack")
                slackClient.updateReadTeamGroup(redTeamForDay)
                logger.info("Todays red team has been updated in the slack user group")
                slackClient.tulleMedHege()
            } catch (e: Exception) {
                logger.error("Error occurred attempting to use slack API", e)
            }
            locked = true
        } else if (clock().hour != postTime && locked) {
            locked = false
        }
    }
}