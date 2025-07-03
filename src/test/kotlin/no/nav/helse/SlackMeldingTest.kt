package no.nav.helse

import kotlin.test.assertEquals
import no.nav.helse.slack.tulleMessages
import org.junit.jupiter.api.Test

class SlackMeldingTest {

    @Test
    fun `riktig formatering`() {

        val forventet = ":wave: Morning :hehege: <@U070RMKTUT1>. Har du lest slack i 15 min i dag? Kan du fortelle mer om det?"
        val id = "U070RMKTUT1"
        val melding = tulleMessages.first()("<@${id}>")
        assertEquals(forventet, melding)
    }
}
