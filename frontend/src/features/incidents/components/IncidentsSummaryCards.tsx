import { Card, CardContent, Grid, Stack, Typography } from '@mui/material';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import ScheduleIcon from '@mui/icons-material/Schedule';
import type { Incident } from '../model/incidentTypes';

interface IncidentsSummaryCardsProps {
    incidents: Incident[];
}

export function IncidentsSummaryCards({ incidents }: IncidentsSummaryCardsProps) {
    const open = incidents.filter((incident) => incident.status === 'OPEN').length;
    const resolved = incidents.filter((incident) => incident.status === 'RESOLVED').length;
    const highSeverity = incidents.filter(
        (incident) => incident.severity === 'HIGH' || incident.severity === 'CRITICAL',
    ).length;

    const latestIncident = [...incidents].sort(
        (a, b) => new Date(b.openedAt).getTime() - new Date(a.openedAt).getTime(),
    )[0];

    const cards = [
        {
            title: 'Открытые',
            value: open,
            icon: <ReportProblemIcon />,
            color: 'error.main',
        },
        {
            title: 'Закрытые',
            value: resolved,
            icon: <CheckCircleIcon />,
            color: 'success.main',
        },
        {
            title: 'Высокая критичность',
            value: highSeverity,
            icon: <PriorityHighIcon />,
            color: 'warning.main',
        },
        {
            title: 'Всего инцидентов',
            value: incidents.length,
            icon: <ScheduleIcon />,
            color: 'primary.main',
        },
    ];

    return (
        <Grid container spacing={2}>
            {cards.map((card) => (
                <Grid key={card.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                    <Card
                        elevation={0}
                        sx={{
                            height: '100%',
                            border: 1,
                            borderColor: 'divider',
                        }}
                    >
                        <CardContent>
                            <Stack spacing={1.5}>
                                <Stack
                                    direction="row"
                                    sx={{
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                    }}
                                >
                                    <Typography color="text.secondary" sx={{ fontWeight: 700 }}>
                                        {card.title}
                                    </Typography>

                                    <Stack sx={{ color: card.color }}>
                                        {card.icon}
                                    </Stack>
                                </Stack>

                                <Typography variant="h4">
                                    {card.value}
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            ))}

            {latestIncident && (
                <Grid size={12}>
                    <Card
                        elevation={0}
                        sx={{
                            border: 1,
                            borderColor: latestIncident.status === 'OPEN' ? 'error.main' : 'divider',
                        }}
                    >
                        <CardContent>
                            <Stack spacing={1}>
                                <Typography color="text.secondary" sx={{ fontWeight: 700 }}>
                                    Последний инцидент
                                </Typography>

                                <Typography variant="h6">
                                    {latestIncident.serviceName}
                                </Typography>

                                <Typography variant="body2" color="text.secondary">
                                    {latestIncident.reason}
                                </Typography>
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>
            )}
        </Grid>
    );
}