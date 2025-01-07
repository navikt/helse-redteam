package no.nav.helse

import no.nav.helse.model.MemberDto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TeamsDataFetcherTest {

    @Test
    fun `det skal være mulig å ekskludere folk`() {
        val team = teamDataFromFile(listOf("OND_SLACK"))
        val teametTilThéoden = team.filter { team -> team.members.contains(MemberDto("Théoden", "SLACKID678")) }.first()
        assertTrue(teametTilThéoden.members.contains(MemberDto("Théoden", "SLACKID678")))
        assertFalse(teametTilThéoden.members.contains(MemberDto("Sauron", "OND_SLACK")))
    }
}