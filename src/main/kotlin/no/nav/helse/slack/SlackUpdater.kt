package no.nav.helse.slack

import no.nav.helse.model.Day
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Workday
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SlackUpdater(
    private val clock: Clock,
    private val slackClient: RedTeamSlack,
    private val redTeam: RedTeam
) {
    private val logger = LoggerFactory.getLogger("red-team-slack-updater")
    private val now get() = LocalDateTime.now(clock)
    private val today get() = now.toLocalDate()
    private val postTime = 8
    private val tulletidsrommet = LocalTime.of(8, 30)..LocalTime.of(9, 30)
    private var lastPosted = if (now.hour < 8)
        today.minusDays(1) else
        today
    private var tulleLock = false

    fun handleOverride(overrideDate: LocalDate) {
        if (overrideDate != today) return
        slackClient.updateRedTeamGroup(redTeam.teamFor(today) as Workday)
        val redTeamForDay = redTeam.teamFor(today)
        if (redTeamForDay !is Workday) return
        slackClient.postRedTeamOverride(redTeamForDay)
    }

    fun update() {
        val redTeamForDay = redTeam.teamFor(today)
        if (skalPoste(redTeamForDay)) {
            redTeamForDay as Workday
            try {
                slackClient.postRedTeam(redTeamForDay)
                logger.info("Today's red team has been posted to slack")
                slackClient.updateRedTeamGroup(redTeamForDay)
                logger.info("Today's red team has been updated in the slack user group")

                lastPosted = today
            } catch (e: Exception) {
                logger.error("Error occurred attempting to use slack API", e)
            }
        }
        tulle()
    }

    private fun skalPoste(redTeamForDay: Day): Boolean {
        if (lastPosted >= today) {
            logger.info("Poster ikke, forrige gang: $lastPosted")
            return false
        }
        if (now.hour != postTime) {
            logger.info("Poster ikke, ${now.hour} er utenfor tidsvindu (som er $postTime)")
            return false
        }
        if (redTeamForDay !is Workday) {
            logger.info("Poster ikke, $redTeamForDay != arbeidsdag")
            return false
        }
        return true
    }

    private fun erITulletidsrommet() = now.dayOfWeek == DayOfWeek.FRIDAY && now.toLocalTime() in tulletidsrommet

    private fun tulle() {
        if (erITulletidsrommet() && !tulleLock) {
            try {
                slackClient.tullOgFjas()
                logger.info("Tulla litt")
                tulleLock = true
            } catch (e: Exception) {
                logger.error("Error occurred attempting to use slack API", e)
            }
        } else if (!erITulletidsrommet() && tulleLock) {
            tulleLock = false
        }
    }
}
