package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

fun teamDataFromFile(): List<TeamDto> {
    // Hvis appen ikke kjører på NAIS så bruker den test data
    val teamJson = if (!System.getenv("NAIS_CLUSTER_NAME").isNullOrEmpty())
        File("/var/run/configmaps/team-data.json").readText() else
        object {}.javaClass.getResource("/test-team-data.json").readText()

    val teamNode = jacksonObjectMapper().readTree(teamJson)
    return teamNode.map { team ->  TeamDto(team["name"].asText(), team["members"].asSequence().map { MemberDto(it["name"].asText(), it["slackId"].asText()) }.toList()) }
}