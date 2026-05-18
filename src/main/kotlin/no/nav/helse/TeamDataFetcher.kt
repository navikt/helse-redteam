package no.nav.helse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.model.MemberDto
import no.nav.helse.model.TeamDto
import java.io.File

fun teamDataFromFile(): List<TeamDto> {
    val teamJson = if (viKjørerPåEtNaisCluster())
        hentTeamdataFraConfigMap()
    else
        hentTeamdataFraLokaltFilsystem()

    val contents = jacksonObjectMapper().readTree(teamJson)
    return contents.map { file ->
        TeamDto(
            file["name"].asText(),
            file["members"].map {
                MemberDto(
                    it["name"].asText(),
                    it["slackId"].asText()
                )
            }
        )
    }
}

private fun hentTeamdataFraLokaltFilsystem() = object {}.javaClass.getResource("/test-team-data.json")?.readText()

private fun hentTeamdataFraConfigMap() = File("/var/run/configmaps/team-data.json").readText()

private fun viKjørerPåEtNaisCluster() = !System.getenv("NAIS_CLUSTER_NAME").isNullOrEmpty()
