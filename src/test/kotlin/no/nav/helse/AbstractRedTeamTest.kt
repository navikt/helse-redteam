package no.nav.helse

import no.nav.helse.model.MemberDto
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Team
import no.nav.helse.model.TeamDto
import java.time.LocalDate

open class AbstractRedTeamTest {

    open protected val START_DATE = LocalDate.of(2022, 1, 3)

    internal fun redTeam() = RedTeam(
        START_DATE,
        Team(
            TeamDto("Spleiselaget", genTeam("Sondre", "David", "Christian")),
            TeamDto("Speilvendt",   genTeam("Jakob", "Sindre")),
            TeamDto("Fag",          genTeam("Morten", "Cecilie"))
        )
    )

    private fun genTeam(vararg names: String) = names.map { MemberDto(it, "slackid-$it") }

}