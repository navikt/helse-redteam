val ktorVersion = "3.1.2"
val logbackClassicVersion = "1.5.18"
val logbackEncoderVersion = "8.0"
val mockkVersion = "1.13.17"
val jacksonVersion = "2.18.3"
val slackApiModelKotlinExtensionVersion = "1.45.3"
val junitJupiterVersion = "5.12.1"
val gcpBucketVersion = "2.50.0"

plugins {
    kotlin("jvm") version "2.2.0"
}

group = "no.nav.helse"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackApiModelKotlinExtensionVersion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackApiModelKotlinExtensionVersion")

    implementation("com.google.cloud:google-cloud-storage:$gcpBucketVersion")

    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        constraints {
            implementation("commons-codec:commons-codec") {
                version { require("1.13") }
                because("io.ktor:ktor-server-test-host:3.1.0 drar inn sårbar versjon 1.11")
            }
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("21"))
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    jar {
        archiveFileName.set("app.jar")
        manifest {
            attributes["Main-Class"] = "no.nav.helse.ApplicationKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get()
                .filter { it.name != "app.jar" }
                .forEach {
                    val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                    if (!file.exists())
                        it.copyTo(file)
                }
        }
    }
}
