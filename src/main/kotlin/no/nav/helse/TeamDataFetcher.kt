package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

fun teamDataFromFile(): List<Pair<String, List<String>>> {
    // Hvis appen ikke kjører på NAIS så bruker den test data
    val teamJson = if (!System.getenv("NAIS_CLUSTER_NAME").isNullOrEmpty())
        File("/var/run/configmaps/team-data.json").readText() else
        object {}.javaClass.getResource("/test-team-data.json").readText()

    val teamNode = jacksonObjectMapper().readTree(teamJson)
    return teamNode.map { team -> team["name"].asText() to team["members"].asSequence().map { it.asText() }.toList() }
}