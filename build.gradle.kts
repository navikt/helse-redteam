group = "no.nav.helse"

plugins {
    alias(libs.plugins.sykepenger.deployable)
}

sykepengerDeployable {
    mainClass = "no.nav.helse.ApplicationKt"
}

jib {
    container {
        // Månedsnavn i Slack-meldingene formateres med Locale.getDefault()
        jvmFlags = jvmFlags + "-Duser.language=nb"
    }
}

dependencies {
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.slack.api.client.kotlinExtension)
    implementation(libs.slack.api.model.kotlinExtension)
    implementation(libs.google.cloud.storage)

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.server.testHost)
}
