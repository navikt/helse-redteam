package no.nav.helse

class Team(private val devs1: List<String>, private val dev2s: List<String>, private val fag: List<String>) {
    private var counter = 0

    internal fun next(): List<String> {
        return listOf(devs1[counter % devs1.size], dev2s[counter % dev2s.size], fag[counter % fag.size]).also { counter++ }
    }

    fun minLength() = minOf(devs1.size, dev2s.size, fag.size)
    fun maxLength() = maxOf(devs1.size, dev2s.size, fag.size)
}