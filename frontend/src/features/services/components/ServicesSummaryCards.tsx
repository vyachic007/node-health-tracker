import { Card, CardContent, Grid, Stack, Typography } from '@mui/material';
import DnsIcon from '@mui/icons-material/Dns';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import type { NetworkService } from '../model/serviceTypes';

interface ServicesSummaryCardsProps {
    services: NetworkService[];
}

export function ServicesSummaryCards({ services }: ServicesSummaryCardsProps) {
    const total = services.length;
    const up = services.filter((service) => service.lastStatus === 'UP').length;
    const down = services.filter((service) => service.lastStatus === 'DOWN').length;
    const incidents = services.filter((service) => service.hasOpenIncident).length;

    const cards = [
        {
            title: 'Всего сервисов',
            value: total,
            icon: <DnsIcon />,
            color: 'primary.main',
        },
        {
            title: 'Работают',
            value: up,
            icon: <CheckCircleIcon />,
            color: 'success.main',
        },
        {
            title: 'Недоступны',
            value: down,
            icon: <ErrorIcon />,
            color: 'error.main',
        },
        {
            title: 'С открытым инцидентом',
            value: incidents,
            icon: <ReportProblemIcon />,
            color: 'warning.main',
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
        </Grid>
    );
}