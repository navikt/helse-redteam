package no.nav.helse

class Team(private val devs1: List<String>, private val dev2s: List<String>, private val fag: List<String>) {
    private var counter = 0

    internal fun next(): List<String> {
        return listOf(devs1[counter % devs1.size], dev2s[counter % dev2s.size], fag[counter % fag.size]).also { counter++ }
    }

    internal fun next(swaps: List<Swap>): List<String> {
        val nextTeam = next()
        validate(swaps,  nextTeam)
        val replaced = swaps.map { it.from }
        val replacements = swaps.map { it.to }
        return (nextTeam.filterNot { it in replaced } + replacements)
    }

    private fun validate(swaps: List<Swap>) {
        swaps.forEach { swap -> require(listOf(devs1, dev2s, fag).any { groups -> listOf(swap.from, swap.to).all { it in groups } }
        ) { "Invalid swap: ${swap.from} and ${swap.to} not in same group" }}
    }

    private fun validate(swaps: List<Swap>, nextTeam: List<String>) {
        validate(swaps)
        swaps.map { it.from }.forEach { replaced ->
            require(replaced in nextTeam) { "Invalid swap: replaced $replaced not in the team $nextTeam" }
        }
    }

        fun minLength() = minOf(devs1.size, dev2s.size, fag.size)
        fun maxLength() = maxOf(devs1.size, dev2s.size, fag.size)
    }