package no.nav.helse

class Team(private val devs1: List<String>, private val dev2s: List<String>, private val fag: List<String>) {

    internal fun teamAt(count: Int): List<String> {
        return listOf(devs1[count % devs1.size], dev2s[count % dev2s.size], fag[count % fag.size])
    }

    internal fun teamAt(swaps: List<Swap>, count: Int): List<String> {
        val nextTeam = teamAt(count)
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