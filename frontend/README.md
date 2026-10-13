# Red team-frontend

React-app (Vite og TypeScript) som viser hvem som har red team de neste 14 dagene. Du kan overstyre
hvem som har red team for et team en gitt dag. UI-komponentene er fra [Aksel](https://aksel.nav.no).

Ktor bygger ikke frontenden selv, men serverer de ferdigbygde filene fra jaren på samme origin som
API-et. Se `processResources` i `build.gradle.kts` og `configureRouting` i `Application.kt`.

## Kjøre lokalt

Lokalt serverer Ktor de ferdigbygde filene på samme måte som i prod:

1. Bygg frontenden med `pnpm install` og `pnpm run build` i `frontend/`.
2. Start `LocalApp.kt` (i `src/test/kotlin`) fra IntelliJ eller Gradle. Gradle kopierer `dist/` inn som
   ressurser før appen starter. Appen kjører uten Slack og GCP-bøtte, og leser teamdata fra
   `src/test/resources/test-team-data.json`.
3. Åpne `http://localhost:8080`.

Har du endret frontenden, kjører du `pnpm run build` og starter `LocalApp.kt` på nytt.

## Bygge

```bash
pnpm run build
```

Kommandoen lager `dist/`, som Gradle kopierer inn i jaren. Kjør den før `./gradlew build` hvis du
vil ha med frontenden. Finnes ikke `dist/`, bygger Gradle jaren uten frontend. På GitHub bygger
workflowene frontenden før Gradle kjører (se `.github/actions/bygg-frontend`).
