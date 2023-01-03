package no.nav.helse

import no.nav.helse.model.MemberDto
import no.nav.helse.model.NonWorkday
import no.nav.helse.model.RedTeam
import no.nav.helse.model.Team
import no.nav.helse.model.TeamDto
import java.time.LocalDate

open class AbstractRedTeamTest {

    protected open val START_DATE: LocalDate = LocalDate.of(2022, 1, 3)

    internal fun redTeam(extraNonWorkDays: List<NonWorkday> = emptyList()) = RedTeam(
        START_DATE,
        Team(
            TeamDto("Spleiselaget", genTeam("Sondre", "David", "Christian")),
            TeamDto("Speilvendt",   genTeam("Jakob", "Sindre")),
            TeamDto("Fag",          genTeam("Morten", "Cecilie"))
        ),
        extraNonWorkDays,
    )

    private fun genTeam(vararg names: String) = names.map { MemberDto(it, "slackid-$it") }

}
