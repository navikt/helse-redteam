export type Member = {
    name: string
    slackId: string
}

export type Team = {
    name: string
    members: Member[]
}

export type DayTeam = {
    team: string
    redteamMembers: Member[]
}

// Helgedager og helligdager har bare dato-feltet, ikke teams-feltet.
export type Day = {
    date: string
    teams?: DayTeam[]
}

export type RedTeamCalendar = {
    teams: Team[]
    days: Day[]
}

export type Overstyring = {
    date: string
    team: string
    redteamMembers: string[]
}
