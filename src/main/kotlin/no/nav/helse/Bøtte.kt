package no.nav.helse

import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions
import java.nio.ByteBuffer

/** vet hvordan man henter ut og lagrer ned red-team-greier fra gcp */
interface Bøtte {
    fun hentOverstyringer(): String? = null
    fun lagreOverstyringer(overstyringsjson: String): Boolean { return false}
}

class GCPBøtte(): Bøtte {
    companion object {
        private val bøttenavn: String = "tbd-red-team-bucket"
    }
    override fun hentOverstyringer(): String? {
        val bucket = hentBøtte()

        println("Leser ut alt i bøtta ${bøttenavn}")
        bucket.list().iterateAll().forEach { blob ->
            println("${blob.name} (content-type: ${blob.contentType}, size: ${blob.size})")
        }
        return null
    }

    override fun lagreOverstyringer(overstyringsjson: String): Boolean {
        val writer = hentBøtte().get("overstyringer.json").writer()
        writer.write(ByteBuffer.wrap(overstyringsjson.encodeToByteArray()))
        writer.close()
        return true
    }

    private fun hentBøtte(): Bucket {
        val storage = StorageOptions.getDefaultInstance().service
        return storage.get(bøttenavn) ?: error("Fant ikke bøtta som heter ${bøttenavn}")
    }
}