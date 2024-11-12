val ktorVersion = "2.3.12"
val logbackVersion = "1.4.14"
val logbackEncoderVersion = "7.4"
val kotlinVersion = "1.9.22"
val mockkVersion = "1.13.9"
val jacksonVersion = "2.16.1"
val slackApiModelKotlinExtensionVersion = "1.36.1"
val junitVersion = "5.10.2"
val gcpBucketVersion = "2.35.0"

plugins {
    kotlin("jvm") version "2.0.21"
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
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
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
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }


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
