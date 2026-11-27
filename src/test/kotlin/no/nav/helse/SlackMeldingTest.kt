package no.nav.helse

import no.nav.helse.slack.memeMessages
import no.nav.helse.slack.tulleMessages
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SlackMeldingTest {
    @Test
    fun `alle tulle- og mememeldinger nevner personen`() {
        val person = "<@ABCDEFGHIJK>"
        (tulleMessages + memeMessages).forEach { melding ->
            assertTrue(person in melding(person))
        }
    }
}
