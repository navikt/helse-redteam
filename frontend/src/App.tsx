import { useCallback, useEffect, useState } from 'react'
import { Alert, BodyShort, Button, Heading, Page, Table, UNSAFE_Combobox, VStack } from '@navikt/ds-react'
import { hentRedTeam, lagreOverstyringer } from './api'
import type { Day, DayTeam, Overstyring, RedTeamCalendar } from './types'

const helgeemojis = ['🏖️', '🏄', '🥏', '⚽️', '🛌', '🍹', '🌞', '💃', '🎤']

function helgetekst(dato: string): string {
    const emoji = helgeemojis[Number(dato.replaceAll('-', '')) % helgeemojis.length]
    return `${emoji} Ikke red team-dag ${emoji}`
}

// Hvis man går inn på siden på en lørdag eller søndag, mangler den første dagen teams.
function finnTeamnavn(days: Day[]): string[] {
    return days.find((day) => day.teams !== undefined)?.teams?.map((team) => team.team) ?? []
}

function App() {
    const [kalender, setKalender] = useState<RedTeamCalendar | null>(null)
    const [overstyringer, setOverstyringer] = useState<Overstyring[]>([])
    const [feil, setFeil] = useState<string | null>(null)
    const [lagrer, setLagrer] = useState(false)

    const oppdater = useCallback(
        () =>
            hentRedTeam()
                .then((nyKalender) => {
                    setKalender(nyKalender)
                    setFeil(null)
                })
                .catch((err: unknown) => setFeil(`Får ikke kontakt med red team-API-et: ${String(err)}`)),
        [],
    )

    useEffect(() => {
        void oppdater()
    }, [oppdater])

    function valgteMedlemmer(date: string, dayTeam: DayTeam): string[] {
        const overstyring = overstyringer.find((it) => it.date === date && it.team === dayTeam.team)
        return overstyring?.redteamMembers ?? dayTeam.redteamMembers.map((member) => member.name)
    }

    function velg(date: string, dayTeam: DayTeam, medlem: string, erValgt: boolean) {
        const forrige = valgteMedlemmer(date, dayTeam)
        const redteamMembers = erValgt ? [...forrige, medlem] : forrige.filter((it) => it !== medlem)
        setOverstyringer([
            ...overstyringer.filter((it) => it.date !== date || it.team !== dayTeam.team),
            { date, team: dayTeam.team, redteamMembers },
        ])
    }

    async function lagre() {
        setLagrer(true)
        try {
            await lagreOverstyringer(overstyringer)
            setOverstyringer([])
            await oppdater()
        } catch (err) {
            setFeil(`Klarte ikke å oppdatere red team: ${String(err)}`)
        } finally {
            setLagrer(false)
        }
    }

    const teamnavn = kalender ? finnTeamnavn(kalender.days) : []

    return (
        <Page>
            <Page.Block width="xl" gutters>
                <VStack gap="space-6" paddingBlock="space-8 space-4">
                    <Heading level="1" size="large">
                        Red team
                    </Heading>
                    {feil && <Alert variant="error">{feil}</Alert>}
                    {!kalender && !feil && <BodyShort>Henter red team …</BodyShort>}
                    {kalender && (
                        <Table>
                            <Table.Header>
                                <Table.Row>
                                    <Table.HeaderCell scope="col">Dag</Table.HeaderCell>
                                    {teamnavn.map((navn) => (
                                        <Table.HeaderCell key={navn} scope="col">
                                            {navn}
                                        </Table.HeaderCell>
                                    ))}
                                </Table.Row>
                            </Table.Header>
                            <Table.Body>
                                {kalender.days.map((day) => (
                                    <Table.Row key={day.date}>
                                        <Table.HeaderCell scope="row">{day.date}</Table.HeaderCell>
                                        {day.teams ? (
                                            day.teams.map((dayTeam) => (
                                                <Table.DataCell key={dayTeam.team}>
                                                    <UNSAFE_Combobox
                                                        label={`Red team for ${dayTeam.team} ${day.date}`}
                                                        hideLabel
                                                        size="small"
                                                        isMultiSelect
                                                        options={
                                                            kalender.teams
                                                                .find((team) => team.name === dayTeam.team)
                                                                ?.members.map((member) => member.name) ?? []
                                                        }
                                                        selectedOptions={valgteMedlemmer(day.date, dayTeam)}
                                                        onToggleSelected={(medlem, erValgt) =>
                                                            velg(day.date, dayTeam, medlem, erValgt)
                                                        }
                                                    />
                                                </Table.DataCell>
                                            ))
                                        ) : (
                                            <Table.DataCell colSpan={teamnavn.length} align="center">
                                                {helgetekst(day.date)}
                                            </Table.DataCell>
                                        )}
                                    </Table.Row>
                                ))}
                            </Table.Body>
                        </Table>
                    )}
                    {overstyringer.length !== 0 && (
                        <div>
                            <Button onClick={() => void lagre()} loading={lagrer}>
                                Oppdater red team
                            </Button>
                        </div>
                    )}
                </VStack>
            </Page.Block>
        </Page>
    )
}

export default App
