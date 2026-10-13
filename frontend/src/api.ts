import type { Overstyring, RedTeamCalendar } from './types'

export class ApiError extends Error {
    status: number

    constructor(message: string, status: number) {
        super(message)
        this.status = status
    }
}

export async function hentRedTeam(): Promise<RedTeamCalendar> {
    const response = await fetch('/red-team')
    if (!response.ok) {
        throw new ApiError(await response.text(), response.status)
    }
    return (await response.json()) as RedTeamCalendar
}

export async function lagreOverstyringer(overstyringer: Overstyring[]): Promise<void> {
    const response = await fetch('/red-team', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(overstyringer),
    })
    if (!response.ok) {
        throw new ApiError(await response.text(), response.status)
    }
}
