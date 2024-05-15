# Red team

Applikasjonen red team holder styr på red team-ordningen i team Bømlo og sørger for at 
mennesker som enten er i rotasjonen eller er brukere av red team er godt opplyst om hvem som er på vakt når.

Øvrig dokumentasjon ligger på https://tbd.intern.nav.no/docs/redteam-wiki/.

#### Inkluderer
- [x] Oversikt og overstyring av red team via tbd-siden.
- [x] Persistens av overstyring av red team
- [x] Notifikasjoner på Slack
- [x] Oppdatering av grupper på Slack

### Persistens

I lang tid har overstyringer av hvem som er på red team på en gitt dag forsvunnet hver gang applikasjonen restartes, f.eks ved oppdatering av kode. For å bøte på dette har vi implementert en enkel form for persistens.

#### Hva lagres?
Vi lagrer hvem som har blitt manuelt satt til å være red-team per dag, men ikke den opprinnelige algoritmiske kalenderen.

#### Hvordan lagrer vi det?
Vi lagrer en json-struktur i en GCP-bucket. Vi lagrer _hele_ strukturen _hver_ gang det lages en overstyring. Vi leser opp strukturen hver gang applikasjonen starter.

#### Pros and cons
Vi risikerer at dagbestemmelse-strukturen blir veldig stor over tid, da vi tar var på alle bestemmelser som noensinne er gjort. Dette kan mitigeres ved at vi gjør en manuell redigering av json-strukturen på bøtta i GCP-konsollet.

~Vi implementerte en minste mulige endring, så kjernen av algoritmen er uendret.~

Vi erstattet idéen om overstyring med _dagbestemmelse_ som bare sier hvem som har ansvar hver dag, i stedet for å ta med oss hele kjeden av avløsinger. Vi tror ikke avløser-kjeden ga særlig verdi, og den medførte en del kompleksistet. Bl.a. gjorde den at hele overstyringsopplegget måtte skrives inn på nytt ved nye team-medlemmer.

Selv om vi tar vare på alle dagbestemmelsene, kan vi likevel ikke si at vi har historikk, siden vi ikke sitter med den opprinnelige kalenderen.

Vi mister alt vi tror vi vet om red-team, om vi endrer på team-samensetning, da kjernealgoritmen kalkurerer red-team on-the-fly. Det gjør litt vondt, og vi _kan_ vurdere en enda mer persistent persistens.

#### Alternativer 
- Skrive tilbake til Kubernetes ConfigMap. Dette ville vært en enda mindre endring, men det er ikke anbefalt å skrive til ConfigMap fra podden som bruker ConfigMap. 1) Den er ikke ment å skrives til, 2) det ryktes at podder restartes, når mappet oppdateres
- SQLite. Dette ville likevel krevd en GCP bucket, men gitt oss en større mulighet for historikk. Det ville medført større kode-endring, og det ville blitt vanskeligere å gjøre off-line endringer via GCP-konsollet.
- Redis. Vurdert og forkastet uten noen egentlige argumenter.
- PostgreSQL. Ansett som overkill for dette.

#### Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

#### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen [#team-bømlo-værsågod](https://nav-it.slack.com/archives/C019637N90X).
