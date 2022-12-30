package no.nav.helse.slack

import no.nav.helse.model.RedTeam
import no.nav.helse.model.Workday
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class SlackUpdater(
    private val clock: () -> LocalDateTime,
    private val slackClient: RedTeamSlack,
    private val redTeam: RedTeam
) {
    private val logger = LoggerFactory.getLogger("red-team-slack-updater")
    private val today get() = clock().toLocalDate()
    private val postTime = 8
    private val tulleTime = 9
    private var nextDayToPost = today.plusDays(1)
    private var tulleLock = false

    fun handleOverride(overrideDate: LocalDate) {
        if (overrideDate == today) {
            slackClient.updateRedTeamGroup(redTeam.teamFor(today) as Workday)
        }
    }

    fun update() {
        val redTeamForDay = redTeam.teamFor(today)
        if (nextDayToPost == today && clock().hour == postTime && (redTeamForDay is Workday)) {
            try {
                slackClient.postRedTeam(redTeamForDay)
                logger.info("Today's red team has been posted to slack")
                slackClient.updateRedTeamGroup(redTeamForDay)
                logger.info("Today's red team has been updated in the slack user group")

                nextDayToPost = today.plusDays(1)
            } catch (e: Exception) {
                logger.error("Error occurred attempting to use slack API", e)
            }
        }
        tulle()
    }


    private fun tulle() {

        if (clock().hour == tulleTime && clock().dayOfWeek == DayOfWeek.FRIDAY && !tulleLock) {
            try {
                slackClient.tulleMedNoen()
                logger.info("Tulla litt")
            } catch (e: Exception) {
                logger.error("Error occurred attempting to use slack API", e)
            }
            tulleLock = true
        } else if (clock().hour != tulleTime && tulleLock) {
            tulleLock = false
        }
    }
}
