package no.nav.helse.model

import no.nav.helse.mapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun holidays(): List<NonWorkday> {
    val holidaysText = object {}::class.java.getResource("/helligdagskalender.json")?.readText()
    val holidaysNode = mapper.readTree(holidaysText)
    return holidaysNode.map { NonWorkday(LocalDate.parse(it["dato"].asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy")), it["navn"].asText()) }
}