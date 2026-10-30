package no.nav.helse

import no.nav.helse.model.MemberDto
import no.nav.helse.model.TeamDto
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

fun teamDataFromFile(): List<TeamDto> {
    val teamJson =
        if (viKjørerPåEtNaisCluster()) {
            hentTeamdataFraConfigMap()
        } else {
            hentTeamdataFraLokaltFilsystem()
        }

    val contents = jacksonObjectMapper().readTree(teamJson)
    return contents.values().map { file ->
        TeamDto(
            file["name"].asString(),
            file["members"].values().map {
                MemberDto(
                    it["name"].asString(),
                    it["slackId"].asString(),
                )
            },
        )
    }
}

private fun hentTeamdataFraLokaltFilsystem() = object {}.javaClass.getResource("/test-team-data.json")?.readText()

private fun hentTeamdataFraConfigMap() = File("/var/run/configmaps/team-data.json").readText()

private fun viKjørerPåEtNaisCluster() = !System.getenv("NAIS_CLUSTER_NAME").isNullOrEmpty()
