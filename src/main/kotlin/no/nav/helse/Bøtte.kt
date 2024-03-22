package no.nav.helse

import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

/** vet hvordan man henter ut og lagrer ned red-team-greier fra gcp */
interface Bøtte {
    fun hentOverstyringer(): String? = null
    fun lagreDagbestemmelser(overstyringsjson: String): Boolean { return false }
}

class GCPBøtte : Bøtte {
    companion object {
        private val bøttenavn: String = "tbd-red-team-bucket"
        private val logger: Logger = LoggerFactory.getLogger("red-team-bøtte")
    }
    override fun hentOverstyringer(): String = String(hentBøtte().get("dagbestemmelser.json").getContent())

    override fun lagreDagbestemmelser(overstyringsjson: String): Boolean {
        logger.info("Lagrer overstyringer i bøtta")
        return lagre(overstyringsjson, "dagbestemmelser.json")
    }

    private fun lagre(tekst: String, filnavn: String): Boolean {
        val bøtte = hentBøtte()
        val blob = bøtte.get(filnavn)
        if (blob == null) {
            bøtte.create(filnavn, tekst.encodeToByteArray(), "application/json")
        } else {
            val writer = blob.writer()
            writer.write(ByteBuffer.wrap(tekst.encodeToByteArray()))
            writer.close()
        }
        return true
    }

    private fun hentBøtte(): Bucket {
        val storage = StorageOptions.getDefaultInstance().service
        return storage.get(bøttenavn) ?: error("Fant ikke bøtta som heter ${bøttenavn}")
    }
}