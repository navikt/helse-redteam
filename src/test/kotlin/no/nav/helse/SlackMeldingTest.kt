package no.nav.helse

import kotlin.test.assertEquals
import no.nav.helse.slack.tulleMessages
import org.junit.jupiter.api.Test

class SlackMeldingTest {

    @Test
    fun `riktig formatering`() {

        val forventet = ":wave: Morning :hehege: <@ABCDEFGHIJK>. Har du lest slack i 15 min i dag? Kan du fortelle mer om det?"
        val id = "ABCDEFGHIJK"
        val melding = tulleMessages.first()("<@${id}>")
        assertEquals(forventet, melding)
    }
}
